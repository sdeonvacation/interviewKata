package dev.interviewkata.dto;

import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.TopicArea;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RequestDtoValidationTest {

    @Test
    void startInterviewRequest_holdsValues() {
        StartInterviewRequest req = new StartInterviewRequest(TopicArea.JAVA_CORE, Difficulty.HARD);

        assertEquals(TopicArea.JAVA_CORE, req.topicArea());
        assertEquals(Difficulty.HARD, req.difficulty());
    }

    @Test
    void startReviewRequest_nullableFields() {
        StartReviewRequest req = new StartReviewRequest(null, null, null);

        assertNull(req.topicId());
        assertNull(req.limit());
    }

    @Test
    void startReviewRequest_withValues() {
        UUID topicId = UUID.randomUUID();
        StartReviewRequest req = new StartReviewRequest(topicId, 15, null);

        assertEquals(topicId, req.topicId());
        assertEquals(15, req.limit());
    }

    @Test
    void submitCodeRequest_holdsCode() {
        SubmitCodeRequest req = new SubmitCodeRequest("int x = 1;");

        assertEquals("int x = 1;", req.code());
    }

    @Test
    void submitAnswerRequest_holdsAnswer() {
        SubmitAnswerRequest req = new SubmitAnswerRequest("Polymorphism is...");

        assertEquals("Polymorphism is...", req.answer());
    }
}
