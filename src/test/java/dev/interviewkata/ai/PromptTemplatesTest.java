package dev.interviewkata.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromptTemplatesTest {

    @Test
    void explanationPrompt_containsPlaceholders() {
        String formatted = String.format(PromptTemplates.EXPLANATION_PROMPT, "HashMap", "Java Collections");
        assertTrue(formatted.contains("HashMap"));
        assertTrue(formatted.contains("Java Collections"));
        assertTrue(formatted.contains("interview coach"));
    }

    @Test
    void evaluationSystemPrompt_containsJsonStructure() {
        assertTrue(PromptTemplates.EVALUATION_SYSTEM_PROMPT.contains("score"));
        assertTrue(PromptTemplates.EVALUATION_SYSTEM_PROMPT.contains("feedback"));
        assertTrue(PromptTemplates.EVALUATION_SYSTEM_PROMPT.contains("strengths"));
        assertTrue(PromptTemplates.EVALUATION_SYSTEM_PROMPT.contains("weaknesses"));
    }

    @Test
    void evaluationUserPrompt_formatsCorrectly() {
        String formatted = String.format(PromptTemplates.EVALUATION_USER_PROMPT,
                "What is OOP?", "Object-oriented programming is...", "Cover encapsulation");
        assertTrue(formatted.contains("What is OOP?"));
        assertTrue(formatted.contains("Object-oriented programming is..."));
        assertTrue(formatted.contains("Cover encapsulation"));
    }

    @Test
    void codeReviewPrompt_containsAnalysisCriteria() {
        String formatted = String.format(PromptTemplates.CODE_REVIEW_PROMPT,
                "Sort an array", "Arrays.sort(arr);");
        assertTrue(formatted.contains("Sort an array"));
        assertTrue(formatted.contains("Arrays.sort(arr);"));
        assertTrue(formatted.contains("Time Complexity"));
        assertTrue(formatted.contains("Space Complexity"));
        assertTrue(formatted.contains("Edge Cases"));
    }

    @Test
    void quizGenerationPrompt_includesCount() {
        String formatted = String.format(PromptTemplates.QUIZ_GENERATION_PROMPT,
                5, "Java Streams", "Stream API content");
        assertTrue(formatted.contains("5"));
        assertTrue(formatted.contains("Java Streams"));
        assertTrue(formatted.contains("Stream API content"));
        assertTrue(formatted.contains("JSON array"));
    }

    @Test
    void interviewPrompt_includesAllPhases() {
        String formatted = String.format(PromptTemplates.INTERVIEW_PROMPT,
                "JAVA_CORE", "TECHNICAL", "Previous Q&A");
        assertTrue(formatted.contains("JAVA_CORE"));
        assertTrue(formatted.contains("TECHNICAL"));
        assertTrue(formatted.contains("Previous Q&A"));
        assertTrue(formatted.contains("INTRO"));
        assertTrue(formatted.contains("DEEP_DIVE"));
        assertTrue(formatted.contains("WRAP_UP"));
    }

    @Test
    void promptTemplates_cannotBeInstantiated() {
        // Verify it's a utility class with private constructor
        var constructors = PromptTemplates.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertFalse(constructors[0].canAccess(null));
    }
}
