package dev.interviewkata.controller;

import dev.interviewkata.dto.ChallengeDetailDto;
import dev.interviewkata.dto.ChallengeDto;
import dev.interviewkata.dto.SubmissionResultDto;
import dev.interviewkata.dto.SubmitCodeRequest;
import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.service.ChallengeService;
import jakarta.validation.Valid;
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
            @Valid @RequestBody SubmitCodeRequest request) {
        return ResponseEntity.ok(challengeService.submitSolution(id, request.code(), true));
    }

    @PostMapping("/{id}/run-tests")
    public ResponseEntity<SubmissionResultDto> runTests(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitCodeRequest request) {
        return ResponseEntity.ok(challengeService.submitSolution(id, request.code(), false));
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, String>> runCode(@Valid @RequestBody SubmitCodeRequest request) {
        return ResponseEntity.ok(Map.of("output", "// Code received (" + request.code().length() + " chars). Execution not yet implemented."));
    }
}
