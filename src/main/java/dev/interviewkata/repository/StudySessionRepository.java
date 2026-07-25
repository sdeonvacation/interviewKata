package dev.interviewkata.repository;

import dev.interviewkata.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {

    List<StudySession> findByStartedAtAfterOrderByStartedAtDesc(LocalDateTime since);
}
