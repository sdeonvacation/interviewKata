package dev.interviewkata.service;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.dto.GradeResultDto;
import dev.interviewkata.model.Card;
import dev.interviewkata.model.DailyActivity;
import dev.interviewkata.model.StudySession;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.CardStatus;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.repository.CardReviewRepository;
import dev.interviewkata.repository.DailyActivityRepository;
import dev.interviewkata.repository.StudySessionRepository;
import dev.interviewkata.scheduling.SM2Scheduler;
import dev.interviewkata.scheduling.SM2Scheduler.SM2Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewSessionServiceGradeTest {

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

    private Card card;
    private UUID sessionId;
    private UUID cardId;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        cardId = UUID.randomUUID();

        Topic topic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Java Collections")
                .area(TopicArea.DSA)
                .build();

        card = Card.builder()
                .id(cardId)
                .topic(topic)
                .front("What is a HashMap?")
                .back("A hash table based Map implementation")
                .difficulty(Difficulty.EASY)
                .status(CardStatus.NEW)
                .easeFactor(2.5)
                .intervalDays(0)
                .repetitions(0)
                .build();
    }

    @Test
    void gradeCard_cardAlreadyGradedInSession_throwsIllegalState() {
        when(cardReviewRepository.existsBySessionIdAndCardId(sessionId, cardId)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> reviewSessionService.gradeCard(sessionId, cardId, 4));

        assertEquals("Card already graded in this session", ex.getMessage());
        verify(cardRepository, never()).findById(any());
    }

    @Test
    void gradeCard_cardNotFound_throwsEntityNotFound() {
        when(cardReviewRepository.existsBySessionIdAndCardId(sessionId, cardId)).thenReturn(false);
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> reviewSessionService.gradeCard(sessionId, cardId, 4));
    }

    @Test
    void gradeCard_validGrade_updatesCardAndCreatesReview() {
        when(cardReviewRepository.existsBySessionIdAndCardId(sessionId, cardId)).thenReturn(false);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        SM2Result sm2Result = new SM2Result(3, 2.6, 1, CardStatus.LEARNING);
        when(sm2Scheduler.computeNext(anyInt(), anyInt(), anyDouble(), anyInt())).thenReturn(sm2Result);
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardReviewRepository.save(any())).thenReturn(null);

        DailyActivity activity = DailyActivity.builder()
                .activityDate(LocalDate.now())
                .cardsReviewed(0)
                .build();
        when(dailyActivityRepository.findByActivityDate(any(LocalDate.class)))
                .thenReturn(Optional.of(activity));
        when(dailyActivityRepository.save(any())).thenReturn(activity);

        when(cardReviewRepository.findBySessionId(sessionId)).thenReturn(java.util.List.of());

        GradeResultDto result = reviewSessionService.gradeCard(sessionId, cardId, 4);

        assertNotNull(result);
        verify(cardRepository).save(any(Card.class));
        verify(cardReviewRepository).save(any());
    }

    @Test
    void gradeCard_duplicateGradeAttempt_preventsDoubleGrading() {
        // First grade succeeds
        when(cardReviewRepository.existsBySessionIdAndCardId(sessionId, cardId))
                .thenReturn(false)
                .thenReturn(true);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        SM2Result sm2Result = new SM2Result(3, 2.6, 1, CardStatus.LEARNING);
        when(sm2Scheduler.computeNext(anyInt(), anyInt(), anyDouble(), anyInt())).thenReturn(sm2Result);
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardReviewRepository.save(any())).thenReturn(null);

        DailyActivity activity = DailyActivity.builder()
                .activityDate(LocalDate.now())
                .cardsReviewed(0)
                .build();
        when(dailyActivityRepository.findByActivityDate(any(LocalDate.class)))
                .thenReturn(Optional.of(activity));
        when(dailyActivityRepository.save(any())).thenReturn(activity);
        when(cardReviewRepository.findBySessionId(sessionId)).thenReturn(java.util.List.of());

        // First call succeeds
        reviewSessionService.gradeCard(sessionId, cardId, 4);

        // Second call rejected
        assertThrows(IllegalStateException.class,
                () -> reviewSessionService.gradeCard(sessionId, cardId, 4));
    }
}
