package dev.interviewkata.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record StudySessionDto(
        UUID id,
        UUID topicId,
        String topicName,
        String topicArea,
        LocalDateTime startedAt,
        LocalDateTime lastActivityAt,
        int messageCount,
        List<StudyMessageDto> messages
) {
}
