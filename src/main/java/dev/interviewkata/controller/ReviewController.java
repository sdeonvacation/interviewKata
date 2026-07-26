package dev.interviewkata.controller;

import dev.interviewkata.dto.GradeRequestDto;
import dev.interviewkata.dto.GradeResultDto;
import dev.interviewkata.dto.ReviewSessionDto;
import dev.interviewkata.dto.SessionSummaryDto;
import dev.interviewkata.dto.StartReviewRequest;
import dev.interviewkata.service.ReviewSessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewSessionService reviewSessionService;

    public ReviewController(ReviewSessionService reviewSessionService) {
        this.reviewSessionService = reviewSessionService;
    }

    @PostMapping("/start")
    public ResponseEntity<ReviewSessionDto> startSession(@Valid @RequestBody StartReviewRequest request) {
        int limit = request.limit() != null ? request.limit() : 20;
        boolean includeChildren = Boolean.TRUE.equals(request.includeChildren());
        return ResponseEntity.ok(reviewSessionService.startSession(request.topicId(), limit, includeChildren));
    }

    @PostMapping("/{sessionId}/grade")
    public ResponseEntity<GradeResultDto> gradeCard(
            @PathVariable UUID sessionId,
            @Valid @RequestBody GradeRequestDto request) {
        return ResponseEntity.ok(
                reviewSessionService.gradeCard(sessionId, request.cardId(), request.grade()));
    }

    @GetMapping("/{sessionId}/summary")
    public ResponseEntity<SessionSummaryDto> getSessionSummary(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(reviewSessionService.getSessionSummary(sessionId));
    }
}
