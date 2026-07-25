package dev.interviewkata.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.interviewkata.ai.AiService;
import dev.interviewkata.dto.DesignExerciseDto;
import dev.interviewkata.dto.DtoMapper;
import dev.interviewkata.model.DesignExercise;
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
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public ExerciseController(DesignExerciseService designExerciseService,
                              AiService aiService,
                              ObjectMapper objectMapper) {
        this.designExerciseService = designExerciseService;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
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
    public ResponseEntity<Map<String, String>> submitAnswer(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String answer = body.get("answer");
        DesignExercise exercise = designExerciseService.getExerciseById(id);

        String rubric = exercise.getEvaluationRubric() != null
                ? exercise.getEvaluationRubric().toString()
                : "General system design evaluation";

        String aiResponse = aiService.evaluateAnswer(exercise.getPrompt(), answer, rubric);
        String feedback = extractFeedback(aiResponse);
        return ResponseEntity.ok(Map.of("feedback", feedback));
    }

    private String extractFeedback(String aiResponse) {
        try {
            JsonNode node = objectMapper.readTree(aiResponse);
            StringBuilder sb = new StringBuilder();

            if (node.has("score")) {
                sb.append("**Score: ").append(node.get("score").asInt()).append("/10**\n\n");
            }
            if (node.has("feedback")) {
                sb.append(node.get("feedback").asText()).append("\n\n");
            }
            if (node.has("strengths") && node.get("strengths").isArray() && !node.get("strengths").isEmpty()) {
                sb.append("**Strengths:**\n");
                for (JsonNode s : node.get("strengths")) {
                    sb.append("- ").append(s.asText()).append("\n");
                }
                sb.append("\n");
            }
            if (node.has("weaknesses") && node.get("weaknesses").isArray() && !node.get("weaknesses").isEmpty()) {
                sb.append("**Areas to Improve:**\n");
                for (JsonNode w : node.get("weaknesses")) {
                    sb.append("- ").append(w.asText()).append("\n");
                }
            }
            return sb.toString().trim();
        } catch (Exception e) {
            // Not valid JSON — return as-is (already markdown text)
            return aiResponse;
        }
    }
}
