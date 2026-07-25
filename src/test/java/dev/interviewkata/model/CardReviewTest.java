package dev.interviewkata.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CardReviewTest {

    @Test
    void builder_setsAllFields() {
        Card card = Card.builder()
                .front("Q").back("A")
                .topic(Topic.builder().name("T").area(dev.interviewkata.model.enums.TopicArea.JAVA_CORE).build())
                .difficulty(dev.interviewkata.model.enums.Difficulty.EASY)
                .build();

        UUID sessionId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        CardReview review = CardReview.builder()
                .card(card)
                .sessionId(sessionId)
                .grade(4)
                .previousInterval(7)
                .newInterval(14)
                .previousEase(2.5)
                .newEase(2.6)
                .reviewedAt(now)
                .build();

        assertThat(review.getCard()).isEqualTo(card);
        assertThat(review.getSessionId()).isEqualTo(sessionId);
        assertThat(review.getGrade()).isEqualTo(4);
        assertThat(review.getPreviousInterval()).isEqualTo(7);
        assertThat(review.getNewInterval()).isEqualTo(14);
        assertThat(review.getPreviousEase()).isEqualTo(2.5);
        assertThat(review.getNewEase()).isEqualTo(2.6);
        assertThat(review.getReviewedAt()).isEqualTo(now);
    }
}
