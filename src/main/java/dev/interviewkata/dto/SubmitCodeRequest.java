package dev.interviewkata.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitCodeRequest(
        @NotBlank String code
) {
}
