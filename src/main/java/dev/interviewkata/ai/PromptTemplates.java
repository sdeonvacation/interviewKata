package dev.interviewkata.ai;

/**
 * Prompt templates for AI service interactions.
 */
public final class PromptTemplates {

    private PromptTemplates() {
    }

    public static final String EXPLANATION_PROMPT =
            "You are an expert interview coach specializing in software engineering topics. " +
            "Explain the following concept in depth.\n\n" +
            "Concept: %s\n\n" +
            "Context: %s\n\n" +
            "Be concise but thorough. Use code examples where helpful. " +
            "Structure your explanation with clear sections.";

    public static final String EVALUATION_SYSTEM_PROMPT =
            "You are a technical interview evaluator. Assess the candidate's answer objectively. " +
            "Return your evaluation as JSON with the following structure:\n" +
            "{\"score\": <1-10>, \"feedback\": \"<overall feedback>\", " +
            "\"strengths\": [\"<strength1>\", ...], \"weaknesses\": [\"<weakness1>\", ...]}";

    public static final String EVALUATION_USER_PROMPT =
            "Question: %s\n\n" +
            "Candidate's Answer: %s\n\n" +
            "Evaluation Rubric: %s\n\n" +
            "Evaluate this answer and return JSON.";

    public static final String CODE_REVIEW_PROMPT =
            "You are a senior software engineer conducting a code review. " +
            "Review the following code solution for a programming challenge.\n\n" +
            "Problem Statement:\n%s\n\n" +
            "Submitted Code:\n```\n%s\n```\n\n" +
            "Analyze:\n" +
            "1. **Correctness** - Does it solve the problem?\n" +
            "2. **Time Complexity** - Big-O analysis\n" +
            "3. **Space Complexity** - Memory usage\n" +
            "4. **Code Style** - Readability, naming, structure\n" +
            "5. **Edge Cases** - Are they handled?\n" +
            "6. **Improvements** - Suggestions for optimization\n\n" +
            "Provide feedback in markdown format.";

    public static final String QUIZ_GENERATION_PROMPT =
            "You are a quiz generator for software engineering interview preparation. " +
            "Generate exactly %d multiple-choice questions about the following topic.\n\n" +
            "Topic: %s\n\n" +
            "Content to base questions on:\n%s\n\n" +
            "Return a JSON array where each element has:\n" +
            "{\"questionText\": \"...\", \"options\": [{\"key\": \"A\", \"value\": \"...\"}, " +
            "{\"key\": \"B\", \"value\": \"...\"}, {\"key\": \"C\", \"value\": \"...\"}, " +
            "{\"key\": \"D\", \"value\": \"...\"}], \"correctAnswer\": \"A|B|C|D\", " +
            "\"explanation\": \"...\", \"difficulty\": \"EASY|MEDIUM|HARD\"}\n\n" +
            "Return ONLY the JSON array, no other text.";

    public static final String CARD_GENERATION_PROMPT =
            "You are a flashcard generator for software engineering interview preparation. " +
            "Generate exactly %d flashcards for the following topic.\n\n" +
            "Topic: %s\n" +
            "Area: %s\n\n" +
            "Each flashcard should test a key concept, definition, or practical knowledge point. " +
            "Return a JSON array where each element has:\n" +
            "{\"front\": \"<question or concept to recall>\", \"back\": \"<concise answer>\", " +
            "\"difficulty\": \"EASY|MEDIUM|HARD\", \"tags\": [\"<relevant-tag>\"]}\n\n" +
            "Guidelines:\n" +
            "- Mix difficulties: 2 EASY, 2 MEDIUM, 1 HARD\n" +
            "- Front should be a clear question or prompt\n" +
            "- Back should be a concise but complete answer\n" +
            "- Tags should be 1-3 relevant keywords\n" +
            "- Focus on interview-relevant knowledge\n\n" +
            "Return ONLY the JSON array, no other text.";

    public static final String INTERVIEW_PROMPT =
            "You are a senior technical interviewer conducting a mock interview. " +
            "Topic area: %s. Current phase: %s.\n\n" +
            "Interview phases:\n" +
            "- INTRO: Warm-up questions about experience and general knowledge\n" +
            "- TECHNICAL: Specific technical questions requiring detailed answers\n" +
            "- DEEP_DIVE: Follow-up questions probing deeper understanding\n" +
            "- WRAP_UP: Final questions and summary\n\n" +
            "Transcript so far:\n%s\n\n" +
            "Generate the next interview question. Be natural and conversational. " +
            "If the transcript shows weak areas, probe deeper. " +
            "Return ONLY the question text, nothing else.";
}
