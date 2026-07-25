package dev.interviewkata.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "card_review")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Card card;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(nullable = false)
    private int grade;

    @Column(name = "previous_interval", nullable = false)
    private int previousInterval;

    @Column(name = "new_interval", nullable = false)
    private int newInterval;

    @Column(name = "previous_ease", nullable = false)
    private double previousEase;

    @Column(name = "new_ease", nullable = false)
    private double newEase;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        reviewedAt = LocalDateTime.now();
    }
}
