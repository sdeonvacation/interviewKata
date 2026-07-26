package dev.interviewkata.controller;

import dev.interviewkata.dto.StudyMessageDto;
import dev.interviewkata.dto.StudySessionDto;
import dev.interviewkata.dto.StudySessionSummaryDto;
import dev.interviewkata.service.StudySessionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/study")
public class StudyController {

    private final StudySessionService studySessionService;

    public StudyController(StudySessionService studySessionService) {
        this.studySessionService = studySessionService;
    }

    public record CreateSessionRequest(@NotNull UUID topicId) {}

    public record SendMessageRequest(@NotBlank String message) {}

    @PostMapping("/sessions")
    public ResponseEntity<StudySessionDto> createSession(@Valid @RequestBody CreateSessionRequest request) {
        return ResponseEntity.ok(studySessionService.createSession(request.topicId()));
    }

    @PostMapping("/sessions/{id}/messages")
    public ResponseEntity<StudyMessageDto> sendMessage(@PathVariable UUID id,
                                                       @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(studySessionService.sendMessage(id, request.message()));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<StudySessionSummaryDto>> listSessions() {
        return ResponseEntity.ok(studySessionService.listSessions());
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<StudySessionDto> getSession(@PathVariable UUID id) {
        return ResponseEntity.ok(studySessionService.getSession(id));
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable UUID id) {
        studySessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
