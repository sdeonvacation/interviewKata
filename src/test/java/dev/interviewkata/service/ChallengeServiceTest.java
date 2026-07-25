package dev.interviewkata.service;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.dto.ChallengeDto;
import dev.interviewkata.dto.SubmissionResultDto;
import dev.interviewkata.model.Challenge;
import dev.interviewkata.model.Submission;
import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.model.enums.SubmissionStatus;
import dev.interviewkata.repository.ChallengeRepository;
import dev.interviewkata.repository.SubmissionRepository;
import dev.interviewkata.sandbox.JShellSandbox;
import dev.interviewkata.sandbox.TestCase;
import dev.interviewkata.sandbox.TestCaseResult;
import dev.interviewkata.sandbox.TestResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import dev.interviewkata.model.Topic;
import dev.interviewkata.model.enums.TopicArea;

import java.util.*;

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

    @Mock
    private ChallengePracticeService challengePracticeService;

    @Mock
    private JShellSandbox jshellSandbox;

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

    @Test
    void submitSolution_allTestsPass_statusPassed() {
        UUID challengeId = sampleChallenge.getId();
        sampleChallenge.setStarterCode("public int[] twoSum(int[] nums, int target) {}");
        sampleChallenge.setTestCases(List.of(
                Map.of("input", "nums = [2,7], target = 9", "expected", "[0,1]", "description", "Basic")
        ));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(sampleChallenge));

        TestResult testResult = TestResult.ofResults(
                List.of(TestCaseResult.success("Basic", "[0, 1]", "[0,1]")), 50);
        when(jshellSandbox.executeWithTests(anyString(), anyString(), anyList())).thenReturn(testResult);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmissionResultDto dto = challengeService.submitSolution(challengeId, "user code");

        assertEquals(SubmissionStatus.PASSED, dto.status());
        verify(jshellSandbox).executeWithTests(eq("user code"), anyString(), anyList());
    }

    @Test
    void submitSolution_testsFail_statusFailed() {
        UUID challengeId = sampleChallenge.getId();
        sampleChallenge.setStarterCode("public int solve(int n) {}");
        sampleChallenge.setTestCases(List.of(
                Map.of("input", "n = 5", "expected", "10", "description", "Double")
        ));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(sampleChallenge));

        TestResult testResult = TestResult.ofResults(
                List.of(TestCaseResult.failure("Double", "5", "10")), 30);
        when(jshellSandbox.executeWithTests(anyString(), anyString(), anyList())).thenReturn(testResult);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmissionResultDto dto = challengeService.submitSolution(challengeId, "bad code");

        assertEquals(SubmissionStatus.FAILED, dto.status());
    }

    @Test
    void submitSolution_compilationError_statusError() {
        UUID challengeId = sampleChallenge.getId();
        sampleChallenge.setStarterCode("public int solve(int n) {}");
        sampleChallenge.setTestCases(List.of(
                Map.of("input", "n = 5", "expected", "10", "description", "Double")
        ));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(sampleChallenge));

        TestResult testResult = TestResult.ofCompilationError("cannot find symbol", 10);
        when(jshellSandbox.executeWithTests(anyString(), anyString(), anyList())).thenReturn(testResult);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmissionResultDto dto = challengeService.submitSolution(challengeId, "broken code");

        assertEquals(SubmissionStatus.ERROR, dto.status());
    }

    @Test
    void submitSolution_timeout_statusTimeout() {
        UUID challengeId = sampleChallenge.getId();
        sampleChallenge.setStarterCode("public int solve(int n) {}");
        sampleChallenge.setTestCases(List.of(
                Map.of("input", "n = 5", "expected", "10", "description", "Double")
        ));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(sampleChallenge));

        TestResult testResult = TestResult.ofTimeout(5000);
        when(jshellSandbox.executeWithTests(anyString(), anyString(), anyList())).thenReturn(testResult);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmissionResultDto dto = challengeService.submitSolution(challengeId, "while(true){}");

        assertEquals(SubmissionStatus.TIMEOUT, dto.status());
    }

    @Test
    void submitSolution_savesExecutionTime() {
        UUID challengeId = sampleChallenge.getId();
        sampleChallenge.setStarterCode("public int solve(int n) {}");
        sampleChallenge.setTestCases(List.of(
                Map.of("input", "n = 5", "expected", "10", "description", "Double")
        ));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(sampleChallenge));

        TestResult testResult = TestResult.ofResults(
                List.of(TestCaseResult.success("Double", "10", "10")), 123);
        when(jshellSandbox.executeWithTests(anyString(), anyString(), anyList())).thenReturn(testResult);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmissionResultDto dto = challengeService.submitSolution(challengeId, "code");

        assertEquals(123, dto.executionTimeMs());
    }

    @Test
    void submitSolution_aiReviewFailure_doesNotBlockSubmission() {
        UUID challengeId = sampleChallenge.getId();
        sampleChallenge.setStarterCode("public int solve(int n) {}");
        sampleChallenge.setTestCases(List.of(
                Map.of("input", "n = 5", "expected", "10", "description", "Double")
        ));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(sampleChallenge));

        TestResult testResult = TestResult.ofResults(
                List.of(TestCaseResult.success("Double", "10", "10")), 50);
        when(jshellSandbox.executeWithTests(anyString(), anyString(), anyList())).thenReturn(testResult);
        when(aiService.reviewCode(anyString(), anyString())).thenThrow(new RuntimeException("AI down"));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

        // Should not throw despite AI failure
        SubmissionResultDto dto = challengeService.submitSolution(challengeId, "code");

        assertEquals(SubmissionStatus.PASSED, dto.status());
    }

    @Test
    void submitSolution_storesTestResultsAsJson() {
        UUID challengeId = sampleChallenge.getId();
        sampleChallenge.setStarterCode("public int[] twoSum(int[] nums, int target) {}");
        sampleChallenge.setTestCases(List.of(
                Map.of("input", "nums = [2,7], target = 9", "expected", "[0,1]", "description", "Basic")
        ));
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(sampleChallenge));

        TestResult testResult = TestResult.ofResults(
                List.of(TestCaseResult.success("Basic", "[0, 1]", "[0,1]")), 50);
        when(jshellSandbox.executeWithTests(anyString(), anyString(), anyList())).thenReturn(testResult);

        ArgumentCaptor<Submission> captor = ArgumentCaptor.forClass(Submission.class);
        when(submissionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        challengeService.submitSolution(challengeId, "code");

        Submission saved = captor.getValue();
        assertNotNull(saved.getTestResults());
        assertFalse(saved.getTestResults().isEmpty());
        Map<String, Object> firstResult = saved.getTestResults().get(0);
        assertEquals("Basic", firstResult.get("description"));
        assertEquals(true, firstResult.get("passed"));
    }
}
