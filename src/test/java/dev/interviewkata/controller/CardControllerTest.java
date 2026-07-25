package dev.interviewkata.controller;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.model.Card;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.CardStatus;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardService cardService;

    @Mock
    private AiService aiService;

    @Mock
    private CardRepository cardRepository;

    private CardController controller;

    @BeforeEach
    void setUp() {
        controller = new CardController(cardService, aiService, cardRepository);
    }

    @Test
    void generateExplanation_success_returnsExplanation() {
        UUID cardId = UUID.randomUUID();
        Topic topic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Java")
                .area(TopicArea.JAVA_CORE)
                .build();
        Card card = Card.builder()
                .id(cardId)
                .topic(topic)
                .front("What is polymorphism?")
                .back("The ability of objects to take many forms")
                .difficulty(Difficulty.MEDIUM)
                .status(CardStatus.NEW)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(aiService.generateExplanation("What is polymorphism?", "The ability of objects to take many forms"))
                .thenReturn("Polymorphism allows...");
        when(cardRepository.save(any())).thenReturn(card);

        ResponseEntity<Map<String, String>> response = controller.generateExplanation(cardId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Polymorphism allows...", response.getBody().get("explanation"));
        verify(cardRepository).save(card);
    }

    @Test
    void generateExplanation_cardNotFound_throws() {
        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> controller.generateExplanation(cardId));
        verify(aiService, never()).generateExplanation(any(), any());
    }
}
