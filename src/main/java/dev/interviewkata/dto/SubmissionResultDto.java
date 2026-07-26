package dev.interviewkata.dto;

import dev.interviewkata.model.enums.SubmissionStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SubmissionResultDto(
        UUID id,
        SubmissionStatus status,
        List<Map<String, Object>> testResults,
        String aiReview,
        Integer executionTimeMs,
        String code
) {
}
