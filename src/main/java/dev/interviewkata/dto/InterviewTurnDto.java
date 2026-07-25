package dev.interviewkata.dto;

import dev.interviewkata.model.enums.InterviewPhase;

import java.util.Map;

public record InterviewTurnDto(
        int turnNumber,
        String aiQuestion,
        Map<String, Object> evaluation,
        InterviewPhase phase,
        boolean isComplete
) {
}
