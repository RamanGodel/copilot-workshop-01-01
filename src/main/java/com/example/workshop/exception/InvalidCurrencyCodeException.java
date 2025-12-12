package com.example.workshop.exception;

/**
 * Exception thrown when an invalid currency code is provided.
 */
public class InvalidCurrencyCodeException extends IllegalArgumentException {

    public InvalidCurrencyCodeException(String message) {
        super(message);
    }

    public InvalidCurrencyCodeException(String currencyCode, String reason) {
        super(String.format("Invalid currency code '%s': %s", currencyCode, reason));
    }

    public InvalidCurrencyCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
