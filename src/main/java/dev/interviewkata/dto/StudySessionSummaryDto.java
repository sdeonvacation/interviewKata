package dev.interviewkata.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record StudySessionSummaryDto(
        UUID id,
        UUID topicId,
        String topicName,
        String topicArea,
        LocalDateTime startedAt,
        LocalDateTime lastActivityAt,
        int messageCount,
        String preview
) {
}
