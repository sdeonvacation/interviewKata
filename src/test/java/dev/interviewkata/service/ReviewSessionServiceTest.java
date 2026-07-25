package dev.interviewkata.service;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.dto.ReviewSessionDto;
import dev.interviewkata.model.Card;
import dev.interviewkata.model.StudySession;
import dev.interviewkata.model.enums.CardStatus;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.repository.CardReviewRepository;
import dev.interviewkata.repository.DailyActivityRepository;
import dev.interviewkata.repository.StudySessionRepository;
import dev.interviewkata.scheduling.SM2Scheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewSessionServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardReviewRepository cardReviewRepository;

    @Mock
    private StudySessionRepository studySessionRepository;

    @Mock
    private DailyActivityRepository dailyActivityRepository;

    @Mock
    private SM2Scheduler sm2Scheduler;

    @Mock
    private AiService aiService;

    @InjectMocks
    private ReviewSessionService reviewSessionService;

    private Card newCard;
    private StudySession session;

    @BeforeEach
    void setUp() {
        Topic topic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Java Collections")
                .area(TopicArea.DSA)
                .build();

        newCard = Card.builder()
                .id(UUID.randomUUID())
                .topic(topic)
                .front("What is a HashMap?")
                .back("A hash table based Map implementation")
                .difficulty(Difficulty.EASY)
                .status(CardStatus.NEW)
                .nextReview(null)
                .build();

        session = StudySession.builder()
                .id(UUID.randomUUID())
                .sessionType("REVIEW")
                .itemsCompleted(0)
                .build();
    }

    @Test
    void startSession_noTopic_usesFindDueCards() {
        when(cardRepository.findDueCards(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(newCard)));
        when(studySessionRepository.save(any())).thenReturn(session);

        ReviewSessionDto result = reviewSessionService.startSession(null, 20);

        assertNotNull(result);
        assertEquals(1, result.totalCards());
        verify(cardRepository).findDueCards(any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void startSession_withTopic_usesFindDueCardsByTopicId() {
        UUID topicId = UUID.randomUUID();
        when(cardRepository.findDueCardsByTopicId(eq(topicId), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(newCard)));
        when(studySessionRepository.save(any())).thenReturn(session);

        ReviewSessionDto result = reviewSessionService.startSession(topicId, 20);

        assertNotNull(result);
        assertEquals(1, result.totalCards());
        verify(cardRepository).findDueCardsByTopicId(eq(topicId), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void startSession_includesNewCardsWithNullNextReview() {
        // New cards have nextReview=null and should be included by findDueCards query
        Topic topic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Algorithms")
                .area(TopicArea.DSA)
                .build();

        Card newCardNullReview = Card.builder()
                .id(UUID.randomUUID())
                .topic(topic)
                .front("Question")
                .back("Answer")
                .difficulty(Difficulty.MEDIUM)
                .status(CardStatus.NEW)
                .nextReview(null)
                .build();

        when(cardRepository.findDueCards(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(newCard, newCardNullReview)));
        when(studySessionRepository.save(any())).thenReturn(session);

        ReviewSessionDto result = reviewSessionService.startSession(null, 20);

        assertEquals(2, result.totalCards());
    }

    @Test
    void startSession_defaultLimit_uses20() {
        when(cardRepository.findDueCards(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(studySessionRepository.save(any())).thenReturn(session);

        reviewSessionService.startSession(null, 0);

        verify(cardRepository).findDueCards(any(LocalDateTime.class), argThat(pageable ->
                pageable.getPageSize() == 20));
    }
}
