package dev.interviewkata.dto;

public record StudyMessageDto(
        String role,
        String content,
        int sequence
) {
}
