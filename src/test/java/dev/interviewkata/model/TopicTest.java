package dev.interviewkata.model;

import dev.interviewkata.model.enums.TopicArea;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TopicTest {

    @Test
    void builder_createsInstanceWithDefaults() {
        Topic topic = Topic.builder()
                .name("Java Generics")
                .area(TopicArea.JAVA_CORE)
                .sortOrder(1)
                .build();

        assertThat(topic.getName()).isEqualTo("Java Generics");
        assertThat(topic.getArea()).isEqualTo(TopicArea.JAVA_CORE);
        assertThat(topic.getSortOrder()).isEqualTo(1);
        assertThat(topic.getChildren()).isEmpty();
        assertThat(topic.getParent()).isNull();
    }

    @Test
    void selfReferencing_parentChild() {
        Topic parent = Topic.builder()
                .id(UUID.randomUUID())
                .name("Core Java")
                .area(TopicArea.JAVA_CORE)
                .build();

        Topic child = Topic.builder()
                .name("Generics")
                .area(TopicArea.JAVA_CORE)
                .parent(parent)
                .build();

        parent.getChildren().add(child);

        assertThat(child.getParent()).isEqualTo(parent);
        assertThat(parent.getChildren()).containsExactly(child);
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Topic topic = new Topic(id, "Test", TopicArea.DSA, null, null, "desc", 5, now);

        assertThat(topic.getId()).isEqualTo(id);
        assertThat(topic.getName()).isEqualTo("Test");
        assertThat(topic.getArea()).isEqualTo(TopicArea.DSA);
        assertThat(topic.getDescription()).isEqualTo("desc");
        assertThat(topic.getSortOrder()).isEqualTo(5);
        assertThat(topic.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void noArgsConstructor_createsEmptyInstance() {
        Topic topic = new Topic();
        assertThat(topic.getId()).isNull();
        assertThat(topic.getName()).isNull();
    }
}
