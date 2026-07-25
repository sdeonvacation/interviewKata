package dev.interviewkata.dto;

import java.util.List;

public record DailyRecommendationDto(
        List<CardDto> reviewCards,
        List<ChallengeDto> dsaChallenges,
        DesignExerciseDto designExercise,
        String motivationalMessage,
        List<ChallengeDto> revisionChallenges,
        boolean behavioralPracticeRecommended
) {
}
