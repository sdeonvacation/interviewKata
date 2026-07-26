package dev.interviewkata.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InterviewSummaryDto(
        UUID id,
        String topicArea,
        String difficulty,
        String state,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        int turnCount,
        Double overallScore
) {
}
