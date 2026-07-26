package dev.interviewkata.repository;

import dev.interviewkata.model.Challenge;
import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, UUID> {

    List<Challenge> findByTopicId(UUID topicId);

    Page<Challenge> findByChallengeTypeAndDifficulty(ChallengeType type, Difficulty difficulty, Pageable pageable);

    Page<Challenge> findByChallengeType(ChallengeType type, Pageable pageable);

    Page<Challenge> findByDifficulty(Difficulty difficulty, Pageable pageable);

    List<Challenge> findByNextPracticeDateBeforeAndPracticeCountLessThan(
            LocalDateTime date, int maxCount, Pageable pageable);

    List<Challenge> findByTitle(String title);
}
