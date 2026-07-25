package dev.interviewkata.sandbox;

import dev.interviewkata.config.JShellConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestResultTest {

    @Test
    void ofTimeoutSetsTimeoutFlag() {
        TestResult result = TestResult.ofTimeout(5000);

        assertTrue(result.isTimeout());
        assertFalse(result.hasCompilationError());
        assertFalse(result.allPassed());
        assertEquals(5000, result.totalDurationMs());
        assertEquals(0, result.total());
    }

    @Test
    void ofCompilationErrorSetsError() {
        TestResult result = TestResult.ofCompilationError("syntax error", 100);

        assertTrue(result.hasCompilationError());
        assertFalse(result.isTimeout());
        assertFalse(result.allPassed());
        assertEquals("syntax error", result.compilationError());
    }

    @Test
    void ofResultsCountsPassedAndFailed() {
        var details = java.util.List.of(
                TestCaseResult.success("t1", "5", "5"),
                TestCaseResult.failure("t2", "3", "5"),
                TestCaseResult.success("t3", "ok", "ok")
        );

        TestResult result = TestResult.ofResults(details, 200);

        assertEquals(2, result.passed());
        assertEquals(1, result.failed());
        assertEquals(3, result.total());
        assertFalse(result.allPassed());
        assertFalse(result.isTimeout());
        assertFalse(result.hasCompilationError());
        assertEquals(200, result.totalDurationMs());
    }

    @Test
    void allPassedTrueWhenAllPass() {
        var details = java.util.List.of(
                TestCaseResult.success("t1", "5", "5"),
                TestCaseResult.success("t2", "ok", "ok")
        );

        TestResult result = TestResult.ofResults(details, 100);

        assertTrue(result.allPassed());
    }

    @Test
    void allPassedFalseWhenEmpty() {
        TestResult result = TestResult.ofResults(java.util.List.of(), 0);

        assertFalse(result.allPassed());
    }
}
