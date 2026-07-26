package dev.interviewkata.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyChatAiServiceTest {

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
    void studyChat_success_returnsAiResponse() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Let's explore HashMap! What do you know about hashing?");

        String result = aiService.studyChat(
                "User: I want to learn about HashMap Internals.",
                "HashMap Internals",
                "JAVA_CORE"
        );

        assertEquals("Let's explore HashMap! What do you know about hashing?", result);
        verify(requestSpec).system(contains("STUDYING topic: HashMap Internals (area: JAVA_CORE)"));
        verify(requestSpec).user("User: I want to learn about HashMap Internals.");
    }

    @Test
    void studyChat_nullTranscript_sendsPlaceholder() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Welcome! Let's start with the basics.");

        String result = aiService.studyChat(null, "Spring DI", "SPRING_BOOT");

        assertEquals("Welcome! Let's start with the basics.", result);
        verify(requestSpec).user("(No prior conversation)");
    }

    @Test
    void studyChat_blankTranscript_sendsPlaceholder() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Let me guide you.");

        String result = aiService.studyChat("   ", "B-Tree Index", "DATABASE");

        assertEquals("Let me guide you.", result);
        verify(requestSpec).user("(No prior conversation)");
    }

    @Test
    void studyChat_aiThrows_returnsFallback() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("Connection refused"));

        String result = aiService.studyChat("User: Hello", "CQRS", "ARCHITECTURE");

        assertEquals("I'm having trouble connecting right now. Please try again.", result);
    }

    @Test
    void studyChat_nullResponse_returnsFallback() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(null);

        String result = aiService.studyChat("User: Explain closures", "Closures", "JAVA_CORE");

        assertEquals("I'm having trouble connecting right now. Please try again.", result);
    }

    @Test
    void studyChat_systemPromptContainsStudyRules() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("OK");

        aiService.studyChat("User: test", "Sorting", "DSA");

        verify(requestSpec).system(contains("STRICT RULES"));
        verify(requestSpec).system(contains("Guide users, don't just give answers"));
        verify(requestSpec).system(contains("STUDYING topic: Sorting (area: DSA)"));
    }
}
