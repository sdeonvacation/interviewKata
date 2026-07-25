package dev.interviewkata.repository;

import dev.interviewkata.model.DesignSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DesignSubmissionRepository extends JpaRepository<DesignSubmission, UUID> {

    List<DesignSubmission> findByExerciseIdOrderBySubmittedAtDesc(UUID exerciseId);
}
