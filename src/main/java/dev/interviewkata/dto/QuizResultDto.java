package dev.interviewkata.dto;

import java.util.UUID;

public record QuizResultDto(
        UUID sessionId,
        int totalQuestions,
        int correctAnswers,
        double score
) {
}
