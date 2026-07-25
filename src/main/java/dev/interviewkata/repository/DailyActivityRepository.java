package dev.interviewkata.repository;

import dev.interviewkata.model.DailyActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyActivityRepository extends JpaRepository<DailyActivity, UUID> {

    Optional<DailyActivity> findByActivityDate(LocalDate date);

    List<DailyActivity> findByActivityDateBetweenOrderByActivityDateDesc(LocalDate start, LocalDate end);
}
