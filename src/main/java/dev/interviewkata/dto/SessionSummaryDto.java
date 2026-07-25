package dev.interviewkata.dto;

import java.time.Duration;

public record SessionSummaryDto(
        int totalCards,
        int cardsGraded,
        double averageGrade,
        Duration duration
) {
}
