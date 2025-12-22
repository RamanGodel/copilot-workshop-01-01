package com.example.workshop.provider;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderRatesResponseTest {

    @Test
    void builder_shouldDefaultRatesToEmptyMap() {
        ProviderRatesResponse resp = ProviderRatesResponse.builder()
                .provider("p1")
                .base("USD")
                .timestamp(LocalDateTime.now())
                .build();

        assertThat(resp.getRates()).isNotNull();
        assertThat(resp.getRates()).isEmpty();
    }

    @Test
    void builder_shouldAcceptRates() {
        ProviderRatesResponse resp = ProviderRatesResponse.builder()
                .provider("p1")
                .base("USD")
                .timestamp(LocalDateTime.of(2025, 1, 1, 0, 0))
                .rates(Map.of("EUR", new BigDecimal("0.9")))
                .build();

        assertThat(resp.getProvider()).isEqualTo("p1");
        assertThat(resp.getBase()).isEqualTo("USD");
        assertThat(resp.getRates()).containsEntry("EUR", new BigDecimal("0.9"));
    }
}

