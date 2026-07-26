package dev.interviewkata.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BehavioralAiServiceTest {

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
    void conductBehavioralInterview_success_returnsResponse() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Tell me about a time you led a team through a crisis.");

        String result = aiService.conductBehavioralInterview("transcript", "Leadership", "QUESTION");

        assertEquals("Tell me about a time you led a team through a crisis.", result);
    }

    @Test
    void conductBehavioralInterview_usesCorrectPromptTemplate() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("response");

        aiService.conductBehavioralInterview("some transcript", "Teamwork", "PROBE");

        // Rules go in the system message; transcript goes in the user message
        ArgumentCaptor<String> systemCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).system(systemCaptor.capture());
        String system = systemCaptor.getValue();
        assertTrue(system.contains("behavioral interview"));
        assertTrue(system.contains("Teamwork"));
        assertTrue(system.contains("PROBE"));
        assertTrue(system.contains("STAR"));

        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).user(userCaptor.capture());
        assertTrue(userCaptor.getValue().contains("some transcript"));
    }

    @Test
    void conductBehavioralInterview_emptyTranscript_usesPlaceholder() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Welcome!");

        aiService.conductBehavioralInterview("", "Leadership", "INTRO");

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).user(promptCaptor.capture());
        assertTrue(promptCaptor.getValue().contains("No prior conversation"));
    }

    @Test
    void conductBehavioralInterview_nullTranscript_usesPlaceholder() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Welcome!");

        aiService.conductBehavioralInterview(null, "Adaptability", "INTRO");

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).user(promptCaptor.capture());
        assertTrue(promptCaptor.getValue().contains("No prior conversation"));
    }

    @Test
    void conductBehavioralInterview_aiThrows_returnsFallback() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("API error"));

        String result = aiService.conductBehavioralInterview("transcript", "Communication", "QUESTION");

        assertNotNull(result);
        assertFalse(result.isBlank());
        // Fallback should mention the category
        assertTrue(result.contains("Communication") || result.contains("time"));
    }

    @Test
    void conductBehavioralInterview_fallbackForIntroPhase() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("timeout"));

        String result = aiService.conductBehavioralInterview("", "Leadership", "INTRO");

        assertTrue(result.contains("current role") || result.contains("Welcome"));
    }

    @Test
    void conductBehavioralInterview_fallbackForProbePhase() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("timeout"));

        String result = aiService.conductBehavioralInterview("transcript", "Teamwork", "PROBE");

        assertTrue(result.contains("specific") || result.contains("YOU"));
    }

    @Test
    void conductBehavioralInterview_fallbackForFollowUpPhase() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("timeout"));

        String result = aiService.conductBehavioralInterview("transcript", "Conflict", "FOLLOW_UP");

        assertTrue(result.contains("measurable") || result.contains("outcome"));
    }

    @Test
    void conductBehavioralInterview_fallbackForWrapUpPhase() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("timeout"));

        String result = aiService.conductBehavioralInterview("transcript", "Adaptability", "WRAP_UP");

        assertTrue(result.contains("summarize") || result.contains("Thank"));
    }
}
