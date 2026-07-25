package dev.interviewkata.seed;

import dev.interviewkata.model.Card;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YamlCardParserTest {

    @Mock
    private TopicResolver topicResolver;

    private YamlCardParser parser;
    private Topic topic;

    @BeforeEach
    void setUp() {
        parser = new YamlCardParser(topicResolver);
        topic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Collections")
                .area(TopicArea.JAVA_CORE)
                .build();
    }

    @Test
    void parse_validYaml_returnsCards() throws IOException {
        String yaml = """
                topic: "Java Core/Collections"
                cards:
                  - front: "What is HashMap?"
                    back: "A hash table implementation of Map interface."
                    difficulty: MEDIUM
                    tags: [hashmap, collections]
                  - front: "What is ArrayList?"
                    back: "A resizable array implementation of List."
                    code_snippet: "List<String> list = new ArrayList<>();"
                    explanation: "Backed by Object[] array."
                    difficulty: EASY
                    tags: [arraylist]
                """;
        when(topicResolver.resolve("Java Core/Collections")).thenReturn(topic);

        List<Card> cards = parser.parse(toInputStream(yaml));

        assertThat(cards).hasSize(2);

        Card first = cards.get(0);
        assertThat(first.getFront()).isEqualTo("What is HashMap?");
        assertThat(first.getBack()).isEqualTo("A hash table implementation of Map interface.");
        assertThat(first.getDifficulty()).isEqualTo(Difficulty.MEDIUM);
        assertThat(first.getTags()).containsExactly("hashmap", "collections");
        assertThat(first.getTopic()).isEqualTo(topic);
        assertThat(first.getCodeSnippet()).isNull();

        Card second = cards.get(1);
        assertThat(second.getCodeSnippet()).isEqualTo("List<String> list = new ArrayList<>();");
        assertThat(second.getExplanation()).isEqualTo("Backed by Object[] array.");
        assertThat(second.getDifficulty()).isEqualTo(Difficulty.EASY);
    }

    @Test
    void parse_noTags_returnsEmptyList() throws IOException {
        String yaml = """
                topic: "Java Core/Collections"
                cards:
                  - front: "Q"
                    back: "A"
                    difficulty: HARD
                """;
        when(topicResolver.resolve("Java Core/Collections")).thenReturn(topic);

        List<Card> cards = parser.parse(toInputStream(yaml));

        assertThat(cards.get(0).getTags()).isEmpty();
    }

    @Test
    void parse_invalidYaml_throwsException() {
        String yaml = "not: valid: yaml: [[[";

        assertThatThrownBy(() -> parser.parse(toInputStream(yaml)))
                .isInstanceOf(Exception.class);
    }

    @Test
    void parse_resolvesTopicFromResolver() throws IOException {
        String yaml = """
                topic: "Spring Boot/Core Concepts"
                cards:
                  - front: "Q"
                    back: "A"
                    difficulty: EASY
                """;
        Topic springTopic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Core Concepts")
                .area(TopicArea.SPRING_BOOT)
                .build();
        when(topicResolver.resolve("Spring Boot/Core Concepts")).thenReturn(springTopic);

        List<Card> cards = parser.parse(toInputStream(yaml));

        assertThat(cards.get(0).getTopic()).isEqualTo(springTopic);
    }

    private InputStream toInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
