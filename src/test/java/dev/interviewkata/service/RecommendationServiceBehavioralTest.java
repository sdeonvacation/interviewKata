package dev.interviewkata.service;

import dev.interviewkata.dto.*;
import dev.interviewkata.model.Card;
import dev.interviewkata.model.Challenge;
import dev.interviewkata.model.DesignExercise;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.CardStatus;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceBehavioralTest {

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
    @Mock
    private ChallengePracticeService challengePracticeService;

    private RecommendationService service;

    @BeforeEach
    void setUp() {
        service = new RecommendationService(
                cardRepository, challengeRepository, submissionRepository,
                designExerciseRepository, designSubmissionRepository, progressService,
                challengePracticeService);
    }

    @Test
    void shouldRecommendBehavioralPractice_everyThirdDay() {
        // The method uses day-of-year % 3 == 0
        boolean result = service.shouldRecommendBehavioralPractice();
        int dayOfYear = LocalDate.now().getDayOfYear();
        assertEquals(dayOfYear % 3 == 0, result);
    }

    @Test
    void getDailyRecommendation_includesBehavioralFlag() {
        // Setup minimal mocks for recommendation generation
        Page<Card> emptyPage = new PageImpl<>(List.of());
        when(cardRepository.findDueCards(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(challengeRepository.findAll()).thenReturn(List.of());
        when(designExerciseRepository.findAll()).thenReturn(List.of());
        when(progressService.getCurrentStreak()).thenReturn(0);
        when(progressService.getWeakAreas(0)).thenReturn(List.of());
        when(challengePracticeService.getDuePracticeChallenges(1)).thenReturn(List.of());

        DailyRecommendationDto dto = service.getDailyRecommendation();

        assertNotNull(dto);
        // behavioralPracticeRecommended should be a boolean (true or false depending on day)
        int dayOfYear = LocalDate.now().getDayOfYear();
        assertEquals(dayOfYear % 3 == 0, dto.behavioralPracticeRecommended());
    }
}
