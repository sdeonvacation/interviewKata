package dev.interviewkata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.interviewkata.ai.AiService;
import dev.interviewkata.model.QuizQuestion;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.QuestionType;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.QuizQuestionRepository;
import dev.interviewkata.repository.TopicRepository;
import dev.interviewkata.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizControllerTest {

    @Mock
    private QuizService quizService;

    @Mock
    private AiService aiService;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    private QuizController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        controller = new QuizController(quizService, aiService, topicRepository, quizQuestionRepository, objectMapper);
    }

    @Test
    void generateQuiz_success_parsesAndPersistsQuestions() {
        UUID topicId = UUID.randomUUID();
        Topic topic = Topic.builder()
                .id(topicId)
                .name("Java Streams")
                .area(TopicArea.JAVA_CORE)
                .description("Stream API for functional programming")
                .build();

        String aiResponse = "[{\"questionText\": \"What does map() do?\", " +
                "\"options\": [{\"key\": \"A\", \"value\": \"Transforms elements\"}, " +
                "{\"key\": \"B\", \"value\": \"Filters elements\"}, " +
                "{\"key\": \"C\", \"value\": \"Collects elements\"}, " +
                "{\"key\": \"D\", \"value\": \"Sorts elements\"}], " +
                "\"correctAnswer\": \"A\", \"explanation\": \"map transforms each element\", " +
                "\"difficulty\": \"EASY\"}]";

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(aiService.generateQuizQuestions("Java Streams", "Stream API for functional programming", 5))
                .thenReturn(aiResponse);
        when(quizQuestionRepository.save(any())).thenAnswer(inv -> {
            QuizQuestion q = inv.getArgument(0);
            q.setId(UUID.randomUUID());
            return q;
        });

        Map<String, Object> body = Map.of("topicId", topicId.toString(), "count", 5);
        ResponseEntity<List<QuizQuestion>> response = controller.generateQuiz(body);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        QuizQuestion saved = response.getBody().get(0);
        assertEquals("What does map() do?", saved.getQuestionText());
        assertEquals("A", saved.getCorrectAnswer());
        assertEquals(QuestionType.MCQ, saved.getQuestionType());
        assertTrue(saved.isAiGenerated());
        assertEquals(Difficulty.EASY, saved.getDifficulty());
    }

    @Test
    void generateQuiz_topicNotFound_throws() {
        UUID topicId = UUID.randomUUID();
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

        Map<String, Object> body = Map.of("topicId", topicId.toString(), "count", 5);

        assertThrows(Exception.class, () -> controller.generateQuiz(body));
        verify(aiService, never()).generateQuizQuestions(any(), any(), anyInt());
    }

    @Test
    void generateQuiz_aiReturnsInvalidJson_returnsEmptyList() {
        UUID topicId = UUID.randomUUID();
        Topic topic = Topic.builder()
                .id(topicId)
                .name("DSA")
                .area(TopicArea.DSA)
                .build();

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(aiService.generateQuizQuestions(any(), any(), anyInt())).thenReturn("not valid json");

        Map<String, Object> body = Map.of("topicId", topicId.toString(), "count", 3);
        ResponseEntity<List<QuizQuestion>> response = controller.generateQuiz(body);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void generateQuiz_defaultCount_uses5() {
        UUID topicId = UUID.randomUUID();
        Topic topic = Topic.builder()
                .id(topicId)
                .name("Spring Boot")
                .area(TopicArea.SPRING_BOOT)
                .description("Spring Boot framework")
                .build();

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(aiService.generateQuizQuestions("Spring Boot", "Spring Boot framework", 5))
                .thenReturn("[]");

        Map<String, Object> body = Map.of("topicId", topicId.toString());
        controller.generateQuiz(body);

        verify(aiService).generateQuizQuestions("Spring Boot", "Spring Boot framework", 5);
    }

    @Test
    void generateQuiz_topicWithNullDescription_usesName() {
        UUID topicId = UUID.randomUUID();
        Topic topic = Topic.builder()
                .id(topicId)
                .name("Algorithms")
                .area(TopicArea.DSA)
                .build();

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(aiService.generateQuizQuestions("Algorithms", "Algorithms", 5))
                .thenReturn("[]");

        Map<String, Object> body = Map.of("topicId", topicId.toString(), "count", 5);
        controller.generateQuiz(body);

        verify(aiService).generateQuizQuestions("Algorithms", "Algorithms", 5);
    }
}
