package dev.interviewkata.service;

import dev.interviewkata.dto.DtoMapper;
import dev.interviewkata.dto.TopicDto;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.repository.TopicRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TopicService {

    private final TopicRepository topicRepository;
    private final CardRepository cardRepository;

    public TopicService(TopicRepository topicRepository, CardRepository cardRepository) {
        this.topicRepository = topicRepository;
        this.cardRepository = cardRepository;
    }

    public List<TopicDto> getTopicTree() {
        List<Topic> roots = topicRepository.findByParentIdIsNull();
        return roots.stream()
                .map(this::mapWithCounts)
                .toList();
    }

    public TopicDto getTopicById(UUID id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found: " + id));
        return mapWithCounts(topic);
    }

    public List<TopicDto> getChildren(UUID parentId) {
        topicRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found: " + parentId));
        return topicRepository.findByParentId(parentId).stream()
                .map(this::mapWithCounts)
                .toList();
    }

    public List<TopicDto> getTopicsByArea(TopicArea area) {
        return topicRepository.findByArea(area).stream()
                .map(this::mapWithCounts)
                .toList();
    }

    @Transactional
    public TopicDto createTopic(String name, TopicArea area, UUID parentId, String description) {
        Topic topic = Topic.builder()
                .name(name)
                .area(area)
                .description(description)
                .build();

        if (parentId != null) {
            Topic parent = topicRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent topic not found: " + parentId));
            topic.setParent(parent);
        }

        Topic saved = topicRepository.save(topic);
        return mapWithCounts(saved);
    }

    private TopicDto mapWithCounts(Topic topic) {
        int childCount = topic.getChildren() != null ? topic.getChildren().size() : 0;
        long cardCount = cardRepository.countByTopicOrParent(topic.getId());
        return DtoMapper.toDto(topic, childCount, (int) cardCount);
    }
}
