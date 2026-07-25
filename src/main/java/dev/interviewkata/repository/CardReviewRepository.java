package dev.interviewkata.repository;

import dev.interviewkata.model.CardReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardReviewRepository extends JpaRepository<CardReview, UUID> {

    List<CardReview> findByCardIdOrderByReviewedAtDesc(UUID cardId);

    List<CardReview> findBySessionId(UUID sessionId);

    boolean existsBySessionIdAndCardId(UUID sessionId, UUID cardId);
}
