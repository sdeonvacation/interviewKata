package dev.interviewkata.repository;

import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.TopicArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {

    List<Topic> findByParentIdIsNull();

    List<Topic> findByParentId(UUID parentId);

    List<Topic> findByArea(TopicArea area);
}
