package dev.interviewkata.service;

import dev.interviewkata.dto.TopicDto;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.repository.TopicRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TopicService topicService;

    private Topic rootTopic;
    private Topic childTopic;

    @BeforeEach
    void setUp() {
        rootTopic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Java Core")
                .area(TopicArea.JAVA_CORE)
                .children(new ArrayList<>())
                .build();

        childTopic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Collections")
                .area(TopicArea.JAVA_CORE)
                .parent(rootTopic)
                .children(new ArrayList<>())
                .build();

        rootTopic.getChildren().add(childTopic);
    }

    @Test
    void getTopicTree_usesCountByTopicOrParent() {
        when(topicRepository.findByParentIdIsNull()).thenReturn(List.of(rootTopic));
        when(cardRepository.countByTopicOrParent(rootTopic.getId())).thenReturn(10L);

        List<TopicDto> result = topicService.getTopicTree();

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).cardCount());
        verify(cardRepository).countByTopicOrParent(rootTopic.getId());
    }

    @Test
    void getTopicTree_countsChildrenCorrectly() {
        when(topicRepository.findByParentIdIsNull()).thenReturn(List.of(rootTopic));
        when(cardRepository.countByTopicOrParent(rootTopic.getId())).thenReturn(40L);

        List<TopicDto> result = topicService.getTopicTree();

        assertEquals(40, result.get(0).cardCount());
    }

    @Test
    void getTopicTree_childCountReflectsDirectChildren() {
        when(topicRepository.findByParentIdIsNull()).thenReturn(List.of(rootTopic));
        when(cardRepository.countByTopicOrParent(any())).thenReturn(0L);

        List<TopicDto> result = topicService.getTopicTree();

        assertEquals(1, result.get(0).childCount());
    }

    @Test
    void getTopicTree_noCardsReturnsZero() {
        when(topicRepository.findByParentIdIsNull()).thenReturn(List.of(rootTopic));
        when(cardRepository.countByTopicOrParent(rootTopic.getId())).thenReturn(0L);

        List<TopicDto> result = topicService.getTopicTree();

        assertEquals(0, result.get(0).cardCount());
    }

    @Test
    void getChildren_returnsChildTopicsWithCounts() {
        when(topicRepository.findById(rootTopic.getId())).thenReturn(Optional.of(rootTopic));
        when(topicRepository.findByParentId(rootTopic.getId())).thenReturn(List.of(childTopic));
        when(cardRepository.countByTopicOrParent(childTopic.getId())).thenReturn(5L);

        List<TopicDto> result = topicService.getChildren(rootTopic.getId());

        assertEquals(1, result.size());
        assertEquals("Collections", result.get(0).name());
        assertEquals(5, result.get(0).cardCount());
        assertEquals(TopicArea.JAVA_CORE, result.get(0).area());
    }

    @Test
    void getChildren_returnsEmptyListWhenNoChildren() {
        when(topicRepository.findById(rootTopic.getId())).thenReturn(Optional.of(rootTopic));
        when(topicRepository.findByParentId(rootTopic.getId())).thenReturn(List.of());

        List<TopicDto> result = topicService.getChildren(rootTopic.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void getChildren_throwsWhenParentNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(topicRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> topicService.getChildren(unknownId));
    }
}
