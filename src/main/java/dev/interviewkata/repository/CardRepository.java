package dev.interviewkata.repository;

import dev.interviewkata.model.Card;
import dev.interviewkata.model.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findByTopicId(UUID topicId);

    Page<Card> findByNextReviewBeforeAndStatusNot(LocalDateTime date, CardStatus status, Pageable pageable);

    long countByNextReviewBeforeAndStatusNot(LocalDateTime date, CardStatus status);

    Page<Card> findByTopicIdAndNextReviewBeforeAndStatusNot(UUID topicId, LocalDateTime date, CardStatus status, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.status != 'GRADUATED' AND (c.nextReview IS NULL OR c.nextReview <= :now)")
    long countDueCards(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM Card c WHERE c.status != 'GRADUATED' AND (c.nextReview IS NULL OR c.nextReview <= :now) ORDER BY c.nextReview ASC NULLS FIRST")
    Page<Card> findDueCards(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.topic.id = :topicId AND c.status != 'GRADUATED' AND (c.nextReview IS NULL OR c.nextReview <= :now) ORDER BY c.nextReview ASC NULLS FIRST")
    Page<Card> findDueCardsByTopicId(@Param("topicId") UUID topicId, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE (c.topic.id = :topicId OR c.topic.parent.id = :topicId) AND c.status != 'GRADUATED' AND (c.nextReview IS NULL OR c.nextReview <= :now) ORDER BY c.nextReview ASC NULLS FIRST")
    Page<Card> findDueCardsByTopicOrParent(@Param("topicId") UUID topicId, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Card c WHERE c.topic.id = :topicId OR c.topic.parent.id = :topicId")
    long countByTopicOrParent(@Param("topicId") UUID topicId);
}
