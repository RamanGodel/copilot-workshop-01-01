package com.example.workshop.exception;

/**
 * Exception thrown when a currency is not found in the system.
 */
public class CurrencyNotFoundException extends RuntimeException {

    public CurrencyNotFoundException(String message) {
        super(message);
    }

    public CurrencyNotFoundException(String currencyCode, String additionalInfo) {
        super(String.format("Currency not found: %s. %s", currencyCode, additionalInfo));
    }

    public CurrencyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

