package dev.interviewkata.service;

import dev.interviewkata.dto.DtoMapper;
import dev.interviewkata.dto.TopicDto;
import dev.interviewkata.model.DailyActivity;
import dev.interviewkata.model.UserProgress;
import dev.interviewkata.repository.CardRepository;
import dev.interviewkata.repository.DailyActivityRepository;
import dev.interviewkata.repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProgressService {

    private final UserProgressRepository userProgressRepository;
    private final DailyActivityRepository dailyActivityRepository;
    private final CardRepository cardRepository;
    private final double weakAreaThreshold;

    public ProgressService(UserProgressRepository userProgressRepository,
                           DailyActivityRepository dailyActivityRepository,
                           CardRepository cardRepository,
                           @Value("${interviewkata.progress.weak-area-threshold:0.6}") double weakAreaThreshold) {
        this.userProgressRepository = userProgressRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.cardRepository = cardRepository;
        this.weakAreaThreshold = weakAreaThreshold;
    }

    public List<UserProgress> getOverallProgress() {
        return userProgressRepository.findAll();
    }

    public int getCurrentStreak() {
        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate checkDate = today;

        while (true) {
            var activity = dailyActivityRepository.findByActivityDate(checkDate);
            if (activity.isPresent() && hasActivity(activity.get())) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    public List<TopicDto> getWeakAreas(double threshold) {
        double effectiveThreshold = threshold > 0 ? threshold : weakAreaThreshold;
        return userProgressRepository.findAll().stream()
                .filter(p -> getMasteryRatio(p) < effectiveThreshold)
                .map(p -> {
                    var topic = p.getTopic();
                    int cardCount = cardRepository.findByTopicId(topic.getId()).size();
                    int childCount = topic.getChildren() != null ? topic.getChildren().size() : 0;
                    return DtoMapper.toDto(topic, childCount, cardCount);
                })
                .toList();
    }

    @Transactional
    public void recordActivity(String type) {
        LocalDate today = LocalDate.now();
        DailyActivity activity = dailyActivityRepository.findByActivityDate(today)
                .orElseGet(() -> {
                    DailyActivity newActivity = DailyActivity.builder()
                            .activityDate(today)
                            .build();
                    return dailyActivityRepository.save(newActivity);
                });

        switch (type) {
            case "challenge" -> activity.setChallengesSolved(activity.getChallengesSolved() + 1);
            case "quiz" -> activity.setQuizzesCompleted(activity.getQuizzesCompleted() + 1);
            case "interview" -> activity.setInterviewsDone(activity.getInterviewsDone() + 1);
            default -> { /* no-op for unknown types */ }
        }
        dailyActivityRepository.save(activity);
    }

    private boolean hasActivity(DailyActivity activity) {
        return activity.getCardsReviewed() > 0
                || activity.getChallengesSolved() > 0
                || activity.getQuizzesCompleted() > 0
                || activity.getInterviewsDone() > 0
                || activity.getStudyMinutes() > 0;
    }

    private double getMasteryRatio(UserProgress progress) {
        if (progress.getCardsTotal() == 0) return 0.0;
        return (double) progress.getCardsMastered() / progress.getCardsTotal();
    }
}
