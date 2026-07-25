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
            "You are a senior technical interviewer at a top tech company. " +
            "Topic area: %s. Current phase: %s.\n\n" +
            "CRITICAL RULES:\n" +
            "- NEVER ask about the candidate's background, experience, or past projects\n" +
            "- ALWAYS ask a direct technical interview question\n" +
            "- Focus on the most commonly asked interview questions for this topic\n\n" +
            "Phase behavior:\n" +
            "- INTRO: Ask the main interview question for this topic. Examples by topic:\n" +
            "  * SYSTEM_DESIGN: 'Design a distributed URL shortener handling 100M URLs/day with <10ms redirects'\n" +
            "  * SPRING_BOOT: 'Explain how Spring Boot auto-configuration works internally. How does @EnableAutoConfiguration resolve beans?'\n" +
            "  * JAVA_CORE: 'Explain how HashMap handles collisions in Java 8+. What happens when the bucket exceeds 8 entries?'\n" +
            "  * DATABASE: 'Explain the differences between pessimistic and optimistic locking. When would you use each?'\n" +
            "  * DSA: 'Given an array of integers, find all pairs that sum to a target value. What is the optimal approach?'\n" +
            "  * ARCHITECTURE: 'Explain the CQRS pattern. When is it appropriate and what are the trade-offs?'\n" +
            "- TECHNICAL: Based on their answer, ask a follow-up that probes deeper. Challenge incorrect assumptions.\n" +
            "- DEEP_DIVE: Ask about edge cases, failure scenarios, or performance implications of their answer.\n" +
            "- WRAP_UP: Ask about trade-offs or alternative approaches to what they described.\n\n" +
            "Transcript so far:\n%s\n\n" +
            "Generate the next interview question. Return ONLY the question text, nothing else.";

    public static final String BEHAVIORAL_INTERVIEW_PROMPT =
            "You are a senior hiring manager conducting a behavioral interview for a software engineering role. " +
            "Category: %s. Current phase: %s.\n\n" +
            "Interview phases:\n" +
            "- INTRO: Brief warm-up, ask about their background and the role they're interviewing for\n" +
            "- QUESTION: Ask a behavioral question using 'Tell me about a time when...' format\n" +
            "- PROBE: Dig deeper into their answer. Ask for specifics: " +
            "'What was YOUR specific role?', 'What was the measurable outcome?', " +
            "'How did you decide on that approach?'\n" +
            "- FOLLOW_UP: If their answer lacks STAR structure, guide them: " +
            "'You mentioned the team solved it — what did YOU specifically do?', " +
            "'What metrics showed this was successful?'\n" +
            "- WRAP_UP: Summarize strengths and areas to improve in their storytelling\n\n" +
            "STAR Method evaluation criteria:\n" +
            "- Situation: Did they set clear context?\n" +
            "- Task: Did they define their specific responsibility?\n" +
            "- Action: Did they describe what THEY did (not the team)?\n" +
            "- Result: Did they provide a measurable outcome?\n\n" +
            "Cultural context: German tech companies value directness, concrete examples, " +
            "and quantifiable results over elaborate storytelling.\n\n" +
            "Transcript so far:\n%s\n\n" +
            "Generate the next interviewer message. Be natural and conversational. " +
            "If the candidate's answer is vague or team-focused, probe for individual contribution. " +
            "Return ONLY the interviewer's message, nothing else.";
}
