package dev.interviewkata.model;

import dev.interviewkata.model.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "submission")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Challenge challenge;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "test_results", columnDefinition = "jsonb")
    private List<Map<String, Object>> testResults;

    @Column(name = "ai_review", columnDefinition = "TEXT")
    private String aiReview;

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}
