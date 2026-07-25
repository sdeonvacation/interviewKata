package dev.interviewkata.dto;

import dev.interviewkata.model.enums.CardStatus;
import dev.interviewkata.model.enums.Difficulty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CardDto(
        UUID id,
        UUID topicId,
        String topicName,
        String front,
        String back,
        String codeSnippet,
        String explanation,
        Difficulty difficulty,
        List<String> tags,
        CardStatus status,
        LocalDateTime nextReview
) {
}
