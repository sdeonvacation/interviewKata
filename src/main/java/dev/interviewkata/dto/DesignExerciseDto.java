package dev.interviewkata.dto;

import dev.interviewkata.model.enums.Difficulty;

import java.util.List;
import java.util.UUID;

public record DesignExerciseDto(
        UUID id,
        UUID topicId,
        String title,
        Difficulty difficulty,
        int estimatedMinutes,
        String prompt,
        List<String> evaluationCriteria
) {
}
