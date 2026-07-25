package dev.interviewkata.dto;

import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ChallengeDetailDto(
        UUID id,
        String title,
        String problemStatement,
        Difficulty difficulty,
        ChallengeType challengeType,
        String starterCode,
        List<String> hints,
        int timeLimitSeconds,
        List<SubmissionResultDto> submissions
) {
}
