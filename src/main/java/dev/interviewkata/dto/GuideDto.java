package dev.interviewkata.dto;

import java.util.UUID;

public record GuideDto(
        UUID id,
        UUID topicId,
        String title,
        String contentMarkdown,
        int estimatedMinutes,
        int questionCount
) {
}
