package dev.interviewkata.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.interviewkata.ai.AiService;
import dev.interviewkata.dto.CardDto;
import dev.interviewkata.model.Card;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.CardStatus;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.repository.TopicRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceGenerateTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private AiService aiService;

    private CardService cardService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Topic testTopic;
    private UUID topicId;

    @BeforeEach
    void setUp() {
        cardService = new CardService(cardRepository, topicRepository, aiService, objectMapper);
        topicId = UUID.randomUUID();
        testTopic = Topic.builder()
                .id(topicId)
                .name("Collections")
                .area(TopicArea.JAVA_CORE)
                .build();
    }

    @Test
    void generateCardsForTopic_success_returnsCards() {
        String aiResponse = """
                [
                  {"front": "What is ArrayList?", "back": "Resizable array implementation", "difficulty": "EASY", "tags": ["collections"]},
                  {"front": "HashMap vs TreeMap?", "back": "HashMap O(1) vs TreeMap O(log n)", "difficulty": "MEDIUM", "tags": ["map", "performance"]}
                ]
                """;

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(aiService.generateCards("Collections", "JAVA_CORE", 5)).thenReturn(aiResponse);
        when(cardRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<CardDto> result = cardService.generateCardsForTopic(topicId);

        assertEquals(2, result.size());
        verify(aiService).generateCards("Collections", "JAVA_CORE", 5);
        verify(cardRepository).saveAll(anyList());
    }

    @Test
    void generateCardsForTopic_topicNotFound_throws() {
        UUID unknownId = UUID.randomUUID();
        when(topicRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cardService.generateCardsForTopic(unknownId));
        verify(aiService, never()).generateCards(any(), any(), anyInt());
    }

    @Test
    void generateCardsForTopic_aiReturnsEmptyArray_throws() {
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(aiService.generateCards(any(), any(), anyInt())).thenReturn("[]");

        assertThrows(IllegalStateException.class, () -> cardService.generateCardsForTopic(topicId));
    }

    @Test
    void generateCardsForTopic_aiReturnsInvalidJson_throws() {
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(aiService.generateCards(any(), any(), anyInt())).thenReturn("not json at all");

        assertThrows(IllegalStateException.class, () -> cardService.generateCardsForTopic(topicId));
    }

    @Test
    void generateCardsForTopic_stripsMarkdownFences() {
        String aiResponse = """
                ```json
                [{"front": "What is a Set?", "back": "Unique elements collection", "difficulty": "EASY", "tags": ["set"]}]
                ```""";

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(aiService.generateCards(any(), any(), anyInt())).thenReturn(aiResponse);
        when(cardRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<CardDto> result = cardService.generateCardsForTopic(topicId);

        assertEquals(1, result.size());
        assertEquals("What is a Set?", result.get(0).front());
    }

    @Test
    void generateCardsForTopic_skipsEntriesWithNullFront() {
        String aiResponse = """
                [
                  {"front": null, "back": "answer", "difficulty": "EASY", "tags": []},
                  {"front": "Valid question?", "back": "Valid answer", "difficulty": "HARD", "tags": ["valid"]}
                ]
                """;

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(aiService.generateCards(any(), any(), anyInt())).thenReturn(aiResponse);
        when(cardRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<CardDto> result = cardService.generateCardsForTopic(topicId);

        assertEquals(1, result.size());
        assertEquals("Valid question?", result.get(0).front());
    }

    @Test
    void generateCardsForTopic_defaultsDifficultyToMedium_whenInvalid() {
        String aiResponse = """
                [{"front": "Q?", "back": "A", "difficulty": "UNKNOWN", "tags": []}]
                """;

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(aiService.generateCards(any(), any(), anyInt())).thenReturn(aiResponse);
        when(cardRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<CardDto> result = cardService.generateCardsForTopic(topicId);

        assertEquals(Difficulty.MEDIUM, result.get(0).difficulty());
    }

    @Test
    void generateCardsForTopic_setsCorrectCardFields() {
        String aiResponse = """
                [{"front": "What is ConcurrentHashMap?", "back": "Thread-safe HashMap", "difficulty": "HARD", "tags": ["concurrency", "map"]}]
                """;

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(aiService.generateCards(any(), any(), anyInt())).thenReturn(aiResponse);
        when(cardRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Card>> captor = ArgumentCaptor.forClass(List.class);

        cardService.generateCardsForTopic(topicId);

        verify(cardRepository).saveAll(captor.capture());
        List<Card> saved = captor.getValue();
        assertEquals(1, saved.size());

        Card card = saved.get(0);
        assertEquals("What is ConcurrentHashMap?", card.getFront());
        assertEquals("Thread-safe HashMap", card.getBack());
        assertEquals(Difficulty.HARD, card.getDifficulty());
        assertEquals(List.of("concurrency", "map"), card.getTags());
        assertEquals(testTopic, card.getTopic());
        assertEquals(CardStatus.NEW, card.getStatus());
    }

    @Test
    void generateCardsForTopic_handlesNullDifficulty() {
        String aiResponse = """
                [{"front": "Q?", "back": "A", "tags": ["tag"]}]
                """;

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(aiService.generateCards(any(), any(), anyInt())).thenReturn(aiResponse);
        when(cardRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<CardDto> result = cardService.generateCardsForTopic(topicId);

        assertEquals(Difficulty.MEDIUM, result.get(0).difficulty());
    }

    @Test
    void generateCardsForTopic_handlesNullTags() {
        String aiResponse = """
                [{"front": "Q?", "back": "A", "difficulty": "EASY"}]
                """;

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(aiService.generateCards(any(), any(), anyInt())).thenReturn(aiResponse);
        when(cardRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<CardDto> result = cardService.generateCardsForTopic(topicId);

        assertNotNull(result.get(0).tags());
        assertTrue(result.get(0).tags().isEmpty());
    }
}
