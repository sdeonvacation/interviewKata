package dev.interviewkata.service;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.dto.ChallengeDto;
import dev.interviewkata.model.Challenge;
import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.repository.ChallengeRepository;
import dev.interviewkata.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.TopicArea;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AiService aiService;

    @InjectMocks
    private ChallengeService challengeService;

    private Challenge sampleChallenge;

    @BeforeEach
    void setUp() {
        Topic topic = Topic.builder()
                .id(UUID.randomUUID())
                .name("Data Structures")
                .area(TopicArea.DSA)
                .build();

        sampleChallenge = Challenge.builder()
                .id(UUID.randomUUID())
                .topic(topic)
                .title("Two Sum")
                .challengeType(ChallengeType.DSA)
                .difficulty(Difficulty.EASY)
                .problemStatement("Find two numbers that add up to target")
                .build();
    }

    @Test
    void listChallenges_bothNull_returnsAll() {
        Page<Challenge> page = new PageImpl<>(List.of(sampleChallenge));
        when(challengeRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(any())).thenReturn(List.of());

        Page<ChallengeDto> result = challengeService.listChallenges(null, null, 0);

        assertEquals(1, result.getTotalElements());
        verify(challengeRepository).findAll(any(PageRequest.class));
        verify(challengeRepository, never()).findByChallengeTypeAndDifficulty(any(), any(), any());
    }

    @Test
    void listChallenges_typeOnly_filtersByType() {
        Page<Challenge> page = new PageImpl<>(List.of(sampleChallenge));
        when(challengeRepository.findByChallengeType(eq(ChallengeType.DSA), any(PageRequest.class)))
                .thenReturn(page);
        when(submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(any())).thenReturn(List.of());

        Page<ChallengeDto> result = challengeService.listChallenges(ChallengeType.DSA, null, 0);

        assertEquals(1, result.getTotalElements());
        verify(challengeRepository).findByChallengeType(eq(ChallengeType.DSA), any(PageRequest.class));
    }

    @Test
    void listChallenges_difficultyOnly_filtersByDifficulty() {
        Page<Challenge> page = new PageImpl<>(List.of(sampleChallenge));
        when(challengeRepository.findByDifficulty(eq(Difficulty.EASY), any(PageRequest.class)))
                .thenReturn(page);
        when(submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(any())).thenReturn(List.of());

        Page<ChallengeDto> result = challengeService.listChallenges(null, Difficulty.EASY, 0);

        assertEquals(1, result.getTotalElements());
        verify(challengeRepository).findByDifficulty(eq(Difficulty.EASY), any(PageRequest.class));
    }

    @Test
    void listChallenges_bothProvided_filtersByBoth() {
        Page<Challenge> page = new PageImpl<>(List.of(sampleChallenge));
        when(challengeRepository.findByChallengeTypeAndDifficulty(
                eq(ChallengeType.DSA), eq(Difficulty.EASY), any(PageRequest.class)))
                .thenReturn(page);
        when(submissionRepository.findByChallengeIdOrderBySubmittedAtDesc(any())).thenReturn(List.of());

        Page<ChallengeDto> result = challengeService.listChallenges(ChallengeType.DSA, Difficulty.EASY, 0);

        assertEquals(1, result.getTotalElements());
        verify(challengeRepository).findByChallengeTypeAndDifficulty(
                eq(ChallengeType.DSA), eq(Difficulty.EASY), any(PageRequest.class));
    }

    @Test
    void listChallenges_emptyResult_returnsEmptyPage() {
        Page<Challenge> page = new PageImpl<>(List.of());
        when(challengeRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<ChallengeDto> result = challengeService.listChallenges(null, null, 0);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}
