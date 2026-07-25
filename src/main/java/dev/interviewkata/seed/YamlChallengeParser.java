package dev.interviewkata.seed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.interviewkata.model.Challenge;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YamlChallengeParser {

    private final ObjectMapper yamlMapper;
    private final TopicResolver topicResolver;

    public YamlChallengeParser(TopicResolver topicResolver) {
        this.topicResolver = topicResolver;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * Parse a YAML input stream into Challenge entities with resolved topics.
     */
    public List<Challenge> parse(InputStream inputStream) throws IOException {
        ChallengeSeedFile seedFile = yamlMapper.readValue(inputStream, ChallengeSeedFile.class);
        Topic topic = topicResolver.resolve(seedFile.topic);

        List<Challenge> challenges = new ArrayList<>();
        for (ChallengeEntry entry : seedFile.challenges) {
            List<Map<String, Object>> testCases = new ArrayList<>();
            if (entry.testCases != null) {
                for (TestCaseEntry tc : entry.testCases) {
                    testCases.add(Map.of(
                            "input", tc.input,
                            "expected", tc.expected,
                            "description", tc.description != null ? tc.description : ""
                    ));
                }
            }

            Challenge challenge = Challenge.builder()
                    .topic(topic)
                    .title(entry.title)
                    .problemStatement(entry.problemStatement)
                    .difficulty(entry.difficulty)
                    .challengeType(entry.type)
                    .starterCode(entry.starterCode)
                    .testCases(testCases)
                    .hints(entry.hints != null ? entry.hints : List.of())
                    .timeLimitSeconds(entry.timeLimitSeconds > 0 ? entry.timeLimitSeconds : 300)
                    .build();
            challenges.add(challenge);
        }
        return challenges;
    }

    // DTO classes for YAML deserialization

    public static class ChallengeSeedFile {
        public String topic;
        public List<ChallengeEntry> challenges;
    }

    public static class ChallengeEntry {
        public String title;
        public Difficulty difficulty;
        public ChallengeType type;

        @JsonProperty("time_limit_seconds")
        public int timeLimitSeconds;

        @JsonProperty("problem_statement")
        public String problemStatement;

        @JsonProperty("starter_code")
        public String starterCode;

        @JsonProperty("test_cases")
        public List<TestCaseEntry> testCases;

        public List<String> hints;
    }

    public static class TestCaseEntry {
        public String input;
        public String expected;
        public String description;
    }
}
