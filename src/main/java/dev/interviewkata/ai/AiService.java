package dev.interviewkata.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Central AI facade wrapping Spring AI ChatClient for all AI interactions.
 * All methods are graceful - they return fallback strings if AI is unavailable.
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private static final String FALLBACK_PREFIX = "[AI unavailable] ";
    private static final String NOT_CONFIGURED_MSG =
            "[AI service not configured. Set INTERVIEWKATA_AI_API_KEY to enable.]";

    private final ChatClient chatClient;
    private final ChatClient fallbackChatClient;

    public AiService(ChatClient chatClient,
                     @org.springframework.beans.factory.annotation.Qualifier("fallbackChatClient")
                     @org.springframework.lang.Nullable ChatClient fallbackChatClient) {
        this.chatClient = chatClient;
        this.fallbackChatClient = fallbackChatClient;
    }

    /**
     * Generate an in-depth explanation of a concept for flashcard learning.
     */
    public String generateExplanation(String concept, String context) {
        String prompt = String.format(PromptTemplates.EXPLANATION_PROMPT, concept, context);
        return callAi(prompt, FALLBACK_PREFIX + "Explanation for: " + concept);
    }

    /**
     * Evaluate a candidate's answer against a rubric.
     * Returns JSON with score, feedback, strengths, weaknesses.
     */
    public String evaluateAnswer(String question, String answer, String rubric) {
        String userMessage = String.format(PromptTemplates.EVALUATION_USER_PROMPT, question, answer, rubric);
        return callAiWithSystem(
                PromptTemplates.EVALUATION_SYSTEM_PROMPT,
                userMessage,
                "{\"score\": 0, \"feedback\": \"" + NOT_CONFIGURED_MSG + "\", \"strengths\": [], \"weaknesses\": []}"
        );
    }

    /**
     * Review submitted code for a programming challenge.
     * Returns markdown-formatted feedback.
     */
    public String reviewCode(String code, String problemStatement) {
        String prompt = String.format(PromptTemplates.CODE_REVIEW_PROMPT, problemStatement, code);
        return callAi(prompt, FALLBACK_PREFIX + "Code review not available.");
    }

    /**
     * Generate quiz questions for a topic.
     * Returns JSON array of question objects.
     */
    public String generateQuizQuestions(String topic, String content, int count) {
        String prompt = String.format(PromptTemplates.QUIZ_GENERATION_PROMPT, count, topic, content);
        return callAi(prompt, "[]");
    }

    /**
     * Generate flashcards for a topic.
     * Returns JSON array of card objects with front, back, difficulty, tags.
     */
    public String generateCards(String topicName, String area, int count) {
        String prompt = String.format(PromptTemplates.CARD_GENERATION_PROMPT, count, topicName, area);
        return callAi(prompt, "[]");
    }

    /**
     * Generate the next interview question based on transcript and phase.
     */
    public String conductInterview(String transcript, String topic, String phase) {
        String effectiveTranscript = (transcript == null || transcript.isBlank())
                ? "(No prior conversation)"
                : transcript;
        String prompt = String.format(PromptTemplates.INTERVIEW_PROMPT, topic, phase, effectiveTranscript);
        return callAi(prompt, generateFallbackQuestion(topic, phase));
    }

    /**
     * Conduct a behavioral interview with STAR method probing.
     * Uses a specialized prompt that evaluates answers for Situation, Task, Action, Result completeness.
     */
    public String conductBehavioralInterview(String transcript, String category, String phase) {
        String effectiveTranscript = (transcript == null || transcript.isBlank())
                ? "(No prior conversation)"
                : transcript;
        String prompt = String.format(PromptTemplates.BEHAVIORAL_INTERVIEW_PROMPT,
                category, phase, effectiveTranscript);
        return callAi(prompt, generateBehavioralFallbackQuestion(category, phase));
    }

    private String callAi(String prompt, String fallback) {
        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            return response != null ? response : fallback;
        } catch (Exception e) {
            log.warn("Primary AI call failed: {}", e.getMessage());
            return tryFallback(prompt, null, fallback);
        }
    }

    private String callAiWithSystem(String systemMessage, String userMessage, String fallback) {
        try {
            String response = chatClient.prompt()
                    .system(systemMessage)
                    .user(userMessage)
                    .call()
                    .content();
            return response != null ? response : fallback;
        } catch (Exception e) {
            log.warn("Primary AI call failed: {}", e.getMessage());
            return tryFallback(userMessage, systemMessage, fallback);
        }
    }

    private String tryFallback(String userMessage, String systemMessage, String fallback) {
        if (fallbackChatClient == null) {
            return fallback;
        }
        try {
            log.info("Trying fallback AI provider...");
            var prompt = fallbackChatClient.prompt().user(userMessage);
            if (systemMessage != null) {
                prompt = prompt.system(systemMessage);
            }
            String response = prompt.call().content();
            return response != null ? response : fallback;
        } catch (Exception e2) {
            log.warn("Fallback AI also failed: {}", e2.getMessage());
            return fallback;
        }
    }

    private String generateFallbackQuestion(String topic, String phase) {
        if ("SYSTEM_DESIGN".equals(topic)) {
            return switch (phase) {
                case "INTRO" -> "Design a URL shortening service like TinyURL. It should handle 100 million new URLs per day, redirect with less than 10ms latency, and support custom short URLs. Walk me through your high-level design.";
                case "TECHNICAL" -> "How would you handle the data storage? What database would you choose and how would you partition the data?";
                case "DEEP_DIVE" -> "What happens if one of your servers goes down? How do you ensure high availability and handle failover?";
                case "WRAP_UP" -> "How would you monitor this system in production? What metrics would you track?";
                default -> "Design a notification system that can send 10 million push notifications per day with at-most-once delivery guarantee.";
            };
        }
        return switch (phase) {
            case "INTRO" -> "Let's start with a technical question. Can you explain how " + topic + " handles concurrency or scalability in real-world systems?";
            case "TECHNICAL" -> "Can you explain a key concept in " + topic + " and how you've applied it in production?";
            case "DEEP_DIVE" -> "Let's go deeper. Walk me through a complex scenario involving " + topic + " where you had to make trade-offs.";
            case "WRAP_UP" -> "What areas of " + topic + " would you like to improve, and how would you approach that?";
            default -> "Tell me more about your understanding of " + topic + ".";
        };
    }

    private String generateBehavioralFallbackQuestion(String category, String phase) {
        return switch (phase) {
            case "INTRO" -> "Welcome! Before we begin, tell me briefly about your current role and team.";
            case "QUESTION" -> "Tell me about a time when you faced a challenge related to " + category +
                    ". What was the situation?";
            case "PROBE" -> "That's interesting. Can you be more specific about what YOU personally did in that situation?";
            case "FOLLOW_UP" -> "What was the measurable outcome? How did you know your approach was successful?";
            case "WRAP_UP" -> "Thank you for sharing. Let me summarize: focus on being specific about your individual " +
                    "contributions and always quantify results when possible.";
            default -> "Tell me about a specific situation where you demonstrated " + category + ".";
        };
    }
}
