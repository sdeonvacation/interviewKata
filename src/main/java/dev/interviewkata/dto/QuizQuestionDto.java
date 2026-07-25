package dev.interviewkata.dto;

import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.QuestionType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record QuizQuestionDto(
        UUID id,
        QuestionType questionType,
        String questionText,
        List<Map<String, String>> options,
        Difficulty difficulty
) {
}
