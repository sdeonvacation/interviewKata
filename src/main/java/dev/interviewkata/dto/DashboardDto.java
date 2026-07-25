package dev.interviewkata.dto;

import java.util.List;

public record DashboardDto(
        long dueCardCount,
        int currentStreak,
        int longestStreak,
        DailyActivityDto todayActivity,
        List<String> weakAreas,
        List<RecentSessionDto> recentSessions
) {

    public record DailyActivityDto(
            int cardsReviewed,
            int challengesSolved,
            int quizzesCompleted,
            int interviewsDone,
            int studyMinutes
    ) {
    }

    public record RecentSessionDto(
            String sessionType,
            String topicName,
            int itemsCompleted,
            Double score
    ) {
    }
}
