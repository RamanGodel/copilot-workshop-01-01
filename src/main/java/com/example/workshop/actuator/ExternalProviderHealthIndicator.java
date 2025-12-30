package com.example.workshop.actuator;

import com.example.workshop.provider.ExchangeRateProvider;
import com.example.workshop.provider.ProviderUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom health indicator for external exchange rate providers.
 * Checks the availability of all configured exchange rate providers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalProviderHealthIndicator implements HealthIndicator {

    private final List<ExchangeRateProvider> providers;

    @Override
    public Health health() {
        if (providers == null || providers.isEmpty()) {
            log.warn("No exchange rate providers configured");
            return Health.unknown()
                    .withDetail("message", "No providers configured")
                    .build();
        }

        Map<String, String> providerStatuses = new HashMap<>();
        int availableCount = 0;
        int totalCount = providers.size();

        for (ExchangeRateProvider provider : providers) {
            try {
                long startTime = System.currentTimeMillis();
                var result = provider.fetchLatestRates("USD");
                long responseTime = System.currentTimeMillis() - startTime;
                
                if (result.isPresent()) {
                    providerStatuses.put(provider.getProviderName(), "UP (" + responseTime + "ms)");
                    availableCount++;
                    log.debug("Provider {} is healthy ({}ms)", provider.getProviderName(), responseTime);
                } else {
                    providerStatuses.put(provider.getProviderName(), "NO_DATA");
                    log.warn("Provider {} returned no data", provider.getProviderName());
                }
            } catch (ProviderUnavailableException e) {
                providerStatuses.put(provider.getProviderName(), "DOWN: " + e.getMessage());
                log.warn("Provider {} is down: {}", provider.getProviderName(), e.getMessage());
            } catch (Exception e) {
                providerStatuses.put(provider.getProviderName(), "ERROR: " + e.getClass().getSimpleName());
                log.error("Unexpected error checking provider {}", provider.getProviderName(), e);
            }
        }

        var healthBuilder = Health.up()
                .withDetail("totalProviders", totalCount)
                .withDetail("availableProviders", availableCount)
                .withDetail("providers", providerStatuses);

        // If no providers are available, mark as down
        if (availableCount == 0) {
            return healthBuilder
                    .down()
                    .withDetail("error", "All providers are unavailable")
                    .build();
        }

        // If less than half of providers are down, mark the overall health as degraded
        if (availableCount < totalCount / 2.0) {
            return healthBuilder
                    .status("DEGRADED")
                    .withDetail("warning", "Less than half of providers are available")
                    .build();
        }

        return healthBuilder.build();
    }
}
