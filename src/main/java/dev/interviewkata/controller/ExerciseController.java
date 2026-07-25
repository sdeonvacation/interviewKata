package dev.interviewkata.controller;

import dev.interviewkata.dto.DesignExerciseDto;
import dev.interviewkata.dto.DtoMapper;
import dev.interviewkata.model.DesignExercise;
import dev.interviewkata.model.DesignSubmission;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.service.DesignExerciseService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    private final DesignExerciseService designExerciseService;

    public ExerciseController(DesignExerciseService designExerciseService) {
        this.designExerciseService = designExerciseService;
    }

    @GetMapping
    public ResponseEntity<Page<DesignExerciseDto>> listExercises(
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(designExerciseService.listExercises(difficulty, page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesignExerciseDto> getExerciseById(@PathVariable UUID id) {
        DesignExercise exercise = designExerciseService.getExerciseById(id);
        return ResponseEntity.ok(DtoMapper.toDto(exercise));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<DesignSubmission> submitAnswer(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String answer = body.get("answer");
        return ResponseEntity.ok(designExerciseService.submitAnswer(id, answer));
    }
}
