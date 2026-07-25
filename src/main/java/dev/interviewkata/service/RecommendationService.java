package dev.interviewkata.service;

import dev.interviewkata.dto.*;
import dev.interviewkata.model.Challenge;
import dev.interviewkata.model.DesignExercise;
import dev.interviewkata.model.Submission;
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

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int MAX_REVIEW_CARDS = 10;
    private static final int MIN_REVIEW_CARDS = 5;
    private static final int MAX_DSA_CHALLENGES = 2;

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
        List<CardDto> reviewCards = getReviewCards();
        List<ChallengeDto> dsaChallenges = getUnsolvedChallenges();
        DesignExerciseDto designExercise = getNextDesignExercise();
        String message = generateMotivationalMessage(reviewCards, dsaChallenges, designExercise);

        return new DailyRecommendationDto(reviewCards, dsaChallenges, designExercise, message);
    }

    List<CardDto> getReviewCards() {
        LocalDateTime now = LocalDateTime.now();
        List<TopicDto> weakAreas = progressService.getWeakAreas(0);
        Set<UUID> weakTopicIds = weakAreas.stream()
                .map(TopicDto::id)
                .collect(Collectors.toSet());

        // Prioritize cards from weak topics
        List<CardDto> cards = new ArrayList<>();
        if (!weakTopicIds.isEmpty()) {
            for (UUID topicId : weakTopicIds) {
                if (cards.size() >= MAX_REVIEW_CARDS) break;
                int remaining = MAX_REVIEW_CARDS - cards.size();
                Page<dev.interviewkata.model.Card> topicCards = cardRepository.findDueCardsByTopicId(
                        topicId, now, PageRequest.of(0, remaining));
                topicCards.getContent().stream()
                        .map(DtoMapper::toDto)
                        .forEach(cards::add);
            }
        }

        // Fill remaining slots with any due cards
        if (cards.size() < MIN_REVIEW_CARDS) {
            Set<UUID> alreadyIncluded = cards.stream()
                    .map(CardDto::id)
                    .collect(Collectors.toSet());
            int remaining = MAX_REVIEW_CARDS - cards.size();
            Page<dev.interviewkata.model.Card> dueCards = cardRepository.findDueCards(
                    now, PageRequest.of(0, remaining));
            dueCards.getContent().stream()
                    .filter(c -> !alreadyIncluded.contains(c.getId()))
                    .map(DtoMapper::toDto)
                    .forEach(cards::add);
        }

        return cards.stream().limit(MAX_REVIEW_CARDS).toList();
    }

    List<ChallengeDto> getUnsolvedChallenges() {
        List<Challenge> allChallenges = challengeRepository.findAll();

        // Partition by solved status
        List<Challenge> unsolved = allChallenges.stream()
                .filter(c -> !isSolved(c.getId()))
                .toList();

        if (unsolved.isEmpty()) {
            return List.of();
        }

        // Group by difficulty for progressive selection
        Map<Difficulty, List<Challenge>> byDifficulty = unsolved.stream()
                .collect(Collectors.groupingBy(Challenge::getDifficulty));

        List<Challenge> selected = new ArrayList<>();

        // Start with EASY, move to MEDIUM if all easy solved
        List<Challenge> easyChallenges = byDifficulty.getOrDefault(Difficulty.EASY, List.of());
        if (!easyChallenges.isEmpty()) {
            selected.addAll(pickRandom(easyChallenges, MAX_DSA_CHALLENGES));
        }

        if (selected.size() < MAX_DSA_CHALLENGES) {
            List<Challenge> mediumChallenges = byDifficulty.getOrDefault(Difficulty.MEDIUM, List.of());
            if (!mediumChallenges.isEmpty()) {
                int remaining = MAX_DSA_CHALLENGES - selected.size();
                selected.addAll(pickRandom(mediumChallenges, remaining));
            }
        }

        if (selected.size() < MAX_DSA_CHALLENGES) {
            List<Challenge> hardChallenges = byDifficulty.getOrDefault(Difficulty.HARD, List.of());
            if (!hardChallenges.isEmpty()) {
                int remaining = MAX_DSA_CHALLENGES - selected.size();
                selected.addAll(pickRandom(hardChallenges, remaining));
            }
        }

        return selected.stream()
                .map(c -> DtoMapper.toDto(c, false))
                .toList();
    }

    DesignExerciseDto getNextDesignExercise() {
        List<DesignExercise> allExercises = designExerciseRepository.findAll();
        if (allExercises.isEmpty()) {
            return null;
        }

        // Filter out exercises that have submissions (already attempted)
        List<DesignExercise> unseen = allExercises.stream()
                .filter(e -> designSubmissionRepository.findByExerciseIdOrderBySubmittedAtDesc(e.getId()).isEmpty())
                .toList();

        if (unseen.isEmpty()) {
            // All attempted — pick random for re-practice
            DesignExercise random = allExercises.get(new Random().nextInt(allExercises.size()));
            return DtoMapper.toDto(random);
        }

        // Pick random unseen exercise
        DesignExercise picked = unseen.get(new Random().nextInt(unseen.size()));
        return DtoMapper.toDto(picked);
    }

    String generateMotivationalMessage(List<CardDto> cards, List<ChallengeDto> challenges,
                                       DesignExerciseDto exercise) {
        int streak = progressService.getCurrentStreak();
        List<TopicDto> weakAreas = progressService.getWeakAreas(0);

        StringBuilder message = new StringBuilder();

        if (streak > 0) {
            message.append("Day ").append(streak).append(" streak! ");
        }

        if (!weakAreas.isEmpty()) {
            message.append("Focus on ").append(weakAreas.get(0).name()).append(" today.");
        } else if (!cards.isEmpty()) {
            message.append("Keep your knowledge sharp with today's review.");
        } else if (!challenges.isEmpty()) {
            message.append("Time to sharpen your problem-solving skills.");
        } else {
            message.append("You're making great progress. Keep it up!");
        }

        return message.toString();
    }

    private boolean isSolved(UUID challengeId) {
        List<Submission> submissions = submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(challengeId);
        return submissions.stream().anyMatch(s -> s.getStatus() == SubmissionStatus.PASSED);
    }

    private <T> List<T> pickRandom(List<T> source, int count) {
        if (source.size() <= count) {
            return new ArrayList<>(source);
        }
        List<T> shuffled = new ArrayList<>(source);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }
}
