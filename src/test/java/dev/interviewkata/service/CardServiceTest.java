package dev.interviewkata.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.interviewkata.ai.AiService;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.repository.TopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private AiService aiService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CardService cardService;

    @Test
    void getDueCardCount_usesCountDueCardsQuery() {
        when(cardRepository.countDueCards(any(LocalDateTime.class))).thenReturn(40L);

        long count = cardService.getDueCardCount();

        assertEquals(40L, count);
        verify(cardRepository).countDueCards(any(LocalDateTime.class));
    }

    @Test
    void getDueCardCount_passesCurrentTime() {
        when(cardRepository.countDueCards(any(LocalDateTime.class))).thenReturn(0L);
        LocalDateTime before = LocalDateTime.now();

        cardService.getDueCardCount();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(cardRepository).countDueCards(captor.capture());
        LocalDateTime passed = captor.getValue();
        LocalDateTime after = LocalDateTime.now();

        assertFalse(passed.isBefore(before));
        assertFalse(passed.isAfter(after));
    }

    @Test
    void getDueCardCount_zeroWhenNoDueCards() {
        when(cardRepository.countDueCards(any(LocalDateTime.class))).thenReturn(0L);

        long count = cardService.getDueCardCount();

        assertEquals(0L, count);
    }
}
