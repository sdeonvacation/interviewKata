package dev.interviewkata.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.interviewkata.ai.AiService;
import dev.interviewkata.dto.CardDto;
import dev.interviewkata.dto.DtoMapper;
import dev.interviewkata.model.Card;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.repository.TopicRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CardService {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);
    private static final int DEFAULT_CARD_COUNT = 5;

    private final CardRepository cardRepository;
    private final TopicRepository topicRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public CardService(CardRepository cardRepository,
                       TopicRepository topicRepository,
                       AiService aiService,
                       ObjectMapper objectMapper) {
        this.cardRepository = cardRepository;
        this.topicRepository = topicRepository;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    public List<CardDto> getCardsByTopic(UUID topicId) {
        return cardRepository.findByTopicId(topicId).stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    public CardDto getCardById(UUID id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card not found: " + id));
        return DtoMapper.toDto(card);
    }

    public long getDueCardCount() {
        return cardRepository.countDueCards(LocalDateTime.now());
    }

    @Transactional
    public CardDto createCard(Card card) {
        Card saved = cardRepository.save(card);
        return DtoMapper.toDto(saved);
    }

    @Transactional
    public List<CardDto> generateCardsForTopic(UUID topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found: " + topicId));

        // Pass existing fronts as context so the AI can add fresh angles while still
        // being free to resurface the most frequently-asked interview hotspots.
        List<String> existingFronts = cardRepository.findByTopicId(topicId).stream()
                .map(Card::getFront)
                .toList();

        String aiResponse = aiService.generateCards(
                topic.getName(),
                topic.getArea().name(),
                DEFAULT_CARD_COUNT,
                existingFronts
        );

        List<Card> parsed = parseAndCreateCards(aiResponse, topic);

        // De-duplicate WITHIN this batch only (each of the N generated cards must be unique).
        // Similarity to cards from previous generations is allowed on purpose (hotspot revision).
        List<Card> unique = new ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (Card card : parsed) {
            String key = card.getFront() == null ? "" : card.getFront().trim().toLowerCase().replaceAll("\\s+", " ");
            if (seen.add(key)) {
                unique.add(card);
            }
        }

        List<Card> saved = cardRepository.saveAll(unique);
        return saved.stream().map(DtoMapper::toDto).toList();
    }

    private List<Card> parseAndCreateCards(String aiResponse, Topic topic) {
        List<Card> cards = new ArrayList<>();
        try {
            // Strip markdown code fences if present
            String json = aiResponse.strip();
            if (json.startsWith("```")) {
                json = json.replaceFirst("```(?:json)?\\s*", "");
                json = json.replaceFirst("\\s*```$", "");
            }

            List<Map<String, Object>> cardData = objectMapper.readValue(
                    json, new TypeReference<>() {});

            for (Map<String, Object> entry : cardData) {
                String front = (String) entry.get("front");
                String back = (String) entry.get("back");
                if (front == null || back == null || front.isBlank() || back.isBlank()) {
                    continue;
                }

                Difficulty difficulty = parseDifficulty((String) entry.get("difficulty"));
                List<String> tags = parseTags(entry.get("tags"));

                Card card = Card.builder()
                        .topic(topic)
                        .front(front)
                        .back(back)
                        .difficulty(difficulty)
                        .tags(tags)
                        .build();
                cards.add(card);
            }
        } catch (Exception e) {
            log.error("Failed to parse AI-generated cards for topic {}: {}", topic.getName(), e.getMessage());
        }

        if (cards.isEmpty()) {
            throw new IllegalStateException("AI failed to generate valid cards for topic: " + topic.getName());
        }
        return cards;
    }

    private Difficulty parseDifficulty(String value) {
        if (value == null) return Difficulty.MEDIUM;
        try {
            return Difficulty.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Difficulty.MEDIUM;
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> parseTags(Object tagsObj) {
        if (tagsObj instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }
}
