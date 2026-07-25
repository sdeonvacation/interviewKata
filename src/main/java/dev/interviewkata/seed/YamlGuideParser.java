package dev.interviewkata.seed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.interviewkata.model.Guide;
import dev.interviewkata.model.Topic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class YamlGuideParser {

    private final ObjectMapper yamlMapper;
    private final TopicResolver topicResolver;

    public YamlGuideParser(TopicResolver topicResolver) {
        this.topicResolver = topicResolver;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * Parse a YAML input stream into Guide entities with resolved topics.
     */
    public List<Guide> parse(InputStream inputStream) throws IOException {
        GuideSeedFile seedFile = yamlMapper.readValue(inputStream, GuideSeedFile.class);
        Topic topic = topicResolver.resolve(seedFile.topic);

        List<Guide> guides = new ArrayList<>();
        int sortOrder = 1;
        for (GuideEntry entry : seedFile.guides) {
            Guide guide = Guide.builder()
                    .topic(topic)
                    .title(entry.title)
                    .contentMarkdown(entry.content)
                    .estimatedMinutes(entry.estimatedMinutes)
                    .sortOrder(sortOrder++)
                    .build();
            guides.add(guide);
        }
        return guides;
    }

    // DTO classes for YAML deserialization

    public static class GuideSeedFile {
        public String topic;
        public List<GuideEntry> guides;
    }

    public static class GuideEntry {
        public String title;

        @JsonProperty("estimated_minutes")
        public int estimatedMinutes;

        public String content;
    }
}
