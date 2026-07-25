package dev.interviewkata.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Topic topic;

    @Column(name = "cards_mastered", nullable = false)
    @Builder.Default
    private int cardsMastered = 0;

    @Column(name = "cards_total", nullable = false)
    @Builder.Default
    private int cardsTotal = 0;

    @Column(name = "challenges_solved", nullable = false)
    @Builder.Default
    private int challengesSolved = 0;

    @Column(name = "challenges_total", nullable = false)
    @Builder.Default
    private int challengesTotal = 0;

    @Column(name = "guides_completed", nullable = false)
    @Builder.Default
    private int guidesCompleted = 0;

    @Column(name = "guides_total", nullable = false)
    @Builder.Default
    private int guidesTotal = 0;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;
}
