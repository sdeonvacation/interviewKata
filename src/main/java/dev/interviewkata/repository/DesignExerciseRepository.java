package dev.interviewkata.repository;

import dev.interviewkata.model.DesignExercise;
import dev.interviewkata.model.enums.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DesignExerciseRepository extends JpaRepository<DesignExercise, UUID> {

    Page<DesignExercise> findByDifficulty(Difficulty difficulty, Pageable pageable);

    List<DesignExercise> findByTopicId(UUID topicId);
}
