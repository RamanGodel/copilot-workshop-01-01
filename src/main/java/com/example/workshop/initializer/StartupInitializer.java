package com.example.workshop.initializer;

import com.example.workshop.service.CurrencyService;
import com.example.workshop.service.ExchangeRateRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;

/**
 * Initialization tasks that run when the application is ready.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupInitializer {

    private final DataSource dataSource;
    private final CurrencyService currencyService;
    private final ExchangeRateRefreshService refreshService;

    /**
     * Executes startup tasks when the application is fully ready.
     * This includes verifying database connectivity, loading initial currencies,
     * and fetching initial exchange rates.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application is ready. Starting initialization tasks...");
        Instant start = Instant.now();
        
        try {
            // Task 1: Verify database connectivity
            verifyDatabaseConnectivity();
            
            // Task 2: Initialize default currencies if needed
            initializeDefaultCurrencies();
            
            // Task 3: Load initial exchange rates
            loadInitialExchangeRates();
            
            Duration elapsed = Duration.between(start, Instant.now());
            log.info("Startup initialization completed successfully in {} ms", elapsed.toMillis());
        } catch (Exception e) {
            Duration elapsed = Duration.between(start, Instant.now());
            log.error("Startup initialization failed after {} ms: {}", elapsed.toMillis(), e.getMessage(), e);
        }
    }

    /**
     * Verifies that the application can connect to the database.
     */
    private void verifyDatabaseConnectivity() {
        log.info("Verifying database connectivity...");
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5);
            if (isValid) {
                log.info("Database connectivity verified successfully");
            } else {
                log.warn("Database connection is not valid");
            }
        } catch (Exception e) {
            log.error("Failed to verify database connectivity: {}", e.getMessage(), e);
            throw new RuntimeException("Database connectivity check failed", e);
        }
    }

    /**
     * Initializes default currencies if the database is empty.
     */
    private void initializeDefaultCurrencies() {
        log.info("Checking if default currencies need to be initialized...");
        
        int currencyCount = currencyService.getAllCurrencies().size();
        
        if (currencyCount == 0) {
            log.info("No currencies found. Initializing default currencies...");
            
            String[] defaultCurrencies = {"USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD"};
            
            for (String currencyCode : defaultCurrencies) {
                try {
                    currencyService.addCurrency(currencyCode);
                    log.info("Added default currency: {}", currencyCode);
                } catch (Exception e) {
                    log.warn("Failed to add default currency {}: {}", currencyCode, e.getMessage());
                }
            }
            
            int newCount = currencyService.getAllCurrencies().size();
            log.info("Initialized {} default currencies", newCount);
        } else {
            log.info("Found {} existing currencies. Skipping default initialization", currencyCount);
        }
    }

    /**
     * Loads initial exchange rates from external providers on application startup.
     */
    private void loadInitialExchangeRates() {
        log.info("Loading initial exchange rates...");
        Instant start = Instant.now();
        
        try {
            ExchangeRateRefreshService.RefreshSummary summary = refreshService.refreshAll();
            
            Duration elapsed = Duration.between(start, Instant.now());
            log.info("Initial exchange rates loaded in {} ms. Summary: {} currencies in system, " +
                    "{} processed, {} providers with data, {} rates saved, {} failures",
                    elapsed.toMillis(),
                    summary.getCurrenciesInSystem(),
                    summary.getCurrenciesProcessed(),
                    summary.getProvidersWithData(),
                    summary.getRatesSaved(),
                    summary.getFailures().size());
            
            if (!summary.getFailures().isEmpty()) {
                log.warn("Failures during initial rate loading:");
                summary.getFailures().forEach(log::warn);
            }
        } catch (Exception e) {
            Duration elapsed = Duration.between(start, Instant.now());
            log.error("Failed to load initial exchange rates after {} ms: {}", elapsed.toMillis(), e.getMessage(), e);
            // Don't throw - let the application start even if initial rate loading fails
        }
    }
}
