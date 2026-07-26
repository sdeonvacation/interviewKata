package dev.interviewkata.service;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.model.InterviewTurn;
import dev.interviewkata.model.MockInterview;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.InterviewPhase;
import dev.interviewkata.model.enums.InterviewState;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.InterviewTurnRepository;
import dev.interviewkata.repository.MockInterviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockInterviewEngineTest {

    @Mock
    private MockInterviewRepository mockInterviewRepository;

    @Mock
    private InterviewTurnRepository interviewTurnRepository;

    @Mock
    private AiService aiService;

    private MockInterviewEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MockInterviewEngine(mockInterviewRepository, interviewTurnRepository, aiService);
    }

    @Test
    void startInterview_callsAiForFirstQuestion() {
        when(aiService.conductInterview(eq(""), eq("JAVA_CORE"), eq("INTRO")))
                .thenReturn("What is your experience with Java generics?");

        MockInterview interview = MockInterview.builder()
                .id(UUID.randomUUID())
                .topicArea(TopicArea.JAVA_CORE)
                .difficulty(Difficulty.MEDIUM)
                .state(InterviewState.ASKING)
                .build();
        when(mockInterviewRepository.save(any())).thenReturn(interview);
        when(interviewTurnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        engine.startInterview(TopicArea.JAVA_CORE, Difficulty.MEDIUM);

        ArgumentCaptor<InterviewTurn> turnCaptor = ArgumentCaptor.forClass(InterviewTurn.class);
        verify(interviewTurnRepository).save(turnCaptor.capture());
        assertEquals("What is your experience with Java generics?", turnCaptor.getValue().getAiQuestion());
        assertEquals(InterviewPhase.INTRO, turnCaptor.getValue().getPhase());
    }

    @Test
    void startInterview_aiFails_usesFallbackQuestion() {
        when(aiService.conductInterview(any(), any(), any()))
                .thenReturn("Tell me about your experience with JAVA_CORE. What concepts are you most comfortable with?");

        MockInterview interview = MockInterview.builder()
                .id(UUID.randomUUID())
                .topicArea(TopicArea.JAVA_CORE)
                .difficulty(Difficulty.MEDIUM)
                .state(InterviewState.ASKING)
                .build();
        when(mockInterviewRepository.save(any())).thenReturn(interview);
        when(interviewTurnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        engine.startInterview(TopicArea.JAVA_CORE, Difficulty.MEDIUM);

        ArgumentCaptor<InterviewTurn> turnCaptor = ArgumentCaptor.forClass(InterviewTurn.class);
        verify(interviewTurnRepository).save(turnCaptor.capture());
        assertNotNull(turnCaptor.getValue().getAiQuestion());
        assertFalse(turnCaptor.getValue().getAiQuestion().isBlank());
    }

    @Test
    void submitAnswer_buildsTranscriptAndCallsAi() {
        UUID interviewId = UUID.randomUUID();
        MockInterview interview = MockInterview.builder()
                .id(interviewId)
                .topicArea(TopicArea.SPRING_BOOT)
                .difficulty(Difficulty.MEDIUM)
                .state(InterviewState.ASKING)
                .build();

        InterviewTurn turn1 = InterviewTurn.builder()
                .interview(interview)
                .turnNumber(1)
                .aiQuestion("What is Spring Boot?")
                .userAnswer(null)
                .phase(InterviewPhase.INTRO)
                .build();

        when(mockInterviewRepository.findById(interviewId)).thenReturn(Optional.of(interview));
        when(interviewTurnRepository.findByInterviewIdOrderByTurnNumber(interviewId))
                .thenReturn(List.of(turn1));
        when(interviewTurnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(mockInterviewRepository.save(any())).thenReturn(interview);
        when(aiService.conductInterview(any(), eq("SPRING_BOOT"), eq("INTRO")))
                .thenReturn("Can you explain dependency injection?");

        InterviewTurn result = engine.submitAnswer(interviewId, "Spring Boot is a framework...");

        assertEquals("Can you explain dependency injection?", result.getAiQuestion());
        assertEquals(2, result.getTurnNumber());
        verify(aiService).conductInterview(any(), eq("SPRING_BOOT"), eq("INTRO"));
    }

    @Test
    void submitAnswer_completedInterview_throws() {
        UUID interviewId = UUID.randomUUID();
        MockInterview interview = MockInterview.builder()
                .id(interviewId)
                .topicArea(TopicArea.DSA)
                .difficulty(Difficulty.HARD)
                .state(InterviewState.COMPLETE)
                .build();

        when(mockInterviewRepository.findById(interviewId)).thenReturn(Optional.of(interview));

        assertThrows(IllegalStateException.class,
                () -> engine.submitAnswer(interviewId, "answer"));
    }

    @Test
    void endInterview_setsStateComplete() {
        UUID interviewId = UUID.randomUUID();
        MockInterview interview = MockInterview.builder()
                .id(interviewId)
                .topicArea(TopicArea.SYSTEM_DESIGN)
                .difficulty(Difficulty.HARD)
                .state(InterviewState.ASKING)
                .build();

        when(mockInterviewRepository.findById(interviewId)).thenReturn(Optional.of(interview));
        when(mockInterviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MockInterview result = engine.endInterview(interviewId);

        assertEquals(InterviewState.COMPLETE, result.getState());
        assertNotNull(result.getCompletedAt());
    }

    @Test
    void deleteInterview_deletesTurnsThenInterview() {
        UUID interviewId = UUID.randomUUID();
        when(mockInterviewRepository.existsById(interviewId)).thenReturn(true);

        engine.deleteInterview(interviewId);

        verify(interviewTurnRepository).deleteByInterviewId(interviewId);
        verify(mockInterviewRepository).deleteById(interviewId);
    }

    @Test
    void deleteInterview_missing_throws() {
        UUID interviewId = UUID.randomUUID();
        when(mockInterviewRepository.existsById(interviewId)).thenReturn(false);

        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> engine.deleteInterview(interviewId));
        verify(interviewTurnRepository, never()).deleteByInterviewId(any());
        verify(mockInterviewRepository, never()).deleteById(any());
    }

    @Test
    void listInterviews_mapsSummaries() {
        UUID id = UUID.randomUUID();
        MockInterview interview = MockInterview.builder()
                .id(id)
                .topicArea(TopicArea.DSA)
                .difficulty(Difficulty.MEDIUM)
                .state(InterviewState.COMPLETE)
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .overallScore(8.0)
                .build();
        when(mockInterviewRepository.findAllByOrderByStartedAtDesc()).thenReturn(List.of(interview));
        when(interviewTurnRepository.countByInterviewId(id)).thenReturn(6L);

        var result = engine.listInterviews();

        assertEquals(1, result.size());
        assertEquals("DSA", result.get(0).topicArea());
        assertEquals("MEDIUM", result.get(0).difficulty());
        assertEquals("COMPLETE", result.get(0).state());
        assertEquals(6, result.get(0).turnCount());
        assertEquals(8.0, result.get(0).overallScore());
    }
}
