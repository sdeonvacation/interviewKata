package dev.interviewkata.model;

import dev.interviewkata.model.enums.CardStatus;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.TopicArea;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CardTest {

    @Test
    void builder_appliesDefaults() {
        Topic topic = Topic.builder().name("T").area(TopicArea.JAVA_CORE).build();

        Card card = Card.builder()
                .topic(topic)
                .front("What is generics?")
                .back("Type parameterization")
                .difficulty(Difficulty.EASY)
                .build();

        assertThat(card.getStatus()).isEqualTo(CardStatus.NEW);
        assertThat(card.getEaseFactor()).isEqualTo(2.5);
        assertThat(card.getIntervalDays()).isZero();
        assertThat(card.getRepetitions()).isZero();
        assertThat(card.getTags()).isEmpty();
    }

    @Test
    void builder_overridesDefaults() {
        Card card = Card.builder()
                .topic(Topic.builder().name("T").area(TopicArea.DSA).build())
                .front("Q")
                .back("A")
                .difficulty(Difficulty.HARD)
                .status(CardStatus.GRADUATED)
                .easeFactor(3.0)
                .intervalDays(30)
                .repetitions(10)
                .tags(List.of("algo", "graph"))
                .build();

        assertThat(card.getStatus()).isEqualTo(CardStatus.GRADUATED);
        assertThat(card.getEaseFactor()).isEqualTo(3.0);
        assertThat(card.getIntervalDays()).isEqualTo(30);
        assertThat(card.getRepetitions()).isEqualTo(10);
        assertThat(card.getTags()).containsExactly("algo", "graph");
    }

    @Test
    void nullableFields_allowNull() {
        Card card = Card.builder()
                .topic(Topic.builder().name("T").area(TopicArea.JAVA_CORE).build())
                .front("Q")
                .back("A")
                .difficulty(Difficulty.MEDIUM)
                .build();

        assertThat(card.getCodeSnippet()).isNull();
        assertThat(card.getExplanation()).isNull();
        assertThat(card.getNextReview()).isNull();
    }
}
