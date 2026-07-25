package dev.interviewkata.service;

/**
 * Thrown when a rate limit is exceeded (e.g., daily interview limit).
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
