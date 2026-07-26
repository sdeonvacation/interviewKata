package dev.interviewkata.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * End-to-end persistence + lifecycle for mock interviews: start, answer, turn growth,
 * history, deletion, AI-driven auto-end, and negative paths.
 */
class InterviewPersistenceIntegrationTest extends AbstractIntegrationTest {

    private String startInterview() {
        JsonNode body = postJson("/api/interviews/start",
                Map.of("topicArea", "JAVA_CORE", "difficulty", "MEDIUM")).getBody();
        return body.get("id").asText();
    }

    @Test
    @DisplayName("POSITIVE: start → state ASKING + first turn exists")
    void startCreatesFirstTurn() {
        ResponseEntity<JsonNode> resp = postJson("/api/interviews/start",
                Map.of("topicArea", "JAVA_CORE", "difficulty", "MEDIUM"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = resp.getBody();
        assertThat(body.get("id").asText()).isNotBlank();
        assertThat(body.get("state").asText()).isEqualTo("ASKING");
        assertThat(body.get("topicArea").asText()).isEqualTo("JAVA_CORE");
        assertThat(body.get("difficulty").asText()).isEqualTo("MEDIUM");

        JsonNode turns = getJson("/api/interviews/" + body.get("id").asText() + "/turns").getBody();
        assertThat(turns.size()).isGreaterThanOrEqualTo(1);
        assertThat(turns.get(0).get("turnNumber").asInt()).isEqualTo(1);
        assertThat(turns.get(0).get("aiQuestion").asText()).isNotBlank();

        authDelete("/api/interviews/" + body.get("id").asText(), Void.class);
    }

    @Test
    @DisplayName("POSITIVE: submit answer persists new turn + feeds answer into transcript")
    void submitAnswerPersists() {
        String id = startInterview();

        CallCapture cap = new CallCapture();
        when(aiService.conductInterview(any(), any(), any())).thenAnswer(inv -> {
            cap.add(inv.getArgument(0), inv.getArgument(1), inv.getArgument(2));
            return "NEXT_QUESTION";
        });

        ResponseEntity<JsonNode> answer = postJson(
                "/api/interviews/" + id + "/answer", Map.of("answer", "MY_PERSISTED_ANSWER"));
        assertThat(answer.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(answer.getBody().get("turnNumber").asInt()).isEqualTo(2);

        JsonNode turns = getJson("/api/interviews/" + id + "/turns").getBody();
        assertThat(turns).hasSize(2);

        // The answer was persisted then read back into the transcript passed to the AI.
        List<String[]> calls = cap.snapshot();
        assertThat(calls).hasSize(1);
        assertThat(calls.get(0)[0]).contains("MY_PERSISTED_ANSWER");

        authDelete("/api/interviews/" + id, Void.class);
    }

    @Test
    @DisplayName("POSITIVE: interview appears in history with topicArea/difficulty/turnCount")
    void appearsInHistory() {
        String id = startInterview();
        postJson("/api/interviews/" + id + "/answer", Map.of("answer", "some answer"));

        JsonNode list = getJson("/api/interviews").getBody();
        JsonNode mine = findById(list, id);
        assertThat(mine).isNotNull();
        assertThat(mine.get("topicArea").asText()).isEqualTo("JAVA_CORE");
        assertThat(mine.get("difficulty").asText()).isEqualTo("MEDIUM");
        assertThat(mine.get("turnCount").asInt()).isEqualTo(2);

        authDelete("/api/interviews/" + id, Void.class);
    }

    @Test
    @DisplayName("POSITIVE: delete → 204, GET 404, gone from list, turns cleared")
    void deleteInterview() {
        String id = startInterview();
        postJson("/api/interviews/" + id + "/answer", Map.of("answer", "answer"));

        assertThat(authDelete("/api/interviews/" + id, Void.class).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(getJson("/api/interviews/" + id).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(findById(getJson("/api/interviews").getBody(), id)).isNull();

        ResponseEntity<JsonNode> turns = getJson("/api/interviews/" + id + "/turns");
        assertThat(turns.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(turns.getBody()).isEmpty();
    }

    @Test
    @DisplayName("AUTO-END: AI [INTERVIEW_COMPLETE] transitions interview to COMPLETE + stores feedback")
    void aiDrivenAutoEnd() {
        String id = startInterview();

        when(aiService.conductInterview(any(), any(), any())).thenAnswer(inv -> {
            String transcript = inv.getArgument(0);
            if (transcript != null && transcript.contains("WRAPUP_NOW")) {
                return "Great work, we are done here. [INTERVIEW_COMPLETE]";
            }
            return "NEXT_QUESTION";
        });

        postJson("/api/interviews/" + id + "/answer", Map.of("answer", "final answer WRAPUP_NOW please"));

        JsonNode interview = getJson("/api/interviews/" + id).getBody();
        assertThat(interview.get("state").asText()).isEqualTo("COMPLETE");
        assertThat(interview.get("feedback").asText()).isEqualTo("Great work, we are done here.");

        // Answering a completed interview → 409 conflict.
        assertThat(postJson("/api/interviews/" + id + "/answer", Map.of("answer", "late")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        authDelete("/api/interviews/" + id, Void.class);
    }

    @Test
    @DisplayName("NEGATIVE: missing body → 400")
    void startMissingBody() {
        assertThat(postJson("/api/interviews/start", Map.of()).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("NEGATIVE: invalid enum value → 400")
    void startInvalidEnum() {
        assertThat(postJson("/api/interviews/start",
                Map.of("topicArea", "NOT_A_REAL_AREA", "difficulty", "MEDIUM")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("NEGATIVE: answer on non-existent interview → 404")
    void answerNonExistent() {
        assertThat(postJson("/api/interviews/" + UUID.randomUUID() + "/answer",
                Map.of("answer", "hello")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("NEGATIVE: blank answer → 400")
    void blankAnswer() {
        String id = startInterview();
        assertThat(postJson("/api/interviews/" + id + "/answer", Map.of("answer", "   ")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        authDelete("/api/interviews/" + id, Void.class);
    }

    private JsonNode findById(JsonNode array, String id) {
        if (array == null) return null;
        for (JsonNode n : array) {
            if (id.equals(n.get("id").asText())) return n;
        }
        return null;
    }
}
