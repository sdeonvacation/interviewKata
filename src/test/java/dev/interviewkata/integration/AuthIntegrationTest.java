package dev.interviewkata.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the SimpleAuthFilter guarding /api/**. Missing / wrong bearer token → 401,
 * correct token → 200.
 */
class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("NEGATIVE: no Authorization header → 401")
    void noAuthHeaderRejected() {
        ResponseEntity<JsonNode> resp = rest.exchange(
                url("/api/topics"), HttpMethod.GET,
                new HttpEntity<>(headers(null)), JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("NEGATIVE: wrong token → 401")
    void wrongTokenRejected() {
        ResponseEntity<JsonNode> resp = rest.exchange(
                url("/api/topics"), HttpMethod.GET,
                new HttpEntity<>(headers("Bearer not-the-token")), JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("POSITIVE: correct Bearer dev-token → 200")
    void correctTokenAccepted() {
        ResponseEntity<JsonNode> resp = authGet("/api/topics", JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().isArray()).isTrue();
    }
}
