package dev.interviewkata.controller;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.dto.QuizResultDto;
import dev.interviewkata.model.QuizQuestion;
import dev.interviewkata.model.QuizSession;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.QuestionType;
import dev.interviewkata.repository.QuizQuestionRepository;
import dev.interviewkata.repository.TopicRepository;
import dev.interviewkata.service.QuizService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private static final Logger log = LoggerFactory.getLogger(QuizController.class);

    private final QuizService quizService;
    private final AiService aiService;
    private final TopicRepository topicRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ObjectMapper objectMapper;

    public QuizController(QuizService quizService,
                          AiService aiService,
                          TopicRepository topicRepository,
                          QuizQuestionRepository quizQuestionRepository,
                          ObjectMapper objectMapper) {
        this.quizService = quizService;
        this.aiService = aiService;
        this.topicRepository = topicRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/start")
    public ResponseEntity<QuizSession> startQuiz(@RequestBody Map<String, Object> body) {
        UUID guideId = UUID.fromString((String) body.get("guideId"));
        int count = body.containsKey("count") ? ((Number) body.get("count")).intValue() : 10;
        return ResponseEntity.ok(quizService.startQuiz(guideId, count));
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<Map<String, Boolean>> submitAnswer(
            @PathVariable UUID sessionId,
            @RequestBody Map<String, String> body) {
        UUID questionId = UUID.fromString(body.get("questionId"));
        String answer = body.get("answer");
        boolean correct = quizService.submitAnswer(sessionId, questionId, answer);
        return ResponseEntity.ok(Map.of("correct", correct));
    }

    @GetMapping("/{sessionId}/results")
    public ResponseEntity<QuizResultDto> getResults(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(quizService.getResults(sessionId));
    }

    @PostMapping("/generate")
    public ResponseEntity<List<QuizQuestion>> generateQuiz(@RequestBody Map<String, Object> body) {
        UUID topicId = UUID.fromString((String) body.get("topicId"));
        int count = body.containsKey("count") ? ((Number) body.get("count")).intValue() : 5;

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found: " + topicId));

        String topicContent = topic.getDescription() != null ? topic.getDescription() : topic.getName();
        String aiResponse = aiService.generateQuizQuestions(topic.getName(), topicContent, count);

        List<QuizQuestion> questions = parseAndPersistQuestions(aiResponse, topic);
        return ResponseEntity.ok(questions);
    }

    private List<QuizQuestion> parseAndPersistQuestions(String jsonResponse, Topic topic) {
        List<QuizQuestion> questions = new ArrayList<>();
        try {
            List<Map<String, Object>> parsed = objectMapper.readValue(
                    jsonResponse, new TypeReference<>() {});

            for (Map<String, Object> q : parsed) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> options = (List<Map<String, String>>) q.get("options");
                String difficultyStr = (String) q.getOrDefault("difficulty", "MEDIUM");

                QuizQuestion question = QuizQuestion.builder()
                        .topic(topic)
                        .questionType(QuestionType.MCQ)
                        .questionText((String) q.get("questionText"))
                        .options(options)
                        .correctAnswer((String) q.get("correctAnswer"))
                        .explanation((String) q.get("explanation"))
                        .difficulty(Difficulty.valueOf(difficultyStr))
                        .aiGenerated(true)
                        .build();

                questions.add(quizQuestionRepository.save(question));
            }
        } catch (Exception e) {
            log.warn("Failed to parse AI-generated quiz questions: {}", e.getMessage());
        }
        return questions;
    }
}
