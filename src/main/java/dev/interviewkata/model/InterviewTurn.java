package dev.interviewkata.model;

import dev.interviewkata.model.enums.InterviewPhase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "interview_turn")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewTurn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MockInterview interview;

    @Column(name = "turn_number", nullable = false)
    private int turnNumber;

    @Column(name = "ai_question", nullable = false, columnDefinition = "TEXT")
    private String aiQuestion;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> evaluation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewPhase phase;

    @Column(name = "asked_at", nullable = false)
    private LocalDateTime askedAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @PrePersist
    protected void onCreate() {
        askedAt = LocalDateTime.now();
    }
}
