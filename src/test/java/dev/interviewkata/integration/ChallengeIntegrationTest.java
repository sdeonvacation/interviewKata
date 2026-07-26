package dev.interviewkata.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Challenge listing (paging + filtering) and detail retrieval, plus a not-found path.
 * Code submission is intentionally excluded to avoid exercising the heavy JShell sandbox.
 */
class ChallengeIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("POSITIVE: size=200 returns >20 challenges and content matches totalElements")
    void largePageReturnsAllChallenges() {
        ResponseEntity<JsonNode> resp = getJson("/api/challenges?page=0&size=200");
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = resp.getBody();
        assertThat(body).isNotNull();
        JsonNode content = body.get("content");
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isGreaterThan(20);
        // All challenges fit on one 200-sized page → content count == total.
        assertThat(content.size()).isEqualTo(body.get("totalElements").asInt());
    }

    @Test
    @DisplayName("POSITIVE: filter type=DSA returns only DSA challenges")
    void filterByType() {
        JsonNode content = getJson("/api/challenges?type=DSA&size=200").getBody().get("content");
        assertThat(content.size()).isGreaterThan(0);
        for (JsonNode c : content) {
            assertThat(c.get("challengeType").asText()).isEqualTo("DSA");
        }
    }

    @Test
    @DisplayName("POSITIVE: detail returns problem statement + starter code")
    void challengeDetail() {
        JsonNode content = getJson("/api/challenges?size=200").getBody().get("content");
        String id = content.get(0).get("id").asText();

        ResponseEntity<JsonNode> resp = getJson("/api/challenges/" + id);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode detail = resp.getBody();
        assertThat(detail.get("id").asText()).isEqualTo(id);
        assertThat(detail.get("problemStatement").asText()).isNotBlank();
        assertThat(detail.has("starterCode")).isTrue();
    }

    @Test
    @DisplayName("NEGATIVE: random challenge id → 404")
    void challengeNotFound() {
        assertThat(getJson("/api/challenges/" + UUID.randomUUID()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
