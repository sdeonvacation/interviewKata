package dev.interviewkata.seed;

import dev.interviewkata.model.DesignExercise;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.TopicArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YamlExerciseParserTest {

    @Mock
    private TopicResolver topicResolver;

    private YamlExerciseParser parser;
    private Topic topic;

    @BeforeEach
    void setUp() {
        parser = new YamlExerciseParser(topicResolver);
        topic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Scalability")
                .area(TopicArea.SYSTEM_DESIGN)
                .build();
    }

    @Test
    void parse_validYaml_returnsExercises() throws IOException {
        String yaml = """
                topic: "System Design/Scalability"
                exercises:
                  - title: "Design a URL Shortener"
                    difficulty: MEDIUM
                    estimated_minutes: 45
                    prompt: |
                      Design a URL shortening service.
                      Handle 100M URLs.
                    constraints: |
                      - Redirects < 10ms
                      - 99.99% uptime
                    evaluation_rubric:
                      categories:
                        - name: "Scalability"
                          weight: 0.3
                          criteria: ["Horizontal scaling", "Caching strategy"]
                  - title: "Design a Rate Limiter"
                    difficulty: HARD
                    estimated_minutes: 30
                    prompt: "Design a distributed rate limiter."
                    constraints: "- Sub-ms latency"
                    evaluation_rubric:
                      categories:
                        - name: "Algorithm"
                          weight: 0.5
                          criteria: ["Token bucket", "Sliding window"]
                """;
        when(topicResolver.resolve("System Design/Scalability")).thenReturn(topic);

        List<DesignExercise> exercises = parser.parse(toInputStream(yaml));

        assertThat(exercises).hasSize(2);

        DesignExercise first = exercises.get(0);
        assertThat(first.getTitle()).isEqualTo("Design a URL Shortener");
        assertThat(first.getDifficulty()).isEqualTo(Difficulty.MEDIUM);
        assertThat(first.getEstimatedMinutes()).isEqualTo(45);
        assertThat(first.getPrompt()).contains("Design a URL shortening service");
        assertThat(first.getConstraints()).contains("Redirects < 10ms");
        assertThat(first.getTopic()).isEqualTo(topic);
        assertThat(first.getEvaluationRubric()).isNotNull();
        assertThat(first.getEvaluationRubric()).containsKey("categories");

        DesignExercise second = exercises.get(1);
        assertThat(second.getTitle()).isEqualTo("Design a Rate Limiter");
        assertThat(second.getDifficulty()).isEqualTo(Difficulty.HARD);
        assertThat(second.getEstimatedMinutes()).isEqualTo(30);
    }

    @Test
    void parse_resolvesTopicFromResolver() throws IOException {
        String yaml = """
                topic: "Architecture/Design Patterns"
                exercises:
                  - title: "Design Plugin Architecture"
                    difficulty: MEDIUM
                    estimated_minutes: 30
                    prompt: "Design a plugin system."
                    constraints: "- Hot reload support"
                    evaluation_rubric:
                      categories:
                        - name: "API Design"
                          weight: 1.0
                          criteria: ["Extension points"]
                """;
        Topic archTopic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Design Patterns")
                .area(TopicArea.ARCHITECTURE)
                .build();
        when(topicResolver.resolve("Architecture/Design Patterns")).thenReturn(archTopic);

        List<DesignExercise> exercises = parser.parse(toInputStream(yaml));

        assertThat(exercises.get(0).getTopic()).isEqualTo(archTopic);
    }

    @Test
    void parse_invalidYaml_throwsException() {
        String yaml = "not: valid: yaml: [[[";

        assertThatThrownBy(() -> parser.parse(toInputStream(yaml)))
                .isInstanceOf(Exception.class);
    }

    @Test
    void parse_evaluationRubricPreservesStructure() throws IOException {
        String yaml = """
                topic: "System Design/Scalability"
                exercises:
                  - title: "Test Exercise"
                    difficulty: EASY
                    estimated_minutes: 15
                    prompt: "Test prompt"
                    constraints: "Test constraints"
                    evaluation_rubric:
                      categories:
                        - name: "Design"
                          weight: 0.5
                          criteria: ["Criterion A", "Criterion B"]
                        - name: "Implementation"
                          weight: 0.5
                          criteria: ["Criterion C"]
                """;
        when(topicResolver.resolve("System Design/Scalability")).thenReturn(topic);

        List<DesignExercise> exercises = parser.parse(toInputStream(yaml));

        Map<String, Object> rubric = exercises.get(0).getEvaluationRubric();
        assertThat(rubric).containsKey("categories");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categories = (List<Map<String, Object>>) rubric.get("categories");
        assertThat(categories).hasSize(2);
        assertThat(categories.get(0).get("name")).isEqualTo("Design");
        assertThat(categories.get(0).get("weight")).isEqualTo(0.5);
    }

    @Test
    void parse_emptyExercisesList_returnsEmptyList() throws IOException {
        String yaml = """
                topic: "System Design/Scalability"
                exercises: []
                """;
        when(topicResolver.resolve("System Design/Scalability")).thenReturn(topic);

        List<DesignExercise> exercises = parser.parse(toInputStream(yaml));

        assertThat(exercises).isEmpty();
    }

    private InputStream toInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
