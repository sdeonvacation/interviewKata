package dev.interviewkata.dto;

import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.TopicArea;
import jakarta.validation.constraints.NotNull;

public record StartInterviewRequest(
        @NotNull TopicArea topicArea,
        @NotNull Difficulty difficulty
) {
}
