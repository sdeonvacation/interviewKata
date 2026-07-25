package dev.interviewkata.dto;

import java.time.LocalDateTime;

public record GradeResultDto(
        LocalDateTime nextReviewDate,
        int newInterval,
        long cardsRemaining
) {
}
