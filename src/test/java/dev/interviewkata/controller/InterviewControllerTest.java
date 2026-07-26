package dev.interviewkata.controller;

import dev.interviewkata.dto.StartInterviewRequest;
import dev.interviewkata.dto.SubmitAnswerRequest;
import dev.interviewkata.model.InterviewTurn;
import dev.interviewkata.model.MockInterview;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.InterviewPhase;
import dev.interviewkata.model.enums.InterviewState;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.service.MockInterviewEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewControllerTest {

    @Mock
    private MockInterviewEngine mockInterviewEngine;

    private InterviewController controller;

    @BeforeEach
    void setUp() {
        controller = new InterviewController(mockInterviewEngine);
    }

    @Test
    void startInterview_validRequest_returnsInterview() {
        StartInterviewRequest request = new StartInterviewRequest(TopicArea.JAVA_CORE, Difficulty.MEDIUM);
        MockInterview interview = MockInterview.builder()
                .id(UUID.randomUUID())
                .topicArea(TopicArea.JAVA_CORE)
                .difficulty(Difficulty.MEDIUM)
                .state(InterviewState.ASKING)
                .build();
        when(mockInterviewEngine.startInterview(TopicArea.JAVA_CORE, Difficulty.MEDIUM)).thenReturn(interview);

        ResponseEntity<MockInterview> result = controller.startInterview(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(interview, result.getBody());
        verify(mockInterviewEngine).startInterview(TopicArea.JAVA_CORE, Difficulty.MEDIUM);
    }

    @Test
    void submitAnswer_validRequest_returnsTurn() {
        UUID id = UUID.randomUUID();
        SubmitAnswerRequest request = new SubmitAnswerRequest("My answer about polymorphism");
        InterviewTurn turn = InterviewTurn.builder()
                .id(UUID.randomUUID())
                .userAnswer("My answer about polymorphism")
                .phase(InterviewPhase.TECHNICAL)
                .build();
        when(mockInterviewEngine.submitAnswer(id, "My answer about polymorphism")).thenReturn(turn);

        ResponseEntity<?> result = controller.submitAnswer(id, request);

        assertEquals(200, result.getStatusCode().value());
        verify(mockInterviewEngine).submitAnswer(id, "My answer about polymorphism");
    }

    @Test
    void listInterviews_delegatesToService() {
        var summary = new dev.interviewkata.dto.InterviewSummaryDto(
                UUID.randomUUID(), "DSA", "MEDIUM", "COMPLETE",
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), 6, 8.0);
        when(mockInterviewEngine.listInterviews()).thenReturn(java.util.List.of(summary));

        var result = controller.listInterviews();

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        verify(mockInterviewEngine).listInterviews();
    }

    @Test
    void deleteInterview_delegatesToService() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> result = controller.deleteInterview(id);

        assertEquals(204, result.getStatusCode().value());
        verify(mockInterviewEngine).deleteInterview(id);
    }
}
