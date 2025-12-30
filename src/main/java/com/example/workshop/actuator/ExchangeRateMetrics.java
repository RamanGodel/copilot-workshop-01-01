package com.example.workshop.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Component for tracking custom metrics related to exchange rate operations.
 * Provides counters and timers for monitoring exchange rate fetches and provider performance.
 */
@Slf4j
@Component
public class ExchangeRateMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter exchangeRateFetchSuccessCounter;
    private final Counter exchangeRateFetchFailureCounter;
    private final Timer exchangeRateFetchTimer;

    public ExchangeRateMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.exchangeRateFetchSuccessCounter = Counter.builder("exchange.rate.fetch.success")
                .description("Number of successful exchange rate fetches")
                .register(meterRegistry);
                
        this.exchangeRateFetchFailureCounter = Counter.builder("exchange.rate.fetch.failure")
                .description("Number of failed exchange rate fetches")
                .register(meterRegistry);
                
        // Initialize timer
        this.exchangeRateFetchTimer = Timer.builder("exchange.rate.fetch.duration")
                .description("Time taken to fetch exchange rates")
                .register(meterRegistry);
                
        log.info("Exchange rate metrics initialized");
    }

    /**
     * Record a successful exchange rate fetch.
     */
    public void recordFetchSuccess() {
        exchangeRateFetchSuccessCounter.increment();
    }

    /**
     * Record a failed exchange rate fetch.
     */
    public void recordFetchFailure() {
        exchangeRateFetchFailureCounter.increment();
    }

    /**
     * Record the duration of an exchange rate fetch operation.
     */
    public void recordFetchDuration(long durationMillis) {
        exchangeRateFetchTimer.record(durationMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Record a provider-specific metric.
     */
    public void recordProviderMetric(String providerName, boolean success) {
        Counter.builder("provider.fetch")
                .tag("provider", providerName)
                .tag("status", success ? "success" : "failure")
                .description("Exchange rate provider fetch attempts")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Get the meter registry for custom metric creation.
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
