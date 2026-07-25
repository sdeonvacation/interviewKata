package dev.interviewkata.model;

import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "challenge")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Topic topic;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(name = "problem_statement", nullable = false, columnDefinition = "TEXT")
    private String problemStatement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "challenge_type", nullable = false)
    private ChallengeType challengeType;

    @Column(name = "starter_code", columnDefinition = "TEXT")
    private String starterCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "test_cases", columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, Object>> testCases = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> hints = new ArrayList<>();

    @Column(name = "time_limit_seconds", nullable = false)
    @Builder.Default
    private int timeLimitSeconds = 300;

    @Column(name = "next_practice_date")
    private LocalDateTime nextPracticeDate;

    @Column(name = "practice_interval_days")
    @Builder.Default
    private int practiceIntervalDays = 0;

    @Column(name = "practice_count")
    @Builder.Default
    private int practiceCount = 0;

    @Column(name = "reference_solution", columnDefinition = "TEXT")
    private String referenceSolution;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
