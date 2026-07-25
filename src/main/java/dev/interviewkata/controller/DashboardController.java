package dev.interviewkata.controller;

import dev.interviewkata.dto.DailyRecommendationDto;
import dev.interviewkata.dto.DashboardDto;
import dev.interviewkata.service.DashboardService;
import dev.interviewkata.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final RecommendationService recommendationService;

    public DashboardController(DashboardService dashboardService,
                               RecommendationService recommendationService) {
        this.dashboardService = dashboardService;
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<DashboardDto> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/recommendations")
    public ResponseEntity<DailyRecommendationDto> getRecommendations() {
        return ResponseEntity.ok(recommendationService.getDailyRecommendation());
    }
}
