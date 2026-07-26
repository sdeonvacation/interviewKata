package dev.interviewkata.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flashcard review flow: due-count endpoint, session start, grading a real card (SM-2
 * scheduling + persistence), session summary, and negative paths.
 */
class FlashcardReviewIntegrationTest extends AbstractIntegrationTest {

    private String startReviewSession() {
        return postJson("/api/reviews/start", Map.of("limit", 20)).getBody().get("sessionId").asText();
    }

    @Test
    @DisplayName("POSITIVE: due-card count endpoint responds")
    void dueCount() {
        ResponseEntity<JsonNode> resp = getJson("/api/cards/due");
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().has("count")).isTrue();
        assertThat(resp.getBody().get("count").asLong()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("POSITIVE: grade a real card → SM-2 schedules a future review")
    void gradeCardSchedulesReview() {
        String sessionId = startReviewSession();
        UUID cardId = findAnyCardId();

        ResponseEntity<JsonNode> resp = postJson("/api/reviews/" + sessionId + "/grade",
                Map.of("cardId", cardId.toString(), "grade", 4));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode result = resp.getBody();
        assertThat(result.get("newInterval").asInt()).isGreaterThanOrEqualTo(1);
        assertThat(result.get("nextReviewDate").isNull()).isFalse();

        // Persistence: summary reflects the graded card.
        JsonNode summary = getJson("/api/reviews/" + sessionId + "/summary").getBody();
        assertThat(summary.get("cardsGraded").asInt()).isEqualTo(1);
        assertThat(summary.get("averageGrade").asDouble()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("NEGATIVE: grade non-existent card → 404")
    void gradeMissingCard() {
        String sessionId = startReviewSession();
        ResponseEntity<JsonNode> resp = postJson("/api/reviews/" + sessionId + "/grade",
                Map.of("cardId", UUID.randomUUID().toString(), "grade", 3));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("NEGATIVE: invalid grade value → 400")
    void invalidGrade() {
        String sessionId = startReviewSession();
        Map<String, Object> body = new HashMap<>();
        body.put("cardId", findAnyCardId().toString());
        body.put("grade", 6); // @Max(5)
        assertThat(postJson("/api/reviews/" + sessionId + "/grade", body).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
