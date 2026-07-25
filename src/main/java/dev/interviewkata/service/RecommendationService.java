package dev.interviewkata.service;

import dev.interviewkata.dto.*;
import dev.interviewkata.model.Card;
import dev.interviewkata.model.Challenge;
import dev.interviewkata.model.DesignExercise;
import dev.interviewkata.model.Submission;
import dev.interviewkata.model.enums.CardStatus;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.SubmissionStatus;
import dev.interviewkata.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Systematic daily recommendation engine.
 * 
 * Philosophy: Progression + Revision
 * - New content progresses EASY → MEDIUM → HARD
 * - Previously learned content is revisited via spaced repetition
 * - Each day includes: knowledge review + new DSA + system design
 */
@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int MAX_REVIEW_CARDS = 10;
    private static final int REVISION_CARD_SLOTS = 3; // 3 review cards for previously learned
    private static final int NEW_CARD_SLOTS = 7;      // 7 new/due cards
    private static final int DSA_CHALLENGE_COUNT = 2;

    private final CardRepository cardRepository;
    private final ChallengeRepository challengeRepository;
    private final SubmissionRepository submissionRepository;
    private final DesignExerciseRepository designExerciseRepository;
    private final DesignSubmissionRepository designSubmissionRepository;
    private final ProgressService progressService;

    public RecommendationService(CardRepository cardRepository,
                                 ChallengeRepository challengeRepository,
                                 SubmissionRepository submissionRepository,
                                 DesignExerciseRepository designExerciseRepository,
                                 DesignSubmissionRepository designSubmissionRepository,
                                 ProgressService progressService) {
        this.cardRepository = cardRepository;
        this.challengeRepository = challengeRepository;
        this.submissionRepository = submissionRepository;
        this.designExerciseRepository = designExerciseRepository;
        this.designSubmissionRepository = designSubmissionRepository;
        this.progressService = progressService;
    }

    public DailyRecommendationDto getDailyRecommendation() {
        List<CardDto> reviewCards = getSystematicReviewCards();
        List<ChallengeDto> dsaChallenges = getProgressiveChallenges();
        DesignExerciseDto designExercise = getProgressiveDesignExercise();
        String message = generateMotivationalMessage(reviewCards, dsaChallenges, designExercise);

        return new DailyRecommendationDto(reviewCards, dsaChallenges, designExercise, message);
    }

    /**
     * Systematic review: mix of NEW cards (easy first) + REVISION of previously seen cards.
     * - Slot 1-7: New/due cards ordered by difficulty (EASY → MEDIUM → HARD)
     * - Slot 8-10: Previously reviewed cards due for spaced repetition
     */
    List<CardDto> getSystematicReviewCards() {
        LocalDateTime now = LocalDateTime.now();
        List<CardDto> result = new ArrayList<>();

        // Part 1: New cards (never reviewed) — start with EASY
        Page<Card> newCards = cardRepository.findDueCards(now, PageRequest.of(0, 50));
        List<Card> sortedNew = newCards.getContent().stream()
                .sorted(Comparator
                        .comparing((Card c) -> difficultyOrder(c.getDifficulty()))
                        .thenComparing(Card::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        // Prioritize cards from weak areas first within the sorted list
        List<TopicDto> weakAreas = progressService.getWeakAreas(0);
        Set<UUID> weakTopicIds = weakAreas.stream().map(TopicDto::id).collect(Collectors.toSet());

        List<Card> weakCards = sortedNew.stream()
                .filter(c -> weakTopicIds.contains(c.getTopic().getId()))
                .toList();
        List<Card> otherCards = sortedNew.stream()
                .filter(c -> !weakTopicIds.contains(c.getTopic().getId()))
                .toList();

        // Take from weak areas first, then fill with others
        List<Card> newPool = new ArrayList<>();
        newPool.addAll(weakCards);
        newPool.addAll(otherCards);

        newPool.stream()
                .limit(NEW_CARD_SLOTS)
                .map(DtoMapper::toDto)
                .forEach(result::add);

        // Part 2: Revision cards — previously reviewed cards that are due again (spaced repetition)
        // These are cards with status REVIEW or LEARNING that are due
        List<Card> revisionCards = newCards.getContent().stream()
                .filter(c -> c.getStatus() == CardStatus.REVIEW || c.getStatus() == CardStatus.LEARNING)
                .filter(c -> c.getNextReview() != null && c.getNextReview().isBefore(now))
                .sorted(Comparator.comparing(Card::getNextReview))
                .limit(REVISION_CARD_SLOTS)
                .toList();

        Set<UUID> alreadyAdded = result.stream().map(CardDto::id).collect(Collectors.toSet());
        revisionCards.stream()
                .filter(c -> !alreadyAdded.contains(c.getId()))
                .map(DtoMapper::toDto)
                .forEach(result::add);

        return result.stream().limit(MAX_REVIEW_CARDS).toList();
    }

    /**
     * Progressive DSA challenges: systematic EASY → MEDIUM → HARD progression.
     * - If unsolved EASY exist → recommend next EASY
     * - If all EASY solved → recommend MEDIUM
     * - If all MEDIUM solved → recommend HARD
     * - Include 1 revision challenge (previously solved, for re-practice)
     */
    List<ChallengeDto> getProgressiveChallenges() {
        List<Challenge> allChallenges = challengeRepository.findAll();
        Set<UUID> solvedIds = getSolvedChallengeIds(allChallenges);

        List<Challenge> unsolved = allChallenges.stream()
                .filter(c -> !solvedIds.contains(c.getId()))
                .toList();

        if (unsolved.isEmpty()) {
            // All solved — recommend hardest ones for re-practice
            return allChallenges.stream()
                    .filter(c -> c.getDifficulty() == Difficulty.HARD)
                    .limit(DSA_CHALLENGE_COUNT)
                    .map(c -> DtoMapper.toDto(c, true))
                    .toList();
        }

        // Sort unsolved by difficulty order (EASY first, then MEDIUM, then HARD)
        List<Challenge> sorted = unsolved.stream()
                .sorted(Comparator.comparingInt(c -> difficultyOrder(c.getDifficulty())))
                .toList();

        List<ChallengeDto> result = new ArrayList<>();

        // Pick the NEXT challenge in sequence (easiest unsolved)
        Challenge nextChallenge = sorted.get(0);
        result.add(DtoMapper.toDto(nextChallenge, false));

        // Pick second challenge: same difficulty if available, else next difficulty
        if (sorted.size() > 1) {
            Challenge second = sorted.get(1);
            result.add(DtoMapper.toDto(second, false));
        }

        return result;
    }

    /**
     * Progressive design exercises: EASY → MEDIUM → HARD.
     * Always picks the easiest unstarted exercise.
     */
    DesignExerciseDto getProgressiveDesignExercise() {
        List<DesignExercise> allExercises = designExerciseRepository.findAll();
        if (allExercises.isEmpty()) {
            return null;
        }

        // Get IDs of exercises that have been submitted
        Set<UUID> attemptedIds = allExercises.stream()
                .filter(e -> !designSubmissionRepository.findByExerciseIdOrderBySubmittedAtDesc(e.getId()).isEmpty())
                .map(DesignExercise::getId)
                .collect(Collectors.toSet());

        List<DesignExercise> unseen = allExercises.stream()
                .filter(e -> !attemptedIds.contains(e.getId()))
                .sorted(Comparator.comparingInt(e -> difficultyOrder(e.getDifficulty())))
                .toList();

        if (unseen.isEmpty()) {
            // All attempted — pick easiest for revision
            return allExercises.stream()
                    .sorted(Comparator.comparingInt(e -> difficultyOrder(e.getDifficulty())))
                    .map(DtoMapper::toDto)
                    .findFirst()
                    .orElse(null);
        }

        // Return easiest unseen exercise
        return DtoMapper.toDto(unseen.get(0));
    }

    String generateMotivationalMessage(List<CardDto> cards, List<ChallengeDto> challenges,
                                       DesignExerciseDto exercise) {
        int streak = progressService.getCurrentStreak();
        List<TopicDto> weakAreas = progressService.getWeakAreas(0);

        StringBuilder message = new StringBuilder();

        if (streak >= 7) {
            message.append("\uD83D\uDD25 ").append(streak).append("-day streak! ");
        } else if (streak > 0) {
            message.append("Day ").append(streak).append(" — ");
        }

        // Determine current level based on what's being recommended
        if (!challenges.isEmpty()) {
            Difficulty level = challenges.get(0).difficulty();
            String levelMsg = switch (level) {
                case EASY -> "Building foundations. Master the basics first.";
                case MEDIUM -> "Leveling up! Medium challenges unlock pattern recognition.";
                case HARD -> "Advanced territory. You're ready for the toughest problems.";
            };
            message.append(levelMsg);
        } else if (!weakAreas.isEmpty()) {
            message.append("Focus on ").append(weakAreas.get(0).name()).append(" today.");
        } else {
            message.append("Consistent practice beats intensity. Keep going.");
        }

        return message.toString();
    }

    private Set<UUID> getSolvedChallengeIds(List<Challenge> challenges) {
        return challenges.stream()
                .filter(c -> {
                    List<Submission> subs = submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(c.getId());
                    return subs.stream().anyMatch(s -> s.getStatus() == SubmissionStatus.PASSED);
                })
                .map(Challenge::getId)
                .collect(Collectors.toSet());
    }

    private int difficultyOrder(Difficulty d) {
        if (d == null) return 1;
        return switch (d) {
            case EASY -> 0;
            case MEDIUM -> 1;
            case HARD -> 2;
        };
    }
}
