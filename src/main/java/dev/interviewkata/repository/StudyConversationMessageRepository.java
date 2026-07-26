package dev.interviewkata.repository;

import dev.interviewkata.model.StudyConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudyConversationMessageRepository extends JpaRepository<StudyConversationMessage, UUID> {

    List<StudyConversationMessage> findByConversationIdOrderBySequenceAsc(UUID conversationId);

    long countByConversationId(UUID conversationId);

    void deleteByConversationId(UUID conversationId);
}
