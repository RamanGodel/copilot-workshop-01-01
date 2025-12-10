package com.example.workshop.exception;

/**
 * Exception thrown when exchange rate data is not found.
 */
public class ExchangeRateNotFoundException extends RuntimeException {

    public ExchangeRateNotFoundException(String message) {
        super(message);
    }

    public ExchangeRateNotFoundException(String baseCurrency, String targetCurrency) {
        super(String.format("Exchange rate not found for %s to %s", baseCurrency, targetCurrency));
    }

    public ExchangeRateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

