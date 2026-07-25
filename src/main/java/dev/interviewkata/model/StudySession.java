package dev.interviewkata.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "study_session")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_type", nullable = false, length = 50)
    private String sessionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Topic topic;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "items_completed", nullable = false)
    private int itemsCompleted;

    @Column
    private Double score;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
    }
}
