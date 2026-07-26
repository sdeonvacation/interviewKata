package dev.interviewkata.controller;

import dev.interviewkata.ai.AiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    public record AskRequest(
            @NotBlank String question,
            @NotBlank String context
    ) {}

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@Valid @RequestBody AskRequest request) {
        String answer = aiService.answerQuestion(request.question(), request.context());
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}
