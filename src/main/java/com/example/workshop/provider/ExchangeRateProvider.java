package com.example.workshop.provider;

import java.util.Optional;

import com.example.workshop.provider.ProviderRatesResponse;
import com.example.workshop.provider.ProviderUnavailableException;

/**
 * SPI for fetching exchange rates from an external system.
 *
 * Implementations should:
 * - Return a normalized {@link ProviderRatesResponse} on success.
 * - Return {@link Optional#empty()} for "no data" scenarios.
 * - Throw {@link ProviderUnavailableException} for transport / parse / provider errors.
 */
public interface ExchangeRateProvider {

    String getProviderName();

    /**
     * Fetch latest rates for a base currency.
     *
     * @param baseCurrencyCode ISO 4217 base currency code (e.g. "USD")
     */
    Optional<ProviderRatesResponse> fetchLatestRates(String baseCurrencyCode);
}
