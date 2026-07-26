package dev.interviewkata.service;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.dto.CardDto;
import dev.interviewkata.dto.DtoMapper;
import dev.interviewkata.dto.GradeResultDto;
import dev.interviewkata.dto.ReviewSessionDto;
import dev.interviewkata.dto.SessionSummaryDto;
import dev.interviewkata.model.Card;
import dev.interviewkata.model.CardReview;
import dev.interviewkata.model.DailyActivity;
import dev.interviewkata.model.StudySession;
import dev.interviewkata.model.enums.CardStatus;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.repository.CardReviewRepository;
import dev.interviewkata.repository.DailyActivityRepository;
import dev.interviewkata.repository.StudySessionRepository;
import dev.interviewkata.scheduling.SM2Scheduler;
import dev.interviewkata.scheduling.SM2Scheduler.SM2Result;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReviewSessionService {

    private static final Logger log = LoggerFactory.getLogger(ReviewSessionService.class);

    private final CardRepository cardRepository;
    private final CardReviewRepository cardReviewRepository;
    private final StudySessionRepository studySessionRepository;
    private final DailyActivityRepository dailyActivityRepository;
    private final SM2Scheduler sm2Scheduler;
    private final AiService aiService;

    public ReviewSessionService(CardRepository cardRepository,
                                CardReviewRepository cardReviewRepository,
                                StudySessionRepository studySessionRepository,
                                DailyActivityRepository dailyActivityRepository,
                                SM2Scheduler sm2Scheduler,
                                AiService aiService) {
        this.cardRepository = cardRepository;
        this.cardReviewRepository = cardReviewRepository;
        this.studySessionRepository = studySessionRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.sm2Scheduler = sm2Scheduler;
        this.aiService = aiService;
    }

    @Transactional
    public ReviewSessionDto startSession(UUID topicId, int limit) {
        return startSession(topicId, limit, false);
    }

    @Transactional
    public ReviewSessionDto startSession(UUID topicId, int limit, boolean includeChildren) {
        int sessionSize = limit > 0 ? limit : 20;
        List<Card> dueCards;

        if (topicId != null) {
            Page<Card> page = includeChildren
                    ? cardRepository.findDueCardsByTopicOrParent(topicId, LocalDateTime.now(), PageRequest.of(0, sessionSize))
                    : cardRepository.findDueCardsByTopicId(topicId, LocalDateTime.now(), PageRequest.of(0, sessionSize));
            dueCards = page.getContent();
        } else {
            dueCards = cardRepository.findDueCards(
                    LocalDateTime.now(),
                    PageRequest.of(0, sessionSize)).getContent();
        }

        StudySession session = StudySession.builder()
                .sessionType("REVIEW")
                .itemsCompleted(0)
                .build();
        StudySession saved = studySessionRepository.save(session);

        List<CardDto> cardDtos = dueCards.stream()
                .map(DtoMapper::toDto)
                .toList();

        return new ReviewSessionDto(saved.getId(), cardDtos, cardDtos.size());
    }

    @Transactional
    public GradeResultDto gradeCard(UUID sessionId, UUID cardId, int grade) {
        // Verify card hasn't already been graded in this session
        if (cardReviewRepository.existsBySessionIdAndCardId(sessionId, cardId)) {
            throw new IllegalStateException("Card already graded in this session");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found: " + cardId));

        SM2Result result = sm2Scheduler.computeNext(
                grade, card.getIntervalDays(), card.getEaseFactor(), card.getRepetitions());

        // Store previous values for review record
        int previousInterval = card.getIntervalDays();
        double previousEase = card.getEaseFactor();

        // Update card
        card.setEaseFactor(result.newEaseFactor());
        card.setIntervalDays(result.nextInterval());
        card.setRepetitions(result.newRepetitions());
        card.setStatus(result.newStatus());
        card.setNextReview(LocalDateTime.now().plusDays(result.nextInterval()));
        cardRepository.save(card);

        // Create review record
        CardReview review = CardReview.builder()
                .card(card)
                .sessionId(sessionId)
                .grade(grade)
                .previousInterval(previousInterval)
                .newInterval(result.nextInterval())
                .previousEase(previousEase)
                .newEase(result.newEaseFactor())
                .build();
        cardReviewRepository.save(review);

        // Update daily activity
        updateDailyCardsReviewed();

        // Generate AI explanation for poorly-graded cards (non-blocking)
        if (grade <= 2) {
            try {
                String explanation = aiService.generateExplanation(card.getFront(), card.getBack());
                card.setExplanation(explanation);
                cardRepository.save(card);
            } catch (Exception e) {
                log.warn("Failed to generate AI explanation for card {}: {}", cardId, e.getMessage());
            }
        }

        // Count remaining cards in session
        long remaining = cardReviewRepository.findBySessionId(sessionId).size();
        long totalInSession = remaining; // approximation - graded so far

        return new GradeResultDto(card.getNextReview(), result.nextInterval(), 0);
    }

    public SessionSummaryDto getSessionSummary(UUID sessionId) {
        List<CardReview> reviews = cardReviewRepository.findBySessionId(sessionId);
        if (reviews.isEmpty()) {
            return new SessionSummaryDto(0, 0, 0.0, Duration.ZERO);
        }

        double averageGrade = reviews.stream()
                .mapToInt(CardReview::getGrade)
                .average()
                .orElse(0.0);

        LocalDateTime first = reviews.stream()
                .map(CardReview::getReviewedAt)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        LocalDateTime last = reviews.stream()
                .map(CardReview::getReviewedAt)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        Duration duration = Duration.between(first, last);

        return new SessionSummaryDto(reviews.size(), reviews.size(), averageGrade, duration);
    }

    private void updateDailyCardsReviewed() {
        LocalDate today = LocalDate.now();
        DailyActivity activity = dailyActivityRepository.findByActivityDate(today)
                .orElseGet(() -> {
                    DailyActivity newActivity = DailyActivity.builder()
                            .activityDate(today)
                            .build();
                    return dailyActivityRepository.save(newActivity);
                });
        activity.setCardsReviewed(activity.getCardsReviewed() + 1);
        dailyActivityRepository.save(activity);
    }
}
