package dev.interviewkata.service;

import dev.interviewkata.dto.DashboardDto;
import dev.interviewkata.dto.DtoMapper;
import dev.interviewkata.model.DailyActivity;
import dev.interviewkata.model.StudySession;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.repository.DailyActivityRepository;
import dev.interviewkata.repository.StudySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final CardRepository cardRepository;
    private final ProgressService progressService;
    private final DailyActivityRepository dailyActivityRepository;
    private final StudySessionRepository studySessionRepository;

    public DashboardService(CardRepository cardRepository,
                            ProgressService progressService,
                            DailyActivityRepository dailyActivityRepository,
                            StudySessionRepository studySessionRepository) {
        this.cardRepository = cardRepository;
        this.progressService = progressService;
        this.dailyActivityRepository = dailyActivityRepository;
        this.studySessionRepository = studySessionRepository;
    }

    public DashboardDto getDashboard() {
        long dueCardCount = cardRepository.countDueCards(LocalDateTime.now());

        int currentStreak = progressService.getCurrentStreak();

        List<String> weakAreas = progressService.getWeakAreas(0).stream()
                .map(t -> t.name())
                .toList();

        DashboardDto.DailyActivityDto todayActivity = dailyActivityRepository
                .findByActivityDate(LocalDate.now())
                .map(DtoMapper::toDto)
                .orElse(new DashboardDto.DailyActivityDto(0, 0, 0, 0, 0));

        List<StudySession> recentSessions = studySessionRepository
                .findByStartedAtAfterOrderByStartedAtDesc(LocalDateTime.now().minusDays(7));
        List<DashboardDto.RecentSessionDto> recentSessionDtos = recentSessions.stream()
                .limit(10)
                .map(DtoMapper::toDto)
                .toList();

        // longestStreak not tracked separately yet, use currentStreak as placeholder
        return new DashboardDto(
                dueCardCount,
                currentStreak,
                currentStreak,
                todayActivity,
                weakAreas,
                recentSessionDtos
        );
    }
}
