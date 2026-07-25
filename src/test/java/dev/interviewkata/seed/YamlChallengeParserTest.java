package dev.interviewkata.seed;

import dev.interviewkata.model.Challenge;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.ChallengeType;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YamlChallengeParserTest {

    @Mock
    private TopicResolver topicResolver;

    private YamlChallengeParser parser;
    private Topic topic;

    @BeforeEach
    void setUp() {
        parser = new YamlChallengeParser(topicResolver);
        topic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Arrays")
                .area(TopicArea.DSA)
                .build();
    }

    @Test
    void parse_validYaml_returnsChallenges() throws IOException {
        String yaml = """
                topic: "DSA/Arrays"
                challenges:
                  - title: "Two Sum"
                    difficulty: EASY
                    type: DSA
                    time_limit_seconds: 300
                    problem_statement: |
                      Given an array of integers, return indices of two numbers that add up to target.
                    starter_code: |
                      public int[] twoSum(int[] nums, int target) { return new int[]{}; }
                    test_cases:
                      - input: "nums = [2,7,11,15], target = 9"
                        expected: "[0,1]"
                        description: "Basic case"
                      - input: "nums = [3,3], target = 6"
                        expected: "[0,1]"
                        description: "Duplicates"
                    hints:
                      - "Use a HashMap"
                      - "Store complement as key"
                """;
        when(topicResolver.resolve("DSA/Arrays")).thenReturn(topic);

        List<Challenge> challenges = parser.parse(toInputStream(yaml));

        assertThat(challenges).hasSize(1);

        Challenge challenge = challenges.get(0);
        assertThat(challenge.getTitle()).isEqualTo("Two Sum");
        assertThat(challenge.getDifficulty()).isEqualTo(Difficulty.EASY);
        assertThat(challenge.getChallengeType()).isEqualTo(ChallengeType.DSA);
        assertThat(challenge.getTimeLimitSeconds()).isEqualTo(300);
        assertThat(challenge.getProblemStatement()).contains("array of integers");
        assertThat(challenge.getStarterCode()).contains("twoSum");
        assertThat(challenge.getTopic()).isEqualTo(topic);

        List<Map<String, Object>> testCases = challenge.getTestCases();
        assertThat(testCases).hasSize(2);
        assertThat(testCases.get(0).get("input")).isEqualTo("nums = [2,7,11,15], target = 9");
        assertThat(testCases.get(0).get("expected")).isEqualTo("[0,1]");
        assertThat(testCases.get(0).get("description")).isEqualTo("Basic case");

        assertThat(challenge.getHints()).containsExactly("Use a HashMap", "Store complement as key");
    }

    @Test
    void parse_noHints_returnsEmptyList() throws IOException {
        String yaml = """
                topic: "DSA/Arrays"
                challenges:
                  - title: "Simple"
                    difficulty: EASY
                    type: DSA
                    time_limit_seconds: 180
                    problem_statement: "Do something"
                    starter_code: "// code"
                    test_cases:
                      - input: "1"
                        expected: "1"
                        description: "identity"
                """;
        when(topicResolver.resolve("DSA/Arrays")).thenReturn(topic);

        List<Challenge> challenges = parser.parse(toInputStream(yaml));

        assertThat(challenges.get(0).getHints()).isEmpty();
    }

    @Test
    void parse_defaultTimeLimitWhenZero() throws IOException {
        String yaml = """
                topic: "DSA/Arrays"
                challenges:
                  - title: "No Time"
                    difficulty: MEDIUM
                    type: JAVA
                    time_limit_seconds: 0
                    problem_statement: "Solve it"
                    starter_code: "// code"
                    test_cases:
                      - input: "x"
                        expected: "y"
                        description: "test"
                """;
        when(topicResolver.resolve("DSA/Arrays")).thenReturn(topic);

        List<Challenge> challenges = parser.parse(toInputStream(yaml));

        assertThat(challenges.get(0).getTimeLimitSeconds()).isEqualTo(300);
    }

    @Test
    void parse_nullTestCaseDescription_defaultsToEmpty() throws IOException {
        String yaml = """
                topic: "DSA/Arrays"
                challenges:
                  - title: "Test"
                    difficulty: HARD
                    type: DSA
                    time_limit_seconds: 600
                    problem_statement: "Solve"
                    starter_code: "// code"
                    test_cases:
                      - input: "a"
                        expected: "b"
                    hints:
                      - "hint"
                """;
        when(topicResolver.resolve("DSA/Arrays")).thenReturn(topic);

        List<Challenge> challenges = parser.parse(toInputStream(yaml));

        Map<String, Object> testCase = challenges.get(0).getTestCases().get(0);
        assertThat(testCase.get("description")).isEqualTo("");
    }

    private InputStream toInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
