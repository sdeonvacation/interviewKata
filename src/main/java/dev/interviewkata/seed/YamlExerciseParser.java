package dev.interviewkata.seed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.interviewkata.model.DesignExercise;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.Difficulty;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YamlExerciseParser {

    private final ObjectMapper yamlMapper;
    private final TopicResolver topicResolver;

    public YamlExerciseParser(TopicResolver topicResolver) {
        this.topicResolver = topicResolver;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * Parse a YAML input stream into DesignExercise entities with resolved topics.
     */
    public List<DesignExercise> parse(InputStream inputStream) throws IOException {
        ExerciseSeedFile seedFile = yamlMapper.readValue(inputStream, ExerciseSeedFile.class);
        Topic topic = topicResolver.resolve(seedFile.topic);

        List<DesignExercise> exercises = new ArrayList<>();
        for (ExerciseEntry entry : seedFile.exercises) {
            DesignExercise exercise = DesignExercise.builder()
                    .topic(topic)
                    .title(entry.title)
                    .difficulty(entry.difficulty)
                    .estimatedMinutes(entry.estimatedMinutes)
                    .prompt(entry.prompt)
                    .constraints(entry.constraints)
                    .evaluationRubric(entry.evaluationRubric)
                    .referenceApproach(entry.referenceApproach)
                    .build();
            exercises.add(exercise);
        }
        return exercises;
    }

    // DTO classes for YAML deserialization

    public static class ExerciseSeedFile {
        public String topic;
        public List<ExerciseEntry> exercises;
    }

    public static class ExerciseEntry {
        public String title;
        public Difficulty difficulty;

        @JsonProperty("estimated_minutes")
        public int estimatedMinutes;

        public String prompt;
        public String constraints;

        @JsonProperty("evaluation_rubric")
        public Map<String, Object> evaluationRubric;

        @JsonProperty("reference_approach")
        public String referenceApproach;
    }
}
