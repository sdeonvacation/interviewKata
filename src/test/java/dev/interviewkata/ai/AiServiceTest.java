package dev.interviewkata.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private AiService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiService(chatClient, null);
    }

    @Test
    void generateExplanation_success_returnsAiResponse() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Detailed explanation of HashMap");

        String result = aiService.generateExplanation("HashMap", "Java Collections");

        assertEquals("Detailed explanation of HashMap", result);
        verify(chatClient).prompt();
    }

    @Test
    void generateExplanation_aiThrows_returnsFallback() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("API key not set"));

        String result = aiService.generateExplanation("HashMap", "Java Collections");

        assertTrue(result.contains("[AI unavailable]"));
        assertTrue(result.contains("HashMap"));
    }

    @Test
    void generateExplanation_nullResponse_returnsFallback() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(null);

        String result = aiService.generateExplanation("HashMap", "Java Collections");

        assertTrue(result.contains("[AI unavailable]"));
    }

    @Test
    void evaluateAnswer_success_returnsJson() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(
                "{\"score\": 8, \"feedback\": \"Good\", \"strengths\": [\"Clear\"], \"weaknesses\": []}");

        String result = aiService.evaluateAnswer("What is polymorphism?", "It's...", "OOP concepts");

        assertTrue(result.contains("\"score\""));
        assertTrue(result.contains("8"));
    }

    @Test
    void evaluateAnswer_aiThrows_returnsFallbackJson() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("Connection refused"));

        String result = aiService.evaluateAnswer("What is polymorphism?", "It's...", "OOP concepts");

        assertTrue(result.contains("\"score\": 0"));
        assertTrue(result.contains("AI service not configured"));
    }

    @Test
    void reviewCode_success_returnsMarkdown() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("## Code Review\n- Time: O(n)\n- Space: O(1)");

        String result = aiService.reviewCode("int[] arr = new int[10];", "Sort an array");

        assertTrue(result.contains("Code Review"));
        assertTrue(result.contains("O(n)"));
    }

    @Test
    void reviewCode_aiThrows_returnsFallback() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("Timeout"));

        String result = aiService.reviewCode("code", "problem");

        assertTrue(result.contains("[AI unavailable]"));
    }

    @Test
    void generateQuizQuestions_success_returnsJsonArray() {
        String mockResponse = "[{\"questionText\": \"What is JVM?\", \"options\": [], \"correctAnswer\": \"A\"}]";
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(mockResponse);

        String result = aiService.generateQuizQuestions("Java", "JVM internals", 5);

        assertTrue(result.contains("questionText"));
    }

    @Test
    void generateQuizQuestions_aiThrows_returnsEmptyArray() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("Error"));

        String result = aiService.generateQuizQuestions("Java", "content", 5);

        assertEquals("[]", result);
    }

    @Test
    void conductInterview_success_returnsQuestion() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Can you explain the difference between ArrayList and LinkedList?");

        String result = aiService.conductInterview("", "JAVA_CORE", "INTRO");

        assertEquals("Can you explain the difference between ArrayList and LinkedList?", result);
    }

    @Test
    void conductInterview_aiThrows_returnsFallbackQuestion() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("Error"));

        String result = aiService.conductInterview("", "JAVA_CORE", "INTRO");

        // New fallback asks direct technical question (HashMap internals for JAVA_CORE)
        assertFalse(result.isEmpty());
        assertTrue(result.length() > 30);
    }

    @Test
    void conductInterview_nullTranscript_handledGracefully() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Tell me about Spring Boot.");

        String result = aiService.conductInterview(null, "SPRING_BOOT", "TECHNICAL");

        assertEquals("Tell me about Spring Boot.", result);
    }

    @Test
    void conductInterview_deepDivePhase_returnsFallbackForPhase() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("Error"));

        String result = aiService.conductInterview("transcript", "DSA", "DEEP_DIVE");

        assertFalse(result.isEmpty());
        // New fallback asks about space complexity or edge cases
        assertTrue(result.length() > 20);
    }

    @Test
    void conductInterview_wrapUpPhase_returnsFallbackForPhase() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("Error"));

        String result = aiService.conductInterview("transcript", "SYSTEM_DESIGN", "WRAP_UP");

        assertFalse(result.isEmpty());
        assertTrue(result.length() > 20);
    }

    @Test
    void conductInterview_unknownPhase_returnsGenericFallback() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("Error"));

        String result = aiService.conductInterview("transcript", "DATABASE", "UNKNOWN");

        assertFalse(result.isEmpty());
    }
}
