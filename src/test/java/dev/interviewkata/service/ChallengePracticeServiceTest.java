package dev.interviewkata.service;

import dev.interviewkata.model.Challenge;
import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.TopicArea;
import dev.interviewkata.repository.ChallengeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengePracticeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @InjectMocks
    private ChallengePracticeService practiceService;

    private Challenge challenge;
    private UUID challengeId;

    @BeforeEach
    void setUp() {
        challengeId = UUID.randomUUID();
        Topic topic = Topic.builder()
                .id(UUID.randomUUID())
                .name("DSA")
                .area(TopicArea.DSA)
                .build();

        challenge = Challenge.builder()
                .id(challengeId)
                .topic(topic)
                .title("Two Sum")
                .challengeType(ChallengeType.DSA)
                .difficulty(Difficulty.EASY)
                .problemStatement("Find two numbers")
                .practiceIntervalDays(0)
                .practiceCount(0)
                .build();
    }

    @Test
    void scheduleNextPractice_firstSolve_setsInitialInterval() {
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(challengeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        practiceService.scheduleNextPractice(challengeId);

        ArgumentCaptor<Challenge> captor = ArgumentCaptor.forClass(Challenge.class);
        verify(challengeRepository).save(captor.capture());

        Challenge saved = captor.getValue();
        assertEquals(7, saved.getPracticeIntervalDays());
        assertEquals(1, saved.getPracticeCount());
        assertNotNull(saved.getNextPracticeDate());
        assertTrue(saved.getNextPracticeDate().isAfter(LocalDateTime.now().plusDays(6)));
    }

    @Test
    void scheduleNextPractice_secondSolve_doublesInterval() {
        challenge.setPracticeIntervalDays(7);
        challenge.setPracticeCount(1);
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(challengeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        practiceService.scheduleNextPractice(challengeId);

        ArgumentCaptor<Challenge> captor = ArgumentCaptor.forClass(Challenge.class);
        verify(challengeRepository).save(captor.capture());

        Challenge saved = captor.getValue();
        assertEquals(14, saved.getPracticeIntervalDays());
        assertEquals(2, saved.getPracticeCount());
    }

    @Test
    void scheduleNextPractice_thirdSolve_interval30() {
        challenge.setPracticeIntervalDays(14);
        challenge.setPracticeCount(2);
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(challengeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        practiceService.scheduleNextPractice(challengeId);

        ArgumentCaptor<Challenge> captor = ArgumentCaptor.forClass(Challenge.class);
        verify(challengeRepository).save(captor.capture());

        assertEquals(28, captor.getValue().getPracticeIntervalDays());
        assertEquals(3, captor.getValue().getPracticeCount());
    }

    @Test
    void scheduleNextPractice_intervalCappedAt60() {
        challenge.setPracticeIntervalDays(45);
        challenge.setPracticeCount(3);
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(challengeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        practiceService.scheduleNextPractice(challengeId);

        ArgumentCaptor<Challenge> captor = ArgumentCaptor.forClass(Challenge.class);
        verify(challengeRepository).save(captor.capture());

        assertEquals(60, captor.getValue().getPracticeIntervalDays());
    }

    @Test
    void scheduleNextPractice_maxCountReached_retires() {
        challenge.setPracticeIntervalDays(60);
        challenge.setPracticeCount(4); // MAX_PRACTICE_COUNT
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(challengeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        practiceService.scheduleNextPractice(challengeId);

        ArgumentCaptor<Challenge> captor = ArgumentCaptor.forClass(Challenge.class);
        verify(challengeRepository).save(captor.capture());

        assertNull(captor.getValue().getNextPracticeDate());
    }

    @Test
    void scheduleNextPractice_challengeNotFound_doesNothing() {
        UUID unknownId = UUID.randomUUID();
        when(challengeRepository.findById(unknownId)).thenReturn(Optional.empty());

        practiceService.scheduleNextPractice(unknownId);

        verify(challengeRepository, never()).save(any());
    }

    @Test
    void getDuePracticeChallenges_returnsDueChallenges() {
        challenge.setNextPracticeDate(LocalDateTime.now().minusDays(1));
        when(challengeRepository.findByNextPracticeDateBeforeAndPracticeCountLessThan(
                any(), eq(4), any()))
                .thenReturn(List.of(challenge));

        List<Challenge> due = practiceService.getDuePracticeChallenges(5);

        assertEquals(1, due.size());
        assertEquals(challengeId, due.get(0).getId());
    }

    @Test
    void computeNextInterval_zero_returnsInitial() {
        assertEquals(7, practiceService.computeNextInterval(0));
    }

    @Test
    void computeNextInterval_7_returns14() {
        assertEquals(14, practiceService.computeNextInterval(7));
    }

    @Test
    void computeNextInterval_30_returns60() {
        assertEquals(60, practiceService.computeNextInterval(30));
    }

    @Test
    void computeNextInterval_45_cappedAt60() {
        assertEquals(60, practiceService.computeNextInterval(45));
    }
}
