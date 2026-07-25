package dev.interviewkata.seed;

import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicResolverTest {

    @Mock
    private TopicRepository topicRepository;

    private TopicResolver topicResolver;

    private Topic javaCore;
    private Topic collections;

    @BeforeEach
    void setUp() {
        topicResolver = new TopicResolver(topicRepository);

        javaCore = Topic.builder()
                .id(UUID.randomUUID())
                .name("Java Core")
                .area(TopicArea.JAVA_CORE)
                .build();

        collections = Topic.builder()
                .id(UUID.randomUUID())
                .name("Collections")
                .area(TopicArea.JAVA_CORE)
                .parent(javaCore)
                .build();
    }

    @Test
    void resolve_rootTopic_returnsRoot() {
        when(topicRepository.findByParentIdIsNull()).thenReturn(List.of(javaCore));

        Topic resolved = topicResolver.resolve("Java Core");

        assertThat(resolved).isEqualTo(javaCore);
    }

    @Test
    void resolve_childTopic_returnsChild() {
        when(topicRepository.findByParentIdIsNull()).thenReturn(List.of(javaCore));
        when(topicRepository.findByParentId(javaCore.getId())).thenReturn(List.of(collections));

        Topic resolved = topicResolver.resolve("Java Core/Collections");

        assertThat(resolved).isEqualTo(collections);
    }

    @Test
    void resolve_caseInsensitive_matchesIgnoringCase() {
        when(topicRepository.findByParentIdIsNull()).thenReturn(List.of(javaCore));
        when(topicRepository.findByParentId(javaCore.getId())).thenReturn(List.of(collections));

        Topic resolved = topicResolver.resolve("java core/collections");

        assertThat(resolved).isEqualTo(collections);
    }

    @Test
    void resolve_blankPath_throwsException() {
        assertThatThrownBy(() -> topicResolver.resolve(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    void resolve_nullPath_throwsException() {
        assertThatThrownBy(() -> topicResolver.resolve(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    void resolve_unknownRoot_throwsException() {
        when(topicRepository.findByParentIdIsNull()).thenReturn(List.of(javaCore));

        assertThatThrownBy(() -> topicResolver.resolve("Unknown Topic"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Root topic not found");
    }

    @Test
    void resolve_unknownChild_autoCreates() {
        when(topicRepository.findByParentIdIsNull()).thenReturn(List.of(javaCore));
        when(topicRepository.findByParentId(javaCore.getId())).thenReturn(List.of(collections));
        
        Topic newTopic = Topic.builder().id(UUID.randomUUID()).name("Unknown").area(javaCore.getArea()).parent(javaCore).build();
        when(topicRepository.save(any(Topic.class))).thenReturn(newTopic);

        Topic result = topicResolver.resolve("Java Core/Unknown");
        assertThat(result).isEqualTo(newTopic);
        verify(topicRepository).save(any(Topic.class));
    }

    @Test
    void resolve_cachesResults_secondCallDoesNotHitRepo() {
        when(topicRepository.findByParentIdIsNull()).thenReturn(List.of(javaCore));
        when(topicRepository.findByParentId(javaCore.getId())).thenReturn(List.of(collections));

        topicResolver.resolve("Java Core/Collections");
        Topic second = topicResolver.resolve("Java Core/Collections");

        assertThat(second).isEqualTo(collections);
        // findByParentIdIsNull called only once due to caching
        org.mockito.Mockito.verify(topicRepository, org.mockito.Mockito.times(1)).findByParentIdIsNull();
    }

    @Test
    void clearCache_allowsFreshResolution() {
        when(topicRepository.findByParentIdIsNull()).thenReturn(List.of(javaCore));

        topicResolver.resolve("Java Core");
        topicResolver.clearCache();
        topicResolver.resolve("Java Core");

        org.mockito.Mockito.verify(topicRepository, org.mockito.Mockito.times(2)).findByParentIdIsNull();
    }
}
