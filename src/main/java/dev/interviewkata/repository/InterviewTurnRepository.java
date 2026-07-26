package dev.interviewkata.repository;

import dev.interviewkata.model.InterviewTurn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewTurnRepository extends JpaRepository<InterviewTurn, UUID> {

    List<InterviewTurn> findByInterviewIdOrderByTurnNumber(UUID interviewId);

    long countByInterviewId(UUID interviewId);

    void deleteByInterviewId(UUID interviewId);
}
