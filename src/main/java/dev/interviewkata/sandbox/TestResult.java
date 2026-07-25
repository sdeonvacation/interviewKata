package dev.interviewkata.sandbox;

import java.util.List;

/**
 * Aggregate result of executing all test cases for a submission.
 */
public record TestResult(
        int passed,
        int failed,
        int total,
        List<TestCaseResult> details,
        long totalDurationMs,
        boolean timeout,
        String compilationError
) {

    public boolean allPassed() {
        return passed == total && total > 0;
    }

    public boolean hasCompilationError() {
        return compilationError != null && !compilationError.isBlank();
    }

    public boolean isTimeout() {
        return timeout;
    }

    public static TestResult ofTimeout(long durationMs) {
        return new TestResult(0, 0, 0, List.of(), durationMs, true, null);
    }

    public static TestResult ofCompilationError(String error, long durationMs) {
        return new TestResult(0, 0, 0, List.of(), durationMs, false, error);
    }

    public static TestResult ofResults(List<TestCaseResult> details, long durationMs) {
        int passed = (int) details.stream().filter(TestCaseResult::passed).count();
        int total = details.size();
        return new TestResult(passed, total - passed, total, details, durationMs, false, null);
    }
}
