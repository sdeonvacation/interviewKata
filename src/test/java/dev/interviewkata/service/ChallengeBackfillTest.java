package dev.interviewkata.service;

import dev.interviewkata.ai.AiService;
import dev.interviewkata.model.Challenge;
import dev.interviewkata.model.enums.ChallengeType;
import dev.interviewkata.model.enums.Difficulty;
import dev.interviewkata.repository.ChallengeRepository;
import dev.interviewkata.repository.SubmissionRepository;
import dev.interviewkata.sandbox.JShellSandbox;
import dev.interviewkata.sandbox.TestCaseResult;
import dev.interviewkata.sandbox.TestResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeBackfillTest {

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

    private Challenge challengeWithTests(String title) {
        return Challenge.builder()
                .id(UUID.randomUUID())
                .title(title)
                .challengeType(ChallengeType.DSA)
                .difficulty(Difficulty.EASY)
                .problemStatement("Solve " + title)
                .starterCode("public int solve(int n) {}")
                .testCases(List.of(Map.of("input", "n = 5", "expected", "10", "description", "Double")))
                .build();
    }

    private TestResult passed() {
        return TestResult.ofResults(List.of(TestCaseResult.success("Double", "10", "10")), 20);
    }

    private TestResult failed() {
        return TestResult.ofResults(List.of(TestCaseResult.failure("Double", "5", "10")), 20);
    }

    @Test
    void backfill_savesOnlyChallengesWhoseSolutionPasses() {
        Challenge passing = challengeWithTests("Passing");
        Challenge failing = challengeWithTests("Failing");

        // First lookup returns both; the post-run remaining lookup returns just the failing one.
        when(challengeRepository.findWithoutReferenceSolution())
                .thenReturn(List.of(passing, failing))
                .thenReturn(List.of(failing));

        when(aiService.generateReferenceSolution(anyString(), anyString(), anyString()))
                .thenReturn("public int solve(int n){return n*2;}");

        when(jshellSandbox.executeWithTests(anyString(), anyString(), anyList()))
                .thenReturn(passed())   // Passing challenge
                .thenReturn(failed())   // Failing challenge attempt 1
                .thenReturn(failed());  // Failing challenge attempt 2

        ChallengeService.BackfillResult result =
                challengeService.backfillReferenceSolutions(20, 2);

        // Only the passing challenge gets a reference solution + save.
        assertNotNull(passing.getReferenceSolution());
        assertNull(failing.getReferenceSolution());
        verify(challengeRepository, times(1)).save(passing);
        verify(challengeRepository, never()).save(failing);

        assertEquals(2, result.attempted());
        assertEquals(1, result.succeeded());
        assertEquals(1, result.failed());
        assertEquals(0, result.skipped());
        assertEquals(1L, result.remaining());
    }

    @Test
    void backfill_retriesFailingChallengeUpToMaxAttempts() {
        Challenge c = challengeWithTests("Retry");
        when(challengeRepository.findWithoutReferenceSolution())
                .thenReturn(List.of(c))
                .thenReturn(List.of(c));
        when(aiService.generateReferenceSolution(anyString(), anyString(), anyString()))
                .thenReturn("public int solve(int n){return n;}");
        when(jshellSandbox.executeWithTests(anyString(), anyString(), anyList()))
                .thenReturn(failed());

        ChallengeService.BackfillResult result =
                challengeService.backfillReferenceSolutions(20, 2);

        // Two attempts: AI + sandbox invoked twice.
        verify(aiService, times(2)).generateReferenceSolution(anyString(), anyString(), anyString());
        verify(jshellSandbox, times(2)).executeWithTests(anyString(), anyString(), anyList());
        verify(challengeRepository, never()).save(any());
        assertEquals(1, result.attempted());
        assertEquals(0, result.succeeded());
        assertEquals(1, result.failed());
    }

    @Test
    void backfill_stopsRetryingOncePassed() {
        Challenge c = challengeWithTests("PassFirst");
        when(challengeRepository.findWithoutReferenceSolution())
                .thenReturn(List.of(c))
                .thenReturn(List.of());
        when(aiService.generateReferenceSolution(anyString(), anyString(), anyString()))
                .thenReturn("public int solve(int n){return n*2;}");
        when(jshellSandbox.executeWithTests(anyString(), anyString(), anyList()))
                .thenReturn(passed());

        ChallengeService.BackfillResult result =
                challengeService.backfillReferenceSolutions(20, 3);

        // Passed on first attempt: only one AI call and one sandbox run.
        verify(aiService, times(1)).generateReferenceSolution(anyString(), anyString(), anyString());
        verify(jshellSandbox, times(1)).executeWithTests(anyString(), anyString(), anyList());
        verify(challengeRepository, times(1)).save(c);
        assertEquals(1, result.succeeded());
        assertEquals(0L, result.remaining());
    }

    @Test
    void backfill_skipsChallengeWithNoTestCases() {
        Challenge noTests = challengeWithTests("NoTests");
        noTests.setTestCases(List.of());

        when(challengeRepository.findWithoutReferenceSolution())
                .thenReturn(List.of(noTests))
                .thenReturn(List.of(noTests));

        ChallengeService.BackfillResult result =
                challengeService.backfillReferenceSolutions(20, 2);

        // Cannot verify without test cases: skipped, no AI/sandbox/save.
        verify(aiService, never()).generateReferenceSolution(anyString(), anyString(), anyString());
        verify(jshellSandbox, never()).executeWithTests(anyString(), anyString(), anyList());
        verify(challengeRepository, never()).save(any());
        assertEquals(0, result.attempted());
        assertEquals(1, result.skipped());
        assertEquals(1L, result.remaining());
    }

    @Test
    void backfill_oneChallengeErrorDoesNotAbortBatch() {
        Challenge boom = challengeWithTests("Boom");
        Challenge ok = challengeWithTests("Ok");
        when(challengeRepository.findWithoutReferenceSolution())
                .thenReturn(List.of(boom, ok))
                .thenReturn(List.of(boom));
        when(aiService.generateReferenceSolution(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("AI down"))
                .thenReturn("public int solve(int n){return n*2;}");
        when(jshellSandbox.executeWithTests(anyString(), anyString(), anyList()))
                .thenReturn(passed());

        ChallengeService.BackfillResult result =
                challengeService.backfillReferenceSolutions(20, 1);

        // Boom throws; Ok still processed and saved.
        verify(challengeRepository, times(1)).save(ok);
        verify(challengeRepository, never()).save(boom);
        assertEquals(2, result.attempted());
        assertEquals(1, result.succeeded());
        assertEquals(1, result.failed());
    }

    @Test
    void backfill_respectsLimit() {
        Challenge a = challengeWithTests("A");
        Challenge b = challengeWithTests("B");
        Challenge c = challengeWithTests("C");
        when(challengeRepository.findWithoutReferenceSolution())
                .thenReturn(List.of(a, b, c))
                .thenReturn(List.of(b, c));
        when(aiService.generateReferenceSolution(anyString(), anyString(), anyString()))
                .thenReturn("public int solve(int n){return n*2;}");
        when(jshellSandbox.executeWithTests(anyString(), anyString(), anyList()))
                .thenReturn(passed());

        ChallengeService.BackfillResult result =
                challengeService.backfillReferenceSolutions(1, 1);

        // Only the first candidate is processed.
        verify(aiService, times(1)).generateReferenceSolution(anyString(), anyString(), anyString());
        assertEquals(1, result.attempted());
        assertEquals(1, result.succeeded());
    }
}
