package dev.interviewkata.controller;

import dev.interviewkata.dto.*;
import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.CardStatus;
import dev.interviewkata.service.DashboardService;
import dev.interviewkata.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private RecommendationService recommendationService;

    private DashboardController controller;

    @BeforeEach
    void setUp() {
        controller = new DashboardController(dashboardService, recommendationService);
    }

    @Test
    void getDashboard_returnsOk() {
        DashboardDto dto = new DashboardDto(5, 3, 3,
                new DashboardDto.DailyActivityDto(2, 1, 0, 0, 30),
                List.of("Trees"), List.of());
        when(dashboardService.getDashboard()).thenReturn(dto);

        ResponseEntity<DashboardDto> response = controller.getDashboard();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void getRecommendations_returnsOk() {
        CardDto card = new CardDto(UUID.randomUUID(), UUID.randomUUID(), "Arrays",
                "Q", "A", null, null, Difficulty.EASY, List.of(), CardStatus.REVIEW, null);
        ChallengeDto challenge = new ChallengeDto(UUID.randomUUID(), UUID.randomUUID(),
                "Two Sum", Difficulty.EASY, ChallengeType.DSA, false);
        DesignExerciseDto exercise = new DesignExerciseDto(UUID.randomUUID(), UUID.randomUUID(),
                "Design Cache", Difficulty.MEDIUM, 45);

        DailyRecommendationDto dto = new DailyRecommendationDto(
                List.of(card), List.of(challenge), exercise, "Day 5 streak!");
        when(recommendationService.getDailyRecommendation()).thenReturn(dto);

        ResponseEntity<DailyRecommendationDto> response = controller.getRecommendations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().reviewCards().size());
        assertEquals(1, response.getBody().dsaChallenges().size());
        assertNotNull(response.getBody().designExercise());
        assertEquals("Day 5 streak!", response.getBody().motivationalMessage());
    }

    @Test
    void getRecommendations_emptyRecommendations() {
        DailyRecommendationDto dto = new DailyRecommendationDto(
                List.of(), List.of(), null, "You're making great progress. Keep it up!");
        when(recommendationService.getDailyRecommendation()).thenReturn(dto);

        ResponseEntity<DailyRecommendationDto> response = controller.getRecommendations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().reviewCards().isEmpty());
        assertTrue(response.getBody().dsaChallenges().isEmpty());
        assertNull(response.getBody().designExercise());
    }
}
