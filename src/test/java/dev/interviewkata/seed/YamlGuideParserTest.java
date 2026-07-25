package dev.interviewkata.seed;

import dev.interviewkata.model.Guide;
import dev.interviewkata.model.Topic;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YamlGuideParserTest {

    @Mock
    private TopicResolver topicResolver;

    private YamlGuideParser parser;
    private Topic topic;

    @BeforeEach
    void setUp() {
        parser = new YamlGuideParser(topicResolver);
        topic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Core Concepts")
                .area(TopicArea.SPRING_BOOT)
                .build();
    }

    @Test
    void parse_validYaml_returnsGuides() throws IOException {
        String yaml = """
                topic: "Spring Boot/Core Concepts"
                guides:
                  - title: "Understanding DI"
                    estimated_minutes: 15
                    content: |
                      # Dependency Injection
                      
                      DI is a design pattern.
                  - title: "Bean Lifecycle"
                    estimated_minutes: 10
                    content: |
                      # Lifecycle
                      
                      Beans have lifecycle callbacks.
                """;
        when(topicResolver.resolve("Spring Boot/Core Concepts")).thenReturn(topic);

        List<Guide> guides = parser.parse(toInputStream(yaml));

        assertThat(guides).hasSize(2);

        Guide first = guides.get(0);
        assertThat(first.getTitle()).isEqualTo("Understanding DI");
        assertThat(first.getEstimatedMinutes()).isEqualTo(15);
        assertThat(first.getContentMarkdown()).contains("# Dependency Injection");
        assertThat(first.getTopic()).isEqualTo(topic);
        assertThat(first.getSortOrder()).isEqualTo(1);

        Guide second = guides.get(1);
        assertThat(second.getTitle()).isEqualTo("Bean Lifecycle");
        assertThat(second.getSortOrder()).isEqualTo(2);
    }

    @Test
    void parse_assignsIncrementingSortOrder() throws IOException {
        String yaml = """
                topic: "Spring Boot/Core Concepts"
                guides:
                  - title: "A"
                    estimated_minutes: 5
                    content: "content a"
                  - title: "B"
                    estimated_minutes: 5
                    content: "content b"
                  - title: "C"
                    estimated_minutes: 5
                    content: "content c"
                """;
        when(topicResolver.resolve("Spring Boot/Core Concepts")).thenReturn(topic);

        List<Guide> guides = parser.parse(toInputStream(yaml));

        assertThat(guides.get(0).getSortOrder()).isEqualTo(1);
        assertThat(guides.get(1).getSortOrder()).isEqualTo(2);
        assertThat(guides.get(2).getSortOrder()).isEqualTo(3);
    }

    private InputStream toInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
