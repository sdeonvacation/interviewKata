package dev.interviewkata.dto;

import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;

import java.util.UUID;

public record ChallengeDto(
        UUID id,
        UUID topicId,
        String title,
        Difficulty difficulty,
        ChallengeType challengeType,
        boolean solved
) {
}
