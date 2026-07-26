package dev.interviewkata.service;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.dto.DtoMapper;
import dev.interviewkata.dto.StudyMessageDto;
import dev.interviewkata.dto.StudySessionDto;
import dev.interviewkata.dto.StudySessionSummaryDto;
import dev.interviewkata.dto.TopicDto;
import dev.interviewkata.model.StudyConversation;
import dev.interviewkata.model.StudyConversationMessage;
import dev.interviewkata.repository.StudyConversationMessageRepository;
import dev.interviewkata.repository.StudyConversationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudySessionService {

    private static final String ROLE_USER = "USER";
    private static final String ROLE_AI = "AI";
    private static final int PREVIEW_MAX_LENGTH = 100;

    private final StudyConversationRepository conversationRepository;
    private final StudyConversationMessageRepository messageRepository;
    private final AiService aiService;
    private final TopicService topicService;

    public StudySessionService(StudyConversationRepository conversationRepository,
                               StudyConversationMessageRepository messageRepository,
                               AiService aiService,
                               TopicService topicService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.aiService = aiService;
        this.topicService = topicService;
    }

    /**
     * Create a fresh conversation for a topic. Multiple sessions per topic are supported;
     * each is tagged with its topic. History is used to revisit older sessions.
     */
    public StudySessionDto createSession(UUID topicId) {
        TopicDto topic = topicService.getTopicById(topicId);
        StudyConversation conversation = StudyConversation.builder()
                .topicId(topicId)
                .topicName(topic.name())
                .topicArea(topic.area().name())
                .build();
        StudyConversation saved = conversationRepository.save(conversation);
        return DtoMapper.toDto(saved, List.of());
    }

    /**
     * Delete a study session and all its messages.
     */
    public void deleteSession(UUID sessionId) {
        if (!conversationRepository.existsById(sessionId)) {
            throw new EntityNotFoundException("Study session not found: " + sessionId);
        }
        messageRepository.deleteByConversationId(sessionId);
        conversationRepository.deleteById(sessionId);
    }

    /**
     * Persist the user message, call the AI with the full transcript, persist the AI reply,
     * and return the AI message.
     */
    public StudyMessageDto sendMessage(UUID sessionId, String userMessage) {
        StudyConversation conversation = conversationRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Study session not found: " + sessionId));

        List<StudyConversationMessage> existing =
                messageRepository.findByConversationIdOrderBySequenceAsc(sessionId);
        int count = existing.size();

        StudyConversationMessage userMsg = StudyConversationMessage.builder()
                .conversation(conversation)
                .role(ROLE_USER)
                .content(userMessage)
                .sequence(count)
                .build();
        messageRepository.save(userMsg);

        List<StudyConversationMessage> all = new ArrayList<>(existing);
        all.add(userMsg);
        String transcript = all.stream()
                .map(m -> (ROLE_USER.equals(m.getRole()) ? "User" : "Tutor") + ": " + m.getContent())
                .collect(Collectors.joining("\n\n"));

        String aiResponse = aiService.studyChat(transcript, conversation.getTopicName(), conversation.getTopicArea());

        StudyConversationMessage aiMsg = StudyConversationMessage.builder()
                .conversation(conversation)
                .role(ROLE_AI)
                .content(aiResponse)
                .sequence(count + 1)
                .build();
        messageRepository.save(aiMsg);

        conversation.setLastActivityAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return DtoMapper.toDto(aiMsg);
    }

    @Transactional(readOnly = true)
    public List<StudySessionSummaryDto> listSessions() {
        return conversationRepository.findAllByOrderByLastActivityAtDesc().stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudySessionDto getSession(UUID sessionId) {
        StudyConversation conversation = conversationRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Study session not found: " + sessionId));
        List<StudyConversationMessage> messages =
                messageRepository.findByConversationIdOrderBySequenceAsc(sessionId);
        return DtoMapper.toDto(conversation, messages);
    }

    private StudySessionSummaryDto toSummary(StudyConversation conversation) {
        long messageCount = messageRepository.countByConversationId(conversation.getId());
        String preview = messageRepository.findByConversationIdOrderBySequenceAsc(conversation.getId()).stream()
                .filter(m -> ROLE_USER.equals(m.getRole()))
                .map(StudyConversationMessage::getContent)
                .findFirst()
                .map(content -> content.length() > PREVIEW_MAX_LENGTH
                        ? content.substring(0, PREVIEW_MAX_LENGTH)
                        : content)
                .orElse("");
        return new StudySessionSummaryDto(
                conversation.getId(),
                conversation.getTopicId(),
                conversation.getTopicName(),
                conversation.getTopicArea(),
                conversation.getStartedAt(),
                conversation.getLastActivityAt(),
                (int) messageCount,
                preview
        );
    }
}
