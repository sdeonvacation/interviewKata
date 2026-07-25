package dev.interviewkata.controller;

import dev.interviewkata.dto.DtoMapper;
import dev.interviewkata.dto.InterviewTurnDto;
import dev.interviewkata.model.InterviewTurn;
import dev.interviewkata.model.MockInterview;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.service.MockInterviewEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final MockInterviewEngine mockInterviewEngine;

    public InterviewController(MockInterviewEngine mockInterviewEngine) {
        this.mockInterviewEngine = mockInterviewEngine;
    }

    @PostMapping("/start")
    public ResponseEntity<MockInterview> startInterview(@RequestBody Map<String, String> body) {
        TopicArea topicArea = TopicArea.valueOf(body.get("topicArea"));
        Difficulty difficulty = Difficulty.valueOf(body.get("difficulty"));
        return ResponseEntity.ok(mockInterviewEngine.startInterview(topicArea, difficulty));
    }

    @PostMapping("/{id}/answer")
    public ResponseEntity<InterviewTurnDto> submitAnswer(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String answer = body.get("answer");
        InterviewTurn turn = mockInterviewEngine.submitAnswer(id, answer);
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
}
