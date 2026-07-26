package dev.interviewkata.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * *** Core guarantee: study-chat AI context isolation. ***
 *
 * <p>Proves that the transcript handed to {@code AiService.studyChat} for one session never
 * contains another session's messages, and the topic name never bleeds across sessions —
 * while within a session earlier messages ARE carried forward (continuity).
 */
class StudyContextLeakIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("no cross-session leak: A's transcript never contains B's messages (and vice versa)")
    void noCrossSessionContextLeak() {
        JsonNode roots = rootTopics();
        assumeThat(roots.size()).as("need >=2 seeded root topics").isGreaterThanOrEqualTo(2);

        UUID topicX = UUID.fromString(roots.get(0).get("id").asText());
        UUID topicY = UUID.fromString(roots.get(1).get("id").asText());

        JsonNode createdA = postJson("/api/study/sessions", Map.of("topicId", topicX.toString())).getBody();
        JsonNode createdB = postJson("/api/study/sessions", Map.of("topicId", topicY.toString())).getBody();
        String topicNameA = createdA.get("topicName").asText();
        String topicNameB = createdB.get("topicName").asText();
        assumeThat(topicNameA).as("distinct topic names").isNotEqualTo(topicNameB);

        String sessionA = createdA.get("id").asText();
        String sessionB = createdB.get("id").asText();

        CallCapture cap = new CallCapture();
        when(aiService.studyChat(any(), any(), any())).thenAnswer(inv -> {
            cap.add(inv.getArgument(0), inv.getArgument(1), inv.getArgument(2));
            return "TUTOR_REPLY";
        });

        // Interleave: A, B, A, B
        postJson("/api/study/sessions/" + sessionA + "/messages", Map.of("message", "SECRET_A_MSG1"));
        postJson("/api/study/sessions/" + sessionB + "/messages", Map.of("message", "SECRET_B_MSG1"));
        postJson("/api/study/sessions/" + sessionA + "/messages", Map.of("message", "SECRET_A_MSG2"));
        postJson("/api/study/sessions/" + sessionB + "/messages", Map.of("message", "SECRET_B_MSG2"));

        List<String[]> calls = cap.snapshot();
        assertThat(calls).hasSize(4);

        List<String[]> aCalls = calls.stream().filter(c -> c[1].equals(topicNameA)).toList();
        List<String[]> bCalls = calls.stream().filter(c -> c[1].equals(topicNameB)).toList();
        assertThat(aCalls).hasSize(2);
        assertThat(bCalls).hasSize(2);

        // Isolation: A's transcripts never mention B's secrets, and topicArea matches A's topic.
        for (String[] c : aCalls) {
            assertThat(c[0]).doesNotContain("SECRET_B");
            assertThat(c[1]).isEqualTo(topicNameA);
        }
        for (String[] c : bCalls) {
            assertThat(c[0]).doesNotContain("SECRET_A");
            assertThat(c[1]).isEqualTo(topicNameB);
        }

        // Continuity within session A: the 2nd call carries the 1st message forward.
        assertThat(aCalls.get(1)[0]).contains("SECRET_A_MSG1");
        assertThat(aCalls.get(1)[0]).contains("SECRET_A_MSG2");
        // First call must NOT yet contain the later message.
        assertThat(aCalls.get(0)[0]).doesNotContain("SECRET_A_MSG2");

        authDelete("/api/study/sessions/" + sessionA, Void.class);
        authDelete("/api/study/sessions/" + sessionB, Void.class);
    }
}
