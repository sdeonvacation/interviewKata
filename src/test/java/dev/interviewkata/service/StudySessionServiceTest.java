package dev.interviewkata.service;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.dto.StudyMessageDto;
import dev.interviewkata.dto.StudySessionDto;
import dev.interviewkata.dto.StudySessionSummaryDto;
import dev.interviewkata.dto.TopicDto;
import dev.interviewkata.model.StudyConversation;
import dev.interviewkata.model.StudyConversationMessage;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.StudyConversationMessageRepository;
import dev.interviewkata.repository.StudyConversationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudySessionServiceTest {

    @Mock
    private StudyConversationRepository conversationRepository;

    @Mock
    private StudyConversationMessageRepository messageRepository;

    @Mock
    private AiService aiService;

    @Mock
    private TopicService topicService;

    private StudySessionService service;

    @BeforeEach
    void setUp() {
        service = new StudySessionService(conversationRepository, messageRepository, aiService, topicService);
    }

    @Test
    void createSession_alwaysCreatesNewSessionFromTopic() {
        UUID topicId = UUID.randomUUID();
        when(topicService.getTopicById(topicId))
                .thenReturn(new TopicDto(topicId, "HashMap Internals", TopicArea.JAVA_CORE,
                        null, null, 0, 0, 5));
        when(conversationRepository.save(any())).thenAnswer(inv -> {
            StudyConversation c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        StudySessionDto result = service.createSession(topicId);

        assertEquals("HashMap Internals", result.topicName());
        assertEquals("JAVA_CORE", result.topicArea());
        assertEquals(topicId, result.topicId());
        assertTrue(result.messages().isEmpty());
        assertEquals(0, result.messageCount());

        ArgumentCaptor<StudyConversation> captor = ArgumentCaptor.forClass(StudyConversation.class);
        verify(conversationRepository).save(captor.capture());
        assertEquals("HashMap Internals", captor.getValue().getTopicName());
    }

    @Test
    void deleteSession_deletesMessagesThenConversation() {
        UUID sessionId = UUID.randomUUID();
        when(conversationRepository.existsById(sessionId)).thenReturn(true);

        service.deleteSession(sessionId);

        verify(messageRepository).deleteByConversationId(sessionId);
        verify(conversationRepository).deleteById(sessionId);
    }

    @Test
    void deleteSession_missing_throws() {
        UUID sessionId = UUID.randomUUID();
        when(conversationRepository.existsById(sessionId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.deleteSession(sessionId));
        verify(messageRepository, never()).deleteByConversationId(any());
        verify(conversationRepository, never()).deleteById(any());
    }

    @Test
    void sendMessage_persistsUserAndAiAndCallsAiWithTranscript() {
        UUID sessionId = UUID.randomUUID();
        StudyConversation conversation = StudyConversation.builder()
                .id(sessionId)
                .topicId(UUID.randomUUID())
                .topicName("Database Indexes")
                .topicArea("DATABASE")
                .startedAt(LocalDateTime.now())
                .lastActivityAt(LocalDateTime.now())
                .build();

        StudyConversationMessage priorUser = StudyConversationMessage.builder()
                .conversation(conversation).role("USER").content("What is an index?").sequence(0).build();
        StudyConversationMessage priorAi = StudyConversationMessage.builder()
                .conversation(conversation).role("AI").content("An index speeds up lookups.").sequence(1).build();

        when(conversationRepository.findById(sessionId)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationIdOrderBySequenceAsc(sessionId))
                .thenReturn(List.of(priorUser, priorAi));
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(conversationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(aiService.studyChat(anyString(), eq("Database Indexes"), eq("DATABASE")))
                .thenReturn("B-tree indexes are the default.");

        StudyMessageDto result = service.sendMessage(sessionId, "Tell me about B-tree indexes");

        assertEquals("AI", result.role());
        assertEquals("B-tree indexes are the default.", result.content());
        assertEquals(3, result.sequence());

        ArgumentCaptor<String> transcriptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiService).studyChat(transcriptCaptor.capture(), eq("Database Indexes"), eq("DATABASE"));
        String transcript = transcriptCaptor.getValue();
        assertTrue(transcript.contains("User: What is an index?"));
        assertTrue(transcript.contains("Tutor: An index speeds up lookups."));
        assertTrue(transcript.contains("User: Tell me about B-tree indexes"));

        // user message + ai message saved
        verify(messageRepository, times(2)).save(any());
        verify(conversationRepository).save(conversation);
    }

    @Test
    void sendMessage_missingSession_throws() {
        UUID sessionId = UUID.randomUUID();
        when(conversationRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.sendMessage(sessionId, "hi"));
    }

    @Test
    void listSessions_mapsSummariesWithCountAndPreview() {
        UUID sessionId = UUID.randomUUID();
        StudyConversation conversation = StudyConversation.builder()
                .id(sessionId)
                .topicId(UUID.randomUUID())
                .topicName("Concurrency")
                .topicArea("JAVA_CORE")
                .startedAt(LocalDateTime.now())
                .lastActivityAt(LocalDateTime.now())
                .build();
        StudyConversationMessage user = StudyConversationMessage.builder()
                .conversation(conversation).role("USER").content("Explain volatile").sequence(0).build();
        StudyConversationMessage ai = StudyConversationMessage.builder()
                .conversation(conversation).role("AI").content("volatile ensures visibility").sequence(1).build();

        when(conversationRepository.findAllByOrderByLastActivityAtDesc())
                .thenReturn(List.of(conversation));
        when(messageRepository.countByConversationId(sessionId)).thenReturn(2L);
        when(messageRepository.findByConversationIdOrderBySequenceAsc(sessionId))
                .thenReturn(List.of(user, ai));

        List<StudySessionSummaryDto> result = service.listSessions();

        assertEquals(1, result.size());
        StudySessionSummaryDto summary = result.get(0);
        assertEquals("Concurrency", summary.topicName());
        assertEquals(2, summary.messageCount());
        assertEquals("Explain volatile", summary.preview());
    }

    @Test
    void getSession_returnsMessages() {
        UUID sessionId = UUID.randomUUID();
        StudyConversation conversation = StudyConversation.builder()
                .id(sessionId)
                .topicId(UUID.randomUUID())
                .topicName("Kafka")
                .topicArea("ARCHITECTURE")
                .startedAt(LocalDateTime.now())
                .lastActivityAt(LocalDateTime.now())
                .build();
        StudyConversationMessage msg = StudyConversationMessage.builder()
                .conversation(conversation).role("USER").content("What is a partition?").sequence(0).build();

        when(conversationRepository.findById(sessionId)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationIdOrderBySequenceAsc(sessionId))
                .thenReturn(List.of(msg));

        StudySessionDto result = service.getSession(sessionId);

        assertEquals(sessionId, result.id());
        assertEquals(1, result.messages().size());
        assertEquals("What is a partition?", result.messages().get(0).content());
    }

    @Test
    void getSession_missing_throws() {
        UUID sessionId = UUID.randomUUID();
        when(conversationRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.getSession(sessionId));
    }
}
