package dev.interviewkata.repository;

import dev.interviewkata.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {

    List<QuizQuestion> findByGuideId(UUID guideId);

    List<QuizQuestion> findByTopicId(UUID topicId);
}
