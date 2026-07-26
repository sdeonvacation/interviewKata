package dev.interviewkata.dto;

import java.util.UUID;

public record StartReviewRequest(
        UUID topicId,
        Integer limit,
        Boolean includeChildren
) {
}
