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
     * Answer a free-form question given a context (card content, challenge description, etc.)
     */
    public String answerQuestion(String question, String context) {
        String userMessage = "Context:\n" + context + "\n\nQuestion: " + question;
        return callAiWithSystem(PromptTemplates.ANSWER_QUESTION_SYSTEM_PROMPT, userMessage,
                FALLBACK_PREFIX + "Unable to answer right now.");
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
        return generateCards(topicName, area, count, java.util.List.of());
    }

    /**
     * Generate fresh cards, instructing the model to avoid repeating existing questions.
     */
    public String generateCards(String topicName, String area, int count, java.util.List<String> existingFronts) {
        String avoidBlock = "";
        if (existingFronts != null && !existingFronts.isEmpty()) {
            String list = existingFronts.stream()
                    .limit(60)
                    .map(f -> "- " + f)
                    .collect(java.util.stream.Collectors.joining("\n"));
            avoidBlock = "For reference, these questions already exist for this topic:\n" + list + "\n\n"
                    + "Prefer introducing fresh questions and new angles, BUT it is fine to revisit the "
                    + "most critical, frequently-asked interview hotspots again (repetition of high-yield "
                    + "questions aids retention). Avoid verbatim duplicates of trivial cards.\n\n";
        }
        String prompt = String.format(PromptTemplates.CARD_GENERATION_PROMPT, count, topicName, area, avoidBlock);
        return callAi(prompt, "[]");
    }

    /**
     * Generate the next interview question based on transcript and phase.
     * Rules go in the system role; the transcript goes in the user role so the model
     * cannot confuse candidate input with interviewer instructions.
     */
    public String conductInterview(String transcript, String topic, String phase) {
        String systemPrompt = String.format(PromptTemplates.INTERVIEW_PROMPT, topic, phase);
        String userMessage = buildTranscriptMessage(transcript);
        return callAiWithSystem(systemPrompt, userMessage, generateFallbackQuestion(topic, phase));
    }

    /**
     * Conduct a study chat session — conversational AI tutoring on a topic.
     * Full transcript is sent each turn to maintain context.
     */
    public String studyChat(String transcript, String topicName, String topicArea) {
        String systemPrompt = String.format(PromptTemplates.STUDY_SYSTEM_PROMPT, topicName, topicArea);
        String effectiveTranscript = (transcript == null || transcript.isBlank())
                ? "(No prior conversation)"
                : transcript;
        return callAiWithSystem(systemPrompt, effectiveTranscript,
                "I'm having trouble connecting right now. Please try again.");
    }

    /**
     * Conduct a behavioral interview with STAR method probing.
     * Uses a specialized prompt that evaluates answers for Situation, Task, Action, Result completeness.
     */
    public String conductBehavioralInterview(String transcript, String category, String phase) {
        String systemPrompt = String.format(PromptTemplates.BEHAVIORAL_INTERVIEW_PROMPT, category, phase);
        String userMessage = buildTranscriptMessage(transcript);
        return callAiWithSystem(systemPrompt, userMessage, generateBehavioralFallbackQuestion(category, phase));
    }

    private String buildTranscriptMessage(String transcript) {
        String effective = (transcript == null || transcript.isBlank())
                ? "(No prior conversation — this is the first question.)"
                : transcript;
        return "Interview transcript so far:\n" + effective + "\n\nGenerate your response.";
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
        return switch (topic) {
            case "SYSTEM_DESIGN" -> switch (phase) {
                case "INTRO" -> "Design a URL shortening service like TinyURL. It should handle 100 million new URLs per day, redirect with less than 10ms latency, and support custom short URLs. Walk me through your high-level design.";
                case "TECHNICAL" -> "How would you handle the data storage? What database would you choose and how would you partition the data?";
                case "DEEP_DIVE" -> "What happens if one of your servers goes down? How do you ensure high availability and handle failover?";
                default -> "How would you monitor this system in production? What metrics would you track?";
            };
            case "SPRING_BOOT" -> switch (phase) {
                case "INTRO" -> "Explain how Spring Boot auto-configuration works. How does @EnableAutoConfiguration decide which beans to create, and how can you override or exclude specific auto-configurations?";
                case "TECHNICAL" -> "What is the difference between @Component, @Service, @Repository, and @Controller? When and why would you use @Configuration with @Bean methods instead?";
                case "DEEP_DIVE" -> "Explain how Spring handles transaction propagation. What happens when a @Transactional method calls another @Transactional method with REQUIRES_NEW?";
                default -> "How would you implement custom health indicators and metrics in a Spring Boot production application?";
            };
            case "JAVA_CORE" -> switch (phase) {
                case "INTRO" -> "Explain how HashMap works internally in Java 8+. What happens during a put() operation when there's a hash collision, and when does the linked list convert to a tree?";
                case "TECHNICAL" -> "What is the difference between synchronized, ReentrantLock, and volatile? Give a scenario where each is the best choice.";
                case "DEEP_DIVE" -> "Explain the Java Memory Model. What guarantees does 'happens-before' provide, and how does it relate to volatile and synchronized?";
                default -> "What are the trade-offs between CompletableFuture and virtual threads (Project Loom) for handling concurrent I/O?";
            };
            case "DATABASE" -> switch (phase) {
                case "INTRO" -> "Explain the differences between the four SQL isolation levels. What anomalies does each prevent, and what is the performance trade-off at each level?";
                case "TECHNICAL" -> "How do B-tree indexes work? When would you choose a composite index over multiple single-column indexes, and why does column order matter?";
                case "DEEP_DIVE" -> "You have a query that's doing a sequential scan on a table with 100M rows despite having an index. What are the possible reasons and how would you diagnose it?";
                default -> "Compare PostgreSQL MVCC with MySQL InnoDB locking. What are the trade-offs for write-heavy workloads?";
            };
            case "DSA" -> switch (phase) {
                case "INTRO" -> "Given an unsorted array of integers, find the length of the longest consecutive sequence. For example, given [100, 4, 200, 1, 3, 2], the answer is 4 (the sequence 1,2,3,4). What is the optimal time complexity you can achieve?";
                case "TECHNICAL" -> "Walk me through your approach step by step. What data structure would you use and why?";
                case "DEEP_DIVE" -> "What is the space complexity of your solution? Can you solve it with O(1) extra space?";
                default -> "If the input was a stream of numbers instead of a fixed array, how would your approach change?";
            };
            case "ARCHITECTURE" -> switch (phase) {
                case "INTRO" -> "Explain the CQRS pattern with Event Sourcing. When is this architecture appropriate, what problems does it solve, and what complexity does it introduce?";
                case "TECHNICAL" -> "How would you handle eventual consistency between the read and write models? What strategies exist and what are their trade-offs?";
                case "DEEP_DIVE" -> "How would you implement a saga pattern across three microservices for an order-payment-shipping flow? How do you handle partial failures?";
                default -> "Compare choreography vs orchestration for microservice coordination. When would you choose each?";
            };
            default -> switch (phase) {
                case "INTRO" -> "Explain a core concept in " + topic + " that is frequently asked in senior engineering interviews.";
                case "TECHNICAL" -> "Can you walk me through a production scenario where this concept caused a real problem?";
                case "DEEP_DIVE" -> "What are the trade-offs of the approach you described? What alternative would you consider?";
                default -> "How would you explain this to a junior engineer joining your team?";
            };
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
