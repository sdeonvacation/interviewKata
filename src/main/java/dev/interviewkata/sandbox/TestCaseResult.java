package dev.interviewkata.sandbox;

/**
 * Result of executing a single test case.
 */
public record TestCaseResult(
        String description,
        boolean passed,
        String actual,
        String expected,
        String error
) {

    public static TestCaseResult success(String description, String actual, String expected) {
        return new TestCaseResult(description, true, actual, expected, null);
    }

    public static TestCaseResult failure(String description, String actual, String expected) {
        return new TestCaseResult(description, false, actual, expected, null);
    }

    public static TestCaseResult error(String description, String expected, String errorMessage) {
        return new TestCaseResult(description, false, null, expected, errorMessage);
    }
}
