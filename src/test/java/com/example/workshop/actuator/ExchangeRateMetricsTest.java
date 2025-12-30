package com.example.workshop.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ExchangeRateMetrics.
 */
class ExchangeRateMetricsTest {

    private MeterRegistry meterRegistry;
    private ExchangeRateMetrics metrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metrics = new ExchangeRateMetrics(meterRegistry);
    }

    @Test
    void shouldRecordFetchSuccess() {
        // Given
        double initialCount = getCounterValue("exchange.rate.fetch.success");

        // When
        metrics.recordFetchSuccess();

        // Then
        double newCount = getCounterValue("exchange.rate.fetch.success");
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    @Test
    void shouldRecordFetchFailure() {
        // Given
        double initialCount = getCounterValue("exchange.rate.fetch.failure");

        // When
        metrics.recordFetchFailure();

        // Then
        double newCount = getCounterValue("exchange.rate.fetch.failure");
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    @Test
    void shouldRecordFetchDuration() {
        // Given
        long duration = 100L;

        // When
        metrics.recordFetchDuration(duration);

        // Then
        Timer timer = meterRegistry.find("exchange.rate.fetch.duration").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    void shouldRecordProviderMetric() {
        // Given
        String providerName = "test-provider";

        // When
        metrics.recordProviderMetric(providerName, true);
        metrics.recordProviderMetric(providerName, false);

        // Then
        Counter successCounter = meterRegistry.find("provider.fetch")
                .tag("provider", providerName)
                .tag("status", "success")
                .counter();
        
        Counter failureCounter = meterRegistry.find("provider.fetch")
                .tag("provider", providerName)
                .tag("status", "failure")
                .counter();

        assertThat(successCounter).isNotNull();
        assertThat(successCounter.count()).isEqualTo(1.0);
        
        assertThat(failureCounter).isNotNull();
        assertThat(failureCounter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldReturnMeterRegistry() {
        // When
        MeterRegistry registry = metrics.getMeterRegistry();

        // Then
        assertThat(registry).isNotNull();
        assertThat(registry).isEqualTo(meterRegistry);
    }

    private double getCounterValue(String counterName) {
        Counter counter = meterRegistry.find(counterName).counter();
        return counter != null ? counter.count() : 0.0;
    }
}
