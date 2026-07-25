package dev.interviewkata.controller;

import dev.interviewkata.dto.ChallengeDetailDto;
import dev.interviewkata.dto.ChallengeDto;
import dev.interviewkata.dto.SubmissionResultDto;
import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.service.ChallengeService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping
    public ResponseEntity<Page<ChallengeDto>> listChallenges(
            @RequestParam(required = false) ChallengeType type,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(challengeService.listChallenges(type, difficulty, page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChallengeDetailDto> getChallengeDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(challengeService.getChallengeDetail(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<SubmissionResultDto> submitSolution(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String code = body.get("code");
        return ResponseEntity.ok(challengeService.submitSolution(id, code));
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, String>> runCode(@RequestBody Map<String, String> body) {
        // Placeholder: echo back the code (real JShell execution in Phase 4)
        String code = body.get("code");
        return ResponseEntity.ok(Map.of("output", "// Code received (" + code.length() + " chars). Execution not yet implemented."));
    }
}
