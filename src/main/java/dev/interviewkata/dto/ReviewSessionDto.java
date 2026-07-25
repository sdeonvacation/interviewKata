package dev.interviewkata.dto;

import java.util.List;
import java.util.UUID;

public record ReviewSessionDto(
        UUID sessionId,
        List<CardDto> cards,
        int totalCards
) {
}
