package dev.interviewkata.scheduling;

import dev.interviewkata.model.enums.CardStatus;
import dev.interviewkata.scheduling.SM2Scheduler.SM2Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SM2SchedulerTest {

    private SM2Scheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SM2Scheduler(21, 1.3);
    }

    @Test
    void grade5_intervalDoubles_easeIncreases() {
        // Starting from rep=2, interval=6, ef=2.5
        SM2Result result = scheduler.computeNext(5, 6, 2.5, 2);

        assertEquals(15, result.nextInterval()); // round(6 * 2.5) = 15
        assertTrue(result.newEaseFactor() > 2.5);
        assertEquals(3, result.newRepetitions());
        assertEquals(CardStatus.REVIEW, result.newStatus());
    }

    @Test
    void grade4_intervalAdvancesNormally() {
        SM2Result result = scheduler.computeNext(4, 6, 2.5, 2);

        assertEquals(15, result.nextInterval()); // round(6 * 2.5) = 15
        // ef = 2.5 + (0.1 - 1*(0.08 + 1*0.02)) = 2.5 + 0.0 = 2.5
        assertEquals(2.5, result.newEaseFactor(), 0.001);
        assertEquals(3, result.newRepetitions());
        assertEquals(CardStatus.REVIEW, result.newStatus());
    }

    @Test
    void grade3_intervalAdvances_easeDecreases() {
        SM2Result result = scheduler.computeNext(3, 6, 2.5, 2);

        assertEquals(15, result.nextInterval()); // round(6 * 2.5) = 15
        // ef = 2.5 + (0.1 - 2*(0.08 + 2*0.02)) = 2.5 + (0.1 - 0.24) = 2.36
        assertEquals(2.36, result.newEaseFactor(), 0.001);
        assertEquals(3, result.newRepetitions());
        assertEquals(CardStatus.REVIEW, result.newStatus());
    }

    @Test
    void grade2_resetsToIntervalOne_repetitionsZero() {
        SM2Result result = scheduler.computeNext(2, 15, 2.5, 5);

        assertEquals(1, result.nextInterval());
        assertEquals(0, result.newRepetitions());
        assertEquals(CardStatus.REVIEW, result.newStatus());
    }

    @Test
    void grade1_resetsToIntervalOne_repetitionsZero() {
        SM2Result result = scheduler.computeNext(1, 15, 2.5, 5);

        assertEquals(1, result.nextInterval());
        assertEquals(0, result.newRepetitions());
        assertEquals(CardStatus.REVIEW, result.newStatus());
    }

    @Test
    void easeFloor_neverBelowMinimum() {
        // Grade 1 with already low ease: ef = 1.3 + (0.1 - 4*(0.08 + 4*0.02)) = 1.3 + (0.1 - 0.64) = 0.76
        SM2Result result = scheduler.computeNext(1, 6, 1.3, 3);

        assertEquals(1.3, result.newEaseFactor(), 0.001);
    }

    @Test
    void newCard_rep0_firstIntervalAlwaysOne() {
        SM2Result result = scheduler.computeNext(5, 0, 2.5, 0);

        assertEquals(1, result.nextInterval());
        assertEquals(1, result.newRepetitions());
    }

    @Test
    void secondReview_rep1_intervalBecomesSix() {
        SM2Result result = scheduler.computeNext(4, 1, 2.5, 1);

        assertEquals(6, result.nextInterval());
        assertEquals(2, result.newRepetitions());
    }

    @Test
    void graduation_intervalExceedsThreshold_graduatedStatus() {
        // rep=2, interval=10, ef=2.5 → nextInterval = round(10 * 2.5) = 25 > 21
        SM2Result result = scheduler.computeNext(5, 10, 2.5, 2);

        assertEquals(25, result.nextInterval());
        assertEquals(CardStatus.GRADUATED, result.newStatus());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6, -1, 10})
    void invalidGrade_throwsIllegalArgumentException(int invalidGrade) {
        assertThrows(IllegalArgumentException.class,
                () -> scheduler.computeNext(invalidGrade, 6, 2.5, 2));
    }

    @Test
    void grade5_easeFactorCalculation() {
        // ef = 2.5 + (0.1 - 0*(0.08 + 0*0.02)) = 2.5 + 0.1 = 2.6
        SM2Result result = scheduler.computeNext(5, 6, 2.5, 2);

        assertEquals(2.6, result.newEaseFactor(), 0.001);
    }

    @Test
    void customGraduatingInterval_respected() {
        SM2Scheduler customScheduler = new SM2Scheduler(10, 1.3);
        // interval=5, ef=2.5, rep=2 → nextInterval = round(5*2.5) = 13 > 10
        SM2Result result = customScheduler.computeNext(4, 5, 2.5, 2);

        assertEquals(13, result.nextInterval());
        assertEquals(CardStatus.GRADUATED, result.newStatus());
    }
}
