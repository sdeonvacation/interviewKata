package dev.interviewkata.controller;

import dev.interviewkata.dto.TopicDto;
import dev.interviewkata.model.UserProgress;
import dev.interviewkata.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping
    public ResponseEntity<List<UserProgress>> getOverallProgress() {
        return ResponseEntity.ok(progressService.getOverallProgress());
    }

    @GetMapping("/streak")
    public ResponseEntity<Map<String, Integer>> getCurrentStreak() {
        return ResponseEntity.ok(Map.of("streak", progressService.getCurrentStreak()));
    }

    @GetMapping("/weak-areas")
    public ResponseEntity<List<TopicDto>> getWeakAreas(
            @RequestParam(defaultValue = "0") double threshold) {
        return ResponseEntity.ok(progressService.getWeakAreas(threshold));
    }
}
