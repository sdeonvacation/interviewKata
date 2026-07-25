package dev.interviewkata.service;

import dev.interviewkata.model.Challenge;
import dev.interviewkata.repository.ChallengeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Manages spaced repetition scheduling for solved DSA challenges.
 * Interval progression: 7 → 14 → 30 → 60 days, then retired.
 */
@Service
public class ChallengePracticeService {

    private static final Logger log = LoggerFactory.getLogger(ChallengePracticeService.class);

    private static final int INITIAL_INTERVAL_DAYS = 7;
    private static final int MAX_PRACTICE_COUNT = 4; // 7, 14, 30, 60 — then done

    private final ChallengeRepository challengeRepository;

    public ChallengePracticeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    /**
     * Schedule a challenge for future re-practice after it's been solved.
     * If already scheduled, doubles the interval (re-practice success).
     */
    @Transactional
    public void scheduleNextPractice(UUID challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId).orElse(null);
        if (challenge == null) return;

        int currentInterval = challenge.getPracticeIntervalDays();
        int practiceCount = challenge.getPracticeCount();

        if (practiceCount >= MAX_PRACTICE_COUNT) {
            // Retired from practice rotation
            challenge.setNextPracticeDate(null);
            challengeRepository.save(challenge);
            log.debug("Challenge {} retired from practice rotation", challengeId);
            return;
        }

        int nextInterval;
        if (currentInterval == 0) {
            // First solve
            nextInterval = INITIAL_INTERVAL_DAYS;
        } else {
            // Re-practice success: double interval with cap
            nextInterval = Math.min(currentInterval * 2, 60);
        }

        challenge.setPracticeIntervalDays(nextInterval);
        challenge.setPracticeCount(practiceCount + 1);
        challenge.setNextPracticeDate(LocalDateTime.now().plusDays(nextInterval));
        challengeRepository.save(challenge);

        log.debug("Challenge {} scheduled for re-practice in {} days (count={})",
                challengeId, nextInterval, practiceCount + 1);
    }

    /**
     * Get challenges that are due for re-practice (next_practice_date <= now).
     */
    @Transactional(readOnly = true)
    public List<Challenge> getDuePracticeChallenges(int limit) {
        return challengeRepository.findByNextPracticeDateBeforeAndPracticeCountLessThan(
                LocalDateTime.now(), MAX_PRACTICE_COUNT, PageRequest.of(0, limit));
    }

    /**
     * Compute the next interval without persisting — for display purposes.
     */
    public int computeNextInterval(int currentInterval) {
        if (currentInterval == 0) return INITIAL_INTERVAL_DAYS;
        return Math.min(currentInterval * 2, 60);
    }
}
