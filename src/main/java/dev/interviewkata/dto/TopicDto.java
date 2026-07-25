package dev.interviewkata.dto;

import dev.interviewkata.model.enums.TopicArea;

import java.util.UUID;

public record TopicDto(
        UUID id,
        String name,
        TopicArea area,
        UUID parentId,
        String description,
        int sortOrder,
        int childCount,
        int cardCount
) {
}
