package dev.interviewkata.integration;

import com.fasterxml.jackson.databind.JsonNode;
import dev.interviewkata.ai.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Base class for all integration / end-to-end tests.
 *
 * <p>Uses a REAL HTTP layer (RANDOM_PORT + {@link TestRestTemplate}) against the full Spring
 * context, backed by a REAL PostgreSQL instance provided by a singleton Testcontainer. Liquibase
 * runs migrations and the content seeder populates topics/challenges/cards on startup, so tests
 * have real seeded data to work against.
 *
 * <p>{@link AiService} is replaced with a Mockito {@code @MockBean} so no external LLM is ever
 * called. Default stubs (installed in {@link #resetAiMock()}) return non-null strings because
 * several persistence columns (study message content, interview ai_question) are NOT NULL —
 * an unstubbed mock returning {@code null} would trigger a constraint violation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    protected static final String VALID_AUTH = "Bearer dev-token";

    // Singleton-container pattern: starts once per JVM, reused across every IT class, torn down
    // by Ryuk at JVM exit. No @Testcontainers lifecycle annotation needed.
    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("interviewkata")
                    .withUsername("interviewkata")
                    .withPassword("interviewkata");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate rest;

    @MockBean
    protected AiService aiService;

    @BeforeEach
    void resetAiMock() {
        Mockito.reset(aiService);
        // Non-null defaults so NOT NULL columns are satisfied. Individual tests override with
        // thenAnswer(...) to capture arguments / drive specific behaviour.
        when(aiService.studyChat(any(), any(), any())).thenReturn("TUTOR_DEFAULT_REPLY");
        when(aiService.conductInterview(any(), any(), any())).thenReturn("AI_QUESTION_DEFAULT");
        when(aiService.conductBehavioralInterview(any(), any(), any())).thenReturn("BEHAVIORAL_Q_DEFAULT");
        when(aiService.answerQuestion(any(), any())).thenReturn("ANSWER_DEFAULT");
        when(aiService.generateExplanation(any(), any())).thenReturn("EXPLANATION_DEFAULT");
        when(aiService.evaluateAnswer(any(), any(), any())).thenReturn("{\"score\":5}");
        when(aiService.reviewCode(any(), any())).thenReturn("CODE_REVIEW_DEFAULT");
        when(aiService.generateQuizQuestions(any(), any(), anyInt())).thenReturn("[]");
        when(aiService.generateCards(any(), any(), anyInt())).thenReturn("[]");
    }

    // ---------------------------------------------------------------------
    // HTTP helpers (auth header attached by default)
    // ---------------------------------------------------------------------

    protected String url(String path) {
        return "http://localhost:" + port + path;
    }

    protected HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set(HttpHeaders.AUTHORIZATION, VALID_AUTH);
        return h;
    }

    protected HttpHeaders headers(String authValue) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        if (authValue != null) {
            h.set(HttpHeaders.AUTHORIZATION, authValue);
        }
        return h;
    }

    protected <T> ResponseEntity<T> authGet(String path, Class<T> type) {
        return rest.exchange(url(path), HttpMethod.GET, new HttpEntity<>(authHeaders()), type);
    }

    protected <T> ResponseEntity<T> authPost(String path, Object body, Class<T> type) {
        return rest.exchange(url(path), HttpMethod.POST, new HttpEntity<>(body, authHeaders()), type);
    }

    protected <T> ResponseEntity<T> authDelete(String path, Class<T> type) {
        return rest.exchange(url(path), HttpMethod.DELETE, new HttpEntity<>(authHeaders()), type);
    }

    protected ResponseEntity<JsonNode> getJson(String path) {
        return authGet(path, JsonNode.class);
    }

    protected ResponseEntity<JsonNode> postJson(String path, Object body) {
        return authPost(path, body, JsonNode.class);
    }

    // ---------------------------------------------------------------------
    // Real-data discovery helpers (never hardcode UUIDs)
    // ---------------------------------------------------------------------

    /** Returns the root topics (GET /api/topics). Fails loudly if seed produced none. */
    protected JsonNode rootTopics() {
        ResponseEntity<JsonNode> resp = getJson("/api/topics");
        JsonNode body = resp.getBody();
        if (body == null || !body.isArray() || body.isEmpty()) {
            throw new IllegalStateException("No seeded topics available: " + resp.getStatusCode());
        }
        return body;
    }

    /** Finds any real card id by walking the topic tree via the API. */
    protected UUID findAnyCardId() {
        for (JsonNode root : rootTopics()) {
            UUID found = findCardUnder(UUID.fromString(root.get("id").asText()), 0);
            if (found != null) {
                return found;
            }
        }
        throw new IllegalStateException("No seeded cards found under any topic");
    }

    private UUID findCardUnder(UUID topicId, int depth) {
        ResponseEntity<JsonNode> cards = getJson("/api/topics/" + topicId + "/cards");
        JsonNode cardBody = cards.getBody();
        if (cardBody != null && cardBody.isArray() && !cardBody.isEmpty()) {
            return UUID.fromString(cardBody.get(0).get("id").asText());
        }
        if (depth >= 3) {
            return null;
        }
        ResponseEntity<JsonNode> children = getJson("/api/topics/" + topicId + "/children");
        JsonNode childBody = children.getBody();
        if (childBody != null && childBody.isArray()) {
            for (JsonNode child : childBody) {
                UUID found = findCardUnder(UUID.fromString(child.get("id").asText()), depth + 1);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /** Thread-safe accumulator used by context-leak tests to capture AI call arguments. */
    protected static final class CallCapture {
        public final List<String[]> calls = new ArrayList<>();

        public synchronized void add(String... args) {
            calls.add(args);
        }

        public synchronized List<String[]> snapshot() {
            return new ArrayList<>(calls);
        }
    }
}
