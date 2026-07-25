package dev.interviewkata.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

public record GradeRequestDto(
        UUID cardId,
        @Min(1) @Max(5) int grade
) {
}
