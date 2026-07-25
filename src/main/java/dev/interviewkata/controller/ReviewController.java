package dev.interviewkata.controller;

import dev.interviewkata.dto.GradeRequestDto;
import dev.interviewkata.dto.GradeResultDto;
import dev.interviewkata.dto.ReviewSessionDto;
import dev.interviewkata.dto.SessionSummaryDto;
import dev.interviewkata.service.ReviewSessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewSessionService reviewSessionService;

    public ReviewController(ReviewSessionService reviewSessionService) {
        this.reviewSessionService = reviewSessionService;
    }

    @PostMapping("/start")
    public ResponseEntity<ReviewSessionDto> startSession(@RequestBody Map<String, Object> body) {
        UUID topicId = body.containsKey("topicId") && body.get("topicId") != null
                ? UUID.fromString((String) body.get("topicId"))
                : null;
        int limit = body.containsKey("limit") ? ((Number) body.get("limit")).intValue() : 20;
        return ResponseEntity.ok(reviewSessionService.startSession(topicId, limit));
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
