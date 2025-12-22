package com.example.workshop.provider;

import com.example.workshop.config.ProvidersProperties;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateProviderAggregatorTest {

    @Test
    void shouldPickFirstProviderWithData_respectingOrder() {
        ExchangeRateProvider p1 = new ExchangeRateProvider() {
            @Override public String getProviderName() { return "p1"; }
            @Override public Optional<ProviderRatesResponse> fetchLatestRates(String baseCurrencyCode) {
                return Optional.empty();
            }
        };

        ExchangeRateProvider p2 = new ExchangeRateProvider() {
            @Override public String getProviderName() { return "p2"; }
            @Override public Optional<ProviderRatesResponse> fetchLatestRates(String baseCurrencyCode) {
                return Optional.of(ProviderRatesResponse.builder()
                        .provider("p2")
                        .base("USD")
                        .timestamp(LocalDateTime.now())
                        .rates(Map.of("EUR", new BigDecimal("0.9")))
                        .build());
            }
        };

        ProvidersProperties props = new ProvidersProperties();
        props.setOrder(List.of("p1", "p2"));

        ExchangeRateProviderAggregator agg = new ExchangeRateProviderAggregator(List.of(p2, p1), props);
        ExchangeRateProviderAggregator.AggregationResult result = agg.fetchLatestRates("USD");

        assertThat(result.hasData()).isTrue();
        assertThat(result.getChosen().getProvider()).isEqualTo("p2");
        assertThat(result.getAttempts()).hasSize(2);
        assertThat(result.getAttempts().get(0).getProvider()).isEqualTo("p1");
        assertThat(result.getAttempts().get(1).getProvider()).isEqualTo("p2");
    }
}

