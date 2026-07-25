package dev.interviewkata.sandbox;

/**
 * Raw result of executing code in the JShell sandbox.
 */
public record ExecutionResult(
        String stdout,
        String stderr,
        boolean success,
        long durationMs,
        String errorMessage
) {

    public static ExecutionResult success(String stdout, long durationMs) {
        return new ExecutionResult(stdout, "", true, durationMs, null);
    }

    public static ExecutionResult error(String stderr, String errorMessage, long durationMs) {
        return new ExecutionResult("", stderr, false, durationMs, errorMessage);
    }

    public static ExecutionResult timeout(long durationMs) {
        return new ExecutionResult("", "", false, durationMs, "Execution timed out");
    }
}
