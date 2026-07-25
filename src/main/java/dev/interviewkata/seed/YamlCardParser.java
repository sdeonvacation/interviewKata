package dev.interviewkata.seed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.interviewkata.model.Card;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.Difficulty;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class YamlCardParser {

    private final ObjectMapper yamlMapper;
    private final TopicResolver topicResolver;

    public YamlCardParser(TopicResolver topicResolver) {
        this.topicResolver = topicResolver;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * Parse a YAML input stream into Card entities with resolved topics.
     */
    public List<Card> parse(InputStream inputStream) throws IOException {
        CardSeedFile seedFile = yamlMapper.readValue(inputStream, CardSeedFile.class);
        Topic topic = topicResolver.resolve(seedFile.topic);

        List<Card> cards = new ArrayList<>();
        for (CardEntry entry : seedFile.cards) {
            Card card = Card.builder()
                    .topic(topic)
                    .front(entry.front)
                    .back(entry.back)
                    .codeSnippet(entry.codeSnippet)
                    .explanation(entry.explanation)
                    .difficulty(entry.difficulty)
                    .tags(entry.tags != null ? entry.tags : List.of())
                    .build();
            cards.add(card);
        }
        return cards;
    }

    // DTO classes for YAML deserialization

    public static class CardSeedFile {
        public String topic;
        public List<CardEntry> cards;
    }

    public static class CardEntry {
        public String front;
        public String back;

        @JsonProperty("code_snippet")
        public String codeSnippet;

        public String explanation;
        public Difficulty difficulty;
        public List<String> tags;
    }
}
