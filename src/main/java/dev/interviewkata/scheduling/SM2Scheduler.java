package dev.interviewkata.scheduling;

import dev.interviewkata.model.enums.CardStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * SM-2 spaced repetition algorithm implementation.
 * Pure computation - no database dependencies.
 */
@Component
public class SM2Scheduler {

    private final int graduatingIntervalDays;
    private final double minimumEaseFactor;

    public SM2Scheduler(
            @Value("${interviewkata.review.graduating-interval-days:21}") int graduatingIntervalDays,
            @Value("${interviewkata.review.minimum-ease-factor:1.3}") double minimumEaseFactor
    ) {
        this.graduatingIntervalDays = graduatingIntervalDays;
        this.minimumEaseFactor = minimumEaseFactor;
    }

    public record SM2Result(int nextInterval, double newEaseFactor, int newRepetitions, CardStatus newStatus) {
    }

    /**
     * Computes the next scheduling state for a card based on grade.
     *
     * @param grade           user grade 1-5
     * @param currentInterval current interval in days
     * @param easeFactor      current ease factor
     * @param repetitions     current repetition count
     * @return computed next state
     * @throws IllegalArgumentException if grade is not in range [1, 5]
     */
    public SM2Result computeNext(int grade, int currentInterval, double easeFactor, int repetitions) {
        if (grade < 1 || grade > 5) {
            throw new IllegalArgumentException("Grade must be between 1 and 5, got: " + grade);
        }

        int nextInterval;
        int newRepetitions;

        if (grade >= 3) {
            // Success path
            if (repetitions == 0) {
                nextInterval = 1;
            } else if (repetitions == 1) {
                nextInterval = 6;
            } else {
                nextInterval = (int) Math.round(currentInterval * easeFactor);
            }
            newRepetitions = repetitions + 1;
        } else {
            // Fail path - reset
            nextInterval = 1;
            newRepetitions = 0;
        }

        // Update ease factor: ef = ef + (0.1 - (5-grade) * (0.08 + (5-grade) * 0.02))
        double newEaseFactor = easeFactor + (0.1 - (5 - grade) * (0.08 + (5 - grade) * 0.02));
        newEaseFactor = Math.max(newEaseFactor, minimumEaseFactor);

        // Determine status
        CardStatus newStatus;
        if (nextInterval > graduatingIntervalDays) {
            newStatus = CardStatus.GRADUATED;
        } else if (nextInterval > 0) {
            newStatus = CardStatus.REVIEW;
        } else {
            newStatus = CardStatus.LEARNING;
        }

        return new SM2Result(nextInterval, newEaseFactor, newRepetitions, newStatus);
    }
}
