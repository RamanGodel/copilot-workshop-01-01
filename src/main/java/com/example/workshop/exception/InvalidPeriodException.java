package com.example.workshop.exception;

/**
 * Exception thrown when an invalid period format is provided.
 */
public class InvalidPeriodException extends RuntimeException {

    public InvalidPeriodException(String message) {
        super(message);
    }

    public InvalidPeriodException(String period, String expectedFormat) {
        super(String.format("Invalid period format '%s'. Expected format: %s", period, expectedFormat));
    }

    public InvalidPeriodException(String message, Throwable cause) {
        super(message, cause);
    }
}

