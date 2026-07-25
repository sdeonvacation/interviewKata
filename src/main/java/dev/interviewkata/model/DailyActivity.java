package dev.interviewkata.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "activity_date", nullable = false, unique = true)
    private LocalDate activityDate;

    @Column(name = "cards_reviewed", nullable = false)
    @Builder.Default
    private int cardsReviewed = 0;

    @Column(name = "challenges_solved", nullable = false)
    @Builder.Default
    private int challengesSolved = 0;

    @Column(name = "quizzes_completed", nullable = false)
    @Builder.Default
    private int quizzesCompleted = 0;

    @Column(name = "interviews_done", nullable = false)
    @Builder.Default
    private int interviewsDone = 0;

    @Column(name = "study_minutes", nullable = false)
    @Builder.Default
    private int studyMinutes = 0;
}
