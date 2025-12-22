package com.example.workshop.provider;

import com.example.workshop.config.ProvidersProperties;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateProviderAggregatorResilienceTest {

    @Test
    void shouldRetryAndStillSucceedWithSecondProvider() {
        AtomicInteger calls = new AtomicInteger();

        ExchangeRateProvider flaky = new ExchangeRateProvider() {
            @Override public String getProviderName() { return "flaky"; }

            @Override
            public Optional<ProviderRatesResponse> fetchLatestRates(String baseCurrencyCode) {
                calls.incrementAndGet();
                throw new ProviderUnavailableException("boom");
            }
        };

        ExchangeRateProvider fallback = new ExchangeRateProvider() {
            @Override public String getProviderName() { return "fallback"; }

            @Override
            public Optional<ProviderRatesResponse> fetchLatestRates(String baseCurrencyCode) {
                return Optional.of(ProviderRatesResponse.builder()
                        .provider("fallback")
                        .base(baseCurrencyCode)
                        .timestamp(java.time.LocalDateTime.now())
                        .rates(java.util.Map.of("EUR", java.math.BigDecimal.ONE))
                        .build());
            }
        };

        ProvidersProperties props = new ProvidersProperties();
        props.setOrder(List.of("flaky", "fallback"));
        props.setRetryMaxAttempts(2);

        ExchangeRateProviderAggregator agg = new ExchangeRateProviderAggregator(List.of(flaky, fallback), props);
        ExchangeRateProviderAggregator.AggregationResult result = agg.fetchLatestRates("USD");

        assertThat(result.hasData()).isTrue();
        assertThat(result.getChosen().getProvider()).isEqualTo("fallback");
        // flaky provider was attempted (may include retries)
        assertThat(calls.get()).isGreaterThanOrEqualTo(1);
    }
}

