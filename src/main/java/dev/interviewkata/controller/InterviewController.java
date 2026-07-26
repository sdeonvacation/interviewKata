package dev.interviewkata.controller;

import dev.interviewkata.dto.DtoMapper;
import dev.interviewkata.dto.InterviewSummaryDto;
import dev.interviewkata.dto.InterviewTurnDto;
import dev.interviewkata.dto.StartInterviewRequest;
import dev.interviewkata.dto.SubmitAnswerRequest;
import dev.interviewkata.model.InterviewTurn;
import dev.interviewkata.model.MockInterview;
import dev.interviewkata.service.MockInterviewEngine;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final MockInterviewEngine mockInterviewEngine;

    public InterviewController(MockInterviewEngine mockInterviewEngine) {
        this.mockInterviewEngine = mockInterviewEngine;
    }

    @PostMapping("/start")
    public ResponseEntity<MockInterview> startInterview(@Valid @RequestBody StartInterviewRequest request) {
        return ResponseEntity.ok(mockInterviewEngine.startInterview(request.topicArea(), request.difficulty()));
    }

    @PostMapping("/{id}/answer")
    public ResponseEntity<InterviewTurnDto> submitAnswer(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitAnswerRequest request) {
        InterviewTurn turn = mockInterviewEngine.submitAnswer(id, request.answer());
        return ResponseEntity.ok(DtoMapper.toDto(turn));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<MockInterview> endInterview(@PathVariable UUID id) {
        return ResponseEntity.ok(mockInterviewEngine.endInterview(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MockInterview> getInterview(@PathVariable UUID id) {
        return ResponseEntity.ok(mockInterviewEngine.getInterview(id));
    }

    @GetMapping("/{id}/turns")
    public ResponseEntity<List<InterviewTurnDto>> getTurns(@PathVariable UUID id) {
        return ResponseEntity.ok(mockInterviewEngine.getTurns(id));
    }

    @GetMapping
    public ResponseEntity<List<InterviewSummaryDto>> listInterviews() {
        return ResponseEntity.ok(mockInterviewEngine.listInterviews());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterview(@PathVariable UUID id) {
        mockInterviewEngine.deleteInterview(id);
        return ResponseEntity.noContent().build();
    }
}
