package com.example.workshop.scheduler;

import com.example.workshop.service.ExchangeRateRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Scheduled tasks for automatic exchange rate updates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

    private final ExchangeRateRefreshService refreshService;

    /**
     * Scheduled task that runs every hour to fetch and update exchange rates for all currencies.
     * Cron expression: "0 0 * * * *" means at the start of every hour (second 0, minute 0).
     */
    @Scheduled(cron = "0 0 * * * *")
    public void refreshExchangeRates() {
        log.info("Starting scheduled exchange rate refresh...");
        Instant start = Instant.now();
        
        try {
            ExchangeRateRefreshService.RefreshSummary summary = refreshService.refreshAll();
            
            Duration elapsed = Duration.between(start, Instant.now());
            log.info("Scheduled refresh completed in {} ms. Summary: {} currencies in system, " +
                    "{} processed, {} providers with data, {} rates saved, {} failures",
                    elapsed.toMillis(),
                    summary.getCurrenciesInSystem(),
                    summary.getCurrenciesProcessed(),
                    summary.getProvidersWithData(),
                    summary.getRatesSaved(),
                    summary.getFailures().size());
            
            if (!summary.getFailures().isEmpty()) {
                log.warn("Failures during scheduled refresh:");
                summary.getFailures().forEach(log::warn);
            }
        } catch (Exception e) {
            Duration elapsed = Duration.between(start, Instant.now());
            log.error("Scheduled refresh failed after {} ms: {}", elapsed.toMillis(), e.getMessage(), e);
        }
    }
}
