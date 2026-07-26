package dev.interviewkata.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end persistence guarantees for study sessions: creation, message ordering/roles,
 * history listing, deletion, multiple sessions per topic, and negative paths.
 */
class StudySessionPersistenceIntegrationTest extends AbstractIntegrationTest {

    private UUID realTopicId() {
        return UUID.fromString(rootTopics().get(0).get("id").asText());
    }

    @Test
    @DisplayName("POSITIVE: create session returns id + topic info + empty messages")
    void createSession() {
        UUID topicId = realTopicId();
        ResponseEntity<JsonNode> resp = postJson("/api/study/sessions", Map.of("topicId", topicId.toString()));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("id").asText()).isNotBlank();
        assertThat(body.get("topicName").asText()).isNotBlank();
        assertThat(body.get("topicArea").asText()).isNotBlank();
        assertThat(body.get("messageCount").asInt()).isZero();
        assertThat(body.get("messages")).isEmpty();

        authDelete("/api/study/sessions/" + body.get("id").asText(), Void.class);
    }

    @Test
    @DisplayName("POSITIVE: two messages persist in order USER,AI,USER,AI with correct roles")
    void messagesPersistInOrder() {
        UUID topicId = realTopicId();
        String sessionId = postJson("/api/study/sessions", Map.of("topicId", topicId.toString()))
                .getBody().get("id").asText();

        ResponseEntity<JsonNode> first = postJson(
                "/api/study/sessions/" + sessionId + "/messages", Map.of("message", "first user message"));
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(first.getBody().get("role").asText()).isEqualTo("AI");

        postJson("/api/study/sessions/" + sessionId + "/messages", Map.of("message", "second user message"));

        JsonNode session = getJson("/api/study/sessions/" + sessionId).getBody();
        JsonNode msgs = session.get("messages");
        assertThat(msgs).hasSize(4);
        assertThat(msgs.get(0).get("role").asText()).isEqualTo("USER");
        assertThat(msgs.get(0).get("content").asText()).isEqualTo("first user message");
        assertThat(msgs.get(0).get("sequence").asInt()).isEqualTo(0);
        assertThat(msgs.get(1).get("role").asText()).isEqualTo("AI");
        assertThat(msgs.get(1).get("sequence").asInt()).isEqualTo(1);
        assertThat(msgs.get(2).get("role").asText()).isEqualTo("USER");
        assertThat(msgs.get(2).get("content").asText()).isEqualTo("second user message");
        assertThat(msgs.get(2).get("sequence").asInt()).isEqualTo(2);
        assertThat(msgs.get(3).get("role").asText()).isEqualTo("AI");
        assertThat(msgs.get(3).get("sequence").asInt()).isEqualTo(3);
        assertThat(session.get("messageCount").asInt()).isEqualTo(4);

        authDelete("/api/study/sessions/" + sessionId, Void.class);
    }

    @Test
    @DisplayName("POSITIVE: session appears in history list with messageCount + topic tag")
    void sessionAppearsInHistory() {
        UUID topicId = realTopicId();
        JsonNode created = postJson("/api/study/sessions", Map.of("topicId", topicId.toString())).getBody();
        String sessionId = created.get("id").asText();
        postJson("/api/study/sessions/" + sessionId + "/messages", Map.of("message", "hello tutor"));

        JsonNode list = getJson("/api/study/sessions").getBody();
        JsonNode mine = findById(list, sessionId);
        assertThat(mine).as("created session present in history").isNotNull();
        assertThat(mine.get("messageCount").asInt()).isEqualTo(2);
        assertThat(mine.get("topicId").asText()).isEqualTo(topicId.toString());
        assertThat(mine.get("topicName").asText()).isEqualTo(created.get("topicName").asText());

        authDelete("/api/study/sessions/" + sessionId, Void.class);
    }

    @Test
    @DisplayName("POSITIVE: delete returns 204 then GET → 404 and gone from list")
    void deleteSession() {
        UUID topicId = realTopicId();
        String sessionId = postJson("/api/study/sessions", Map.of("topicId", topicId.toString()))
                .getBody().get("id").asText();

        ResponseEntity<Void> del = authDelete("/api/study/sessions/" + sessionId, Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(getJson("/api/study/sessions/" + sessionId).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(findById(getJson("/api/study/sessions").getBody(), sessionId)).isNull();
    }

    @Test
    @DisplayName("MULTIPLE sessions per topic: two distinct sessions, both in history")
    void multipleSessionsPerTopic() {
        UUID topicId = realTopicId();
        String a = postJson("/api/study/sessions", Map.of("topicId", topicId.toString())).getBody().get("id").asText();
        String b = postJson("/api/study/sessions", Map.of("topicId", topicId.toString())).getBody().get("id").asText();

        assertThat(a).isNotEqualTo(b);
        JsonNode list = getJson("/api/study/sessions").getBody();
        assertThat(findById(list, a)).isNotNull();
        assertThat(findById(list, b)).isNotNull();

        authDelete("/api/study/sessions/" + a, Void.class);
        authDelete("/api/study/sessions/" + b, Void.class);
    }

    @Test
    @DisplayName("NEGATIVE: non-existent topicId → 404")
    void createWithBadTopic() {
        ResponseEntity<JsonNode> resp = postJson(
                "/api/study/sessions", Map.of("topicId", UUID.randomUUID().toString()));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("NEGATIVE: GET/DELETE random session → 404")
    void randomSessionNotFound() {
        String random = UUID.randomUUID().toString();
        assertThat(getJson("/api/study/sessions/" + random).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(authDelete("/api/study/sessions/" + random, JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("NEGATIVE: blank message body → 400 validation")
    void blankMessageRejected() {
        UUID topicId = realTopicId();
        String sessionId = postJson("/api/study/sessions", Map.of("topicId", topicId.toString()))
                .getBody().get("id").asText();

        ResponseEntity<JsonNode> resp = postJson(
                "/api/study/sessions/" + sessionId + "/messages", Map.of("message", "   "));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        authDelete("/api/study/sessions/" + sessionId, Void.class);
    }

    private JsonNode findById(JsonNode array, String id) {
        if (array == null) return null;
        for (JsonNode n : array) {
            if (id.equals(n.get("id").asText())) return n;
        }
        return null;
    }
}
