package dev.interviewkata.repository;

import dev.interviewkata.model.Guide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuideRepository extends JpaRepository<Guide, UUID> {

    List<Guide> findByTopicIdOrderBySortOrder(UUID topicId);
}
