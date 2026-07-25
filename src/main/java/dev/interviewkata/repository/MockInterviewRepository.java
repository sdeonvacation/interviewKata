package dev.interviewkata.repository;

import dev.interviewkata.model.MockInterview;
import dev.interviewkata.model.enums.InterviewState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface MockInterviewRepository extends JpaRepository<MockInterview, UUID> {

    long countByStartedAtAfter(LocalDateTime since);

    Page<MockInterview> findByStateNot(InterviewState state, Pageable pageable);
}
