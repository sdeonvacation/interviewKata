package dev.interviewkata.controller;

import dev.interviewkata.dto.CardDto;
import dev.interviewkata.model.enums.CardStatus;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.service.CardService;
import dev.interviewkata.service.ChallengeService;
import dev.interviewkata.service.GuideService;
import dev.interviewkata.service.TopicService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicControllerGenerateCardsTest {

    @Mock
    private TopicService topicService;

    @Mock
    private CardService cardService;

    @Mock
    private GuideService guideService;

    @Mock
    private ChallengeService challengeService;

    private TopicController controller;

    @BeforeEach
    void setUp() {
        controller = new TopicController(topicService, cardService, guideService, challengeService);
    }

    @Test
    void generateCards_success_returnsGeneratedCards() {
        UUID topicId = UUID.randomUUID();
        List<CardDto> mockCards = List.of(
                new CardDto(UUID.randomUUID(), topicId, "Collections", "What is ArrayList?",
                        "Resizable array", null, null, Difficulty.EASY,
                        List.of("collections"), CardStatus.NEW, null),
                new CardDto(UUID.randomUUID(), topicId, "Collections", "HashMap internals?",
                        "Hash table with buckets", null, null, Difficulty.MEDIUM,
                        List.of("map"), CardStatus.NEW, null)
        );

        when(cardService.generateCardsForTopic(topicId)).thenReturn(mockCards);

        ResponseEntity<List<CardDto>> response = controller.generateCards(topicId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
        assertEquals("What is ArrayList?", response.getBody().get(0).front());
        verify(cardService).generateCardsForTopic(topicId);
    }

    @Test
    void generateCards_topicNotFound_throws() {
        UUID topicId = UUID.randomUUID();
        when(cardService.generateCardsForTopic(topicId))
                .thenThrow(new EntityNotFoundException("Topic not found: " + topicId));

        assertThrows(EntityNotFoundException.class, () -> controller.generateCards(topicId));
    }

    @Test
    void generateCards_aiFailure_throwsIllegalState() {
        UUID topicId = UUID.randomUUID();
        when(cardService.generateCardsForTopic(topicId))
                .thenThrow(new IllegalStateException("AI failed to generate valid cards"));

        assertThrows(IllegalStateException.class, () -> controller.generateCards(topicId));
    }
}
