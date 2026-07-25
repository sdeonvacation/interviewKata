package dev.interviewkata.service;

import dev.interviewkata.dto.*;
import dev.interviewkata.model.*;
import dev.interviewkata.model.enums.*;
import dev.interviewkata.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private DesignExerciseRepository designExerciseRepository;

    @Mock
    private DesignSubmissionRepository designSubmissionRepository;

    @Mock
    private ProgressService progressService;

    @InjectMocks
    private RecommendationService recommendationService;

    private Topic dsaTopic;
    private Topic designTopic;

    @BeforeEach
    void setUp() {
        dsaTopic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Arrays")
                .area(TopicArea.DSA)
                .build();

        designTopic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Distributed Systems")
                .area(TopicArea.SYSTEM_DESIGN)
                .build();
    }

    @Test
    void getDailyRecommendation_returnsAllSections() {
        // Arrange
        when(progressService.getWeakAreas(0)).thenReturn(List.of());
        when(progressService.getCurrentStreak()).thenReturn(3);
        Page<Card> emptyCardPage = new PageImpl<>(List.of());
        when(cardRepository.findDueCards(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(emptyCardPage);
        when(challengeRepository.findAll()).thenReturn(List.of());
        when(designExerciseRepository.findAll()).thenReturn(List.of());

        // Act
        DailyRecommendationDto result = recommendationService.getDailyRecommendation();

        // Assert
        assertNotNull(result);
        assertNotNull(result.reviewCards());
        assertNotNull(result.dsaChallenges());
        assertNotNull(result.motivationalMessage());
    }

    @Test
    void getReviewCards_prioritizesWeakTopics() {
        // Arrange
        TopicDto weakTopic = new TopicDto(
                dsaTopic.getId(), "Arrays", TopicArea.DSA, null, null, 1, 0, 5);
        when(progressService.getWeakAreas(0)).thenReturn(List.of(weakTopic));

        Card card1 = Card.builder()
                .id(UUID.randomUUID())
                .topic(dsaTopic)
                .front("What is an array?")
                .back("A contiguous block of memory")
                .difficulty(Difficulty.EASY)
                .status(CardStatus.REVIEW)
                .build();

        Page<Card> topicCards = new PageImpl<>(List.of(card1));
        when(cardRepository.findDueCardsByTopicId(eq(dsaTopic.getId()), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(topicCards);

        // Act
        List<CardDto> result = recommendationService.getReviewCards();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(dsaTopic.getId(), result.get(0).topicId());
        verify(cardRepository).findDueCardsByTopicId(eq(dsaTopic.getId()), any(), any());
    }

    @Test
    void getReviewCards_fillsFromGeneralDueWhenWeakTopicsInsufficient() {
        // Arrange
        when(progressService.getWeakAreas(0)).thenReturn(List.of());

        Card card1 = Card.builder()
                .id(UUID.randomUUID())
                .topic(dsaTopic)
                .front("General card")
                .back("Answer")
                .difficulty(Difficulty.MEDIUM)
                .status(CardStatus.REVIEW)
                .build();

        Page<Card> dueCards = new PageImpl<>(List.of(card1));
        when(cardRepository.findDueCards(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(dueCards);

        // Act
        List<CardDto> result = recommendationService.getReviewCards();

        // Assert
        assertEquals(1, result.size());
        verify(cardRepository).findDueCards(any(), any());
    }

    @Test
    void getReviewCards_limitsToMaxCards() {
        // Arrange
        when(progressService.getWeakAreas(0)).thenReturn(List.of());

        List<Card> manyCards = new java.util.ArrayList<>();
        for (int i = 0; i < 15; i++) {
            manyCards.add(Card.builder()
                    .id(UUID.randomUUID())
                    .topic(dsaTopic)
                    .front("Card " + i)
                    .back("Answer " + i)
                    .difficulty(Difficulty.EASY)
                    .status(CardStatus.REVIEW)
                    .build());
        }

        Page<Card> dueCards = new PageImpl<>(manyCards);
        when(cardRepository.findDueCards(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(dueCards);

        // Act
        List<CardDto> result = recommendationService.getReviewCards();

        // Assert
        assertTrue(result.size() <= 10);
    }

    @Test
    void getUnsolvedChallenges_returnsUnsolvedOnly() {
        // Arrange
        Challenge solved = Challenge.builder()
                .id(UUID.randomUUID())
                .topic(dsaTopic)
                .title("Two Sum")
                .difficulty(Difficulty.EASY)
                .challengeType(ChallengeType.DSA)
                .build();

        Challenge unsolved = Challenge.builder()
                .id(UUID.randomUUID())
                .topic(dsaTopic)
                .title("Three Sum")
                .difficulty(Difficulty.EASY)
                .challengeType(ChallengeType.DSA)
                .build();

        when(challengeRepository.findAll()).thenReturn(List.of(solved, unsolved));

        Submission passedSubmission = Submission.builder()
                .id(UUID.randomUUID())
                .challenge(solved)
                .code("solution")
                .status(SubmissionStatus.PASSED)
                .build();

        when(submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(solved.getId()))
                .thenReturn(List.of(passedSubmission));
        when(submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(unsolved.getId()))
                .thenReturn(List.of());

        // Act
        List<ChallengeDto> result = recommendationService.getUnsolvedChallenges();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Three Sum", result.get(0).title());
        assertFalse(result.get(0).solved());
    }

    @Test
    void getUnsolvedChallenges_prefersEasyOverMedium() {
        // Arrange
        Challenge easy = Challenge.builder()
                .id(UUID.randomUUID())
                .topic(dsaTopic)
                .title("Easy Problem")
                .difficulty(Difficulty.EASY)
                .challengeType(ChallengeType.DSA)
                .build();

        Challenge medium = Challenge.builder()
                .id(UUID.randomUUID())
                .topic(dsaTopic)
                .title("Medium Problem")
                .difficulty(Difficulty.MEDIUM)
                .challengeType(ChallengeType.DSA)
                .build();

        when(challengeRepository.findAll()).thenReturn(List.of(easy, medium));
        when(submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(any()))
                .thenReturn(List.of());

        // Act
        List<ChallengeDto> result = recommendationService.getUnsolvedChallenges();

        // Assert
        assertFalse(result.isEmpty());
        assertTrue(result.size() <= 2);
        // Easy should be selected first
        assertTrue(result.stream().anyMatch(c -> c.title().equals("Easy Problem")));
    }

    @Test
    void getUnsolvedChallenges_returnsEmptyWhenAllSolved() {
        // Arrange
        Challenge challenge = Challenge.builder()
                .id(UUID.randomUUID())
                .topic(dsaTopic)
                .title("Solved Problem")
                .difficulty(Difficulty.EASY)
                .challengeType(ChallengeType.DSA)
                .build();

        when(challengeRepository.findAll()).thenReturn(List.of(challenge));

        Submission passed = Submission.builder()
                .id(UUID.randomUUID())
                .challenge(challenge)
                .code("code")
                .status(SubmissionStatus.PASSED)
                .build();
        when(submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(challenge.getId()))
                .thenReturn(List.of(passed));

        // Act
        List<ChallengeDto> result = recommendationService.getUnsolvedChallenges();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getUnsolvedChallenges_limitsToMaxChallenges() {
        // Arrange
        List<Challenge> manyChallenges = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            manyChallenges.add(Challenge.builder()
                    .id(UUID.randomUUID())
                    .topic(dsaTopic)
                    .title("Problem " + i)
                    .difficulty(Difficulty.EASY)
                    .challengeType(ChallengeType.DSA)
                    .build());
        }

        when(challengeRepository.findAll()).thenReturn(manyChallenges);
        when(submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(any()))
                .thenReturn(List.of());

        // Act
        List<ChallengeDto> result = recommendationService.getUnsolvedChallenges();

        // Assert
        assertTrue(result.size() <= 2);
    }

    @Test
    void getNextDesignExercise_returnsUnseenExercise() {
        // Arrange
        DesignExercise exercise = DesignExercise.builder()
                .id(UUID.randomUUID())
                .topic(designTopic)
                .title("Design a URL Shortener")
                .difficulty(Difficulty.MEDIUM)
                .estimatedMinutes(45)
                .build();

        when(designExerciseRepository.findAll()).thenReturn(List.of(exercise));
        when(designSubmissionRepository.findByExerciseIdOrderBySubmittedAtDesc(exercise.getId()))
                .thenReturn(List.of());

        // Act
        DesignExerciseDto result = recommendationService.getNextDesignExercise();

        // Assert
        assertNotNull(result);
        assertEquals("Design a URL Shortener", result.title());
    }

    @Test
    void getNextDesignExercise_returnsNullWhenNoExercises() {
        // Arrange
        when(designExerciseRepository.findAll()).thenReturn(List.of());

        // Act
        DesignExerciseDto result = recommendationService.getNextDesignExercise();

        // Assert
        assertNull(result);
    }

    @Test
    void getNextDesignExercise_fallsBackToAttemptedWhenAllSeen() {
        // Arrange
        DesignExercise exercise = DesignExercise.builder()
                .id(UUID.randomUUID())
                .topic(designTopic)
                .title("Design a Cache")
                .difficulty(Difficulty.EASY)
                .estimatedMinutes(30)
                .build();

        DesignSubmission submission = DesignSubmission.builder()
                .id(UUID.randomUUID())
                .exercise(exercise)
                .answer("My design")
                .build();

        when(designExerciseRepository.findAll()).thenReturn(List.of(exercise));
        when(designSubmissionRepository.findByExerciseIdOrderBySubmittedAtDesc(exercise.getId()))
                .thenReturn(List.of(submission));

        // Act
        DesignExerciseDto result = recommendationService.getNextDesignExercise();

        // Assert
        assertNotNull(result);
        assertEquals("Design a Cache", result.title());
    }

    @Test
    void generateMotivationalMessage_includesStreakWhenPositive() {
        // Arrange
        when(progressService.getCurrentStreak()).thenReturn(5);
        when(progressService.getWeakAreas(0)).thenReturn(List.of());

        // Act
        String message = recommendationService.generateMotivationalMessage(
                List.of(), List.of(), null);

        // Assert
        assertTrue(message.contains("Day 5 streak"));
    }

    @Test
    void generateMotivationalMessage_includesWeakAreaFocus() {
        // Arrange
        when(progressService.getCurrentStreak()).thenReturn(0);
        TopicDto weakTopic = new TopicDto(
                UUID.randomUUID(), "Trees", TopicArea.DSA, null, null, 1, 0, 3);
        when(progressService.getWeakAreas(0)).thenReturn(List.of(weakTopic));

        // Act
        String message = recommendationService.generateMotivationalMessage(
                List.of(), List.of(), null);

        // Assert
        assertTrue(message.contains("Focus on Trees today"));
    }

    @Test
    void generateMotivationalMessage_genericWhenNoStreakOrWeakAreas() {
        // Arrange
        when(progressService.getCurrentStreak()).thenReturn(0);
        when(progressService.getWeakAreas(0)).thenReturn(List.of());

        // Act
        String message = recommendationService.generateMotivationalMessage(
                List.of(), List.of(), null);

        // Assert
        assertFalse(message.isEmpty());
        assertTrue(message.contains("great progress"));
    }

    @Test
    void getDailyRecommendation_fullIntegration() {
        // Arrange - set up a realistic scenario
        TopicDto weakTopic = new TopicDto(
                dsaTopic.getId(), "Arrays", TopicArea.DSA, null, null, 1, 0, 5);
        when(progressService.getWeakAreas(0)).thenReturn(List.of(weakTopic));
        when(progressService.getCurrentStreak()).thenReturn(7);

        Card dueCard = Card.builder()
                .id(UUID.randomUUID())
                .topic(dsaTopic)
                .front("What is O(n)?")
                .back("Linear time")
                .difficulty(Difficulty.EASY)
                .status(CardStatus.REVIEW)
                .build();
        when(cardRepository.findDueCardsByTopicId(eq(dsaTopic.getId()), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dueCard)));

        Challenge challenge = Challenge.builder()
                .id(UUID.randomUUID())
                .topic(dsaTopic)
                .title("Binary Search")
                .difficulty(Difficulty.EASY)
                .challengeType(ChallengeType.DSA)
                .build();
        when(challengeRepository.findAll()).thenReturn(List.of(challenge));
        when(submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(challenge.getId()))
                .thenReturn(List.of());

        DesignExercise exercise = DesignExercise.builder()
                .id(UUID.randomUUID())
                .topic(designTopic)
                .title("Design Twitter")
                .difficulty(Difficulty.HARD)
                .estimatedMinutes(60)
                .build();
        when(designExerciseRepository.findAll()).thenReturn(List.of(exercise));
        when(designSubmissionRepository.findByExerciseIdOrderBySubmittedAtDesc(exercise.getId()))
                .thenReturn(List.of());

        // Act
        DailyRecommendationDto result = recommendationService.getDailyRecommendation();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.reviewCards().size());
        assertEquals(1, result.dsaChallenges().size());
        assertNotNull(result.designExercise());
        assertTrue(result.motivationalMessage().contains("Day 7 streak"));
        assertTrue(result.motivationalMessage().contains("Focus on Arrays today"));
    }
}
