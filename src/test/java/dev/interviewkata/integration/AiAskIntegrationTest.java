package dev.interviewkata.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * /api/ai/ask contract: the service receives exactly the supplied question + context (no
 * unrelated data leaks in), and its answer is returned verbatim. Blank inputs → 400.
 */
class AiAskIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("POSITIVE: answer returned and service receives exact question + context")
    void askPassesExactInputs() {
        CallCapture cap = new CallCapture();
        when(aiService.answerQuestion(any(), any())).thenAnswer(inv -> {
            cap.add(inv.getArgument(0), inv.getArgument(1));
            return "STUBBED_ANSWER";
        });

        ResponseEntity<JsonNode> resp = postJson("/api/ai/ask",
                Map.of("question", "What is a HashMap?", "context", "Java collections basics"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().get("answer").asText()).isEqualTo("STUBBED_ANSWER");

        List<String[]> calls = cap.snapshot();
        assertThat(calls).hasSize(1);
        assertThat(calls.get(0)[0]).isEqualTo("What is a HashMap?");
        assertThat(calls.get(0)[1]).isEqualTo("Java collections basics");
    }

    @Test
    @DisplayName("NEGATIVE: blank question → 400")
    void blankQuestion() {
        Map<String, String> body = new HashMap<>();
        body.put("question", "   ");
        body.put("context", "some context");
        assertThat(postJson("/api/ai/ask", body).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("NEGATIVE: blank context → 400")
    void blankContext() {
        Map<String, String> body = new HashMap<>();
        body.put("question", "a real question");
        body.put("context", "");
        assertThat(postJson("/api/ai/ask", body).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
