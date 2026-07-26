package dev.interviewkata.repository;

import dev.interviewkata.model.StudyConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudyConversationRepository extends JpaRepository<StudyConversation, UUID> {

    List<StudyConversation> findByTopicIdOrderByLastActivityAtDesc(UUID topicId);

    List<StudyConversation> findAllByOrderByLastActivityAtDesc();
}
