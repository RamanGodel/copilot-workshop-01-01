package com.example.workshop.service;

import com.example.workshop.model.Currency;
import com.example.workshop.provider.ExchangeRateProviderAggregator;
import com.example.workshop.provider.ProviderRatesResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateRefreshService {

    private final CurrencyService currencyService;
    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateProviderAggregator aggregator;

    /**
     * Refreshes rates for all base currencies.
     *
     * Important: Do not run the whole refresh in a single transaction.
     * Individual save failures (e.g., missing target currency) should not mark the whole
     * refresh as rollback-only.
     */
    public RefreshSummary refreshAll() {
        List<Currency> currencies = currencyService.getAllCurrencies();

        int currenciesProcessed = 0;
        int ratesSaved = 0;
        int providersWithData = 0;

        List<String> failures = new ArrayList<>();

        for (Currency base : currencies) {
            currenciesProcessed++;
            ExchangeRateProviderAggregator.AggregationResult result = aggregator.fetchLatestRates(base.getCode());

            if (!result.hasData()) {
                continue;
            }

            providersWithData++;
            ProviderRatesResponse chosen = result.getChosen();
            LocalDateTime timestamp = chosen.getTimestamp() == null ? LocalDateTime.now() : chosen.getTimestamp();

            for (Map.Entry<String, BigDecimal> e : chosen.getRates().entrySet()) {
                String targetCode = e.getKey();
                BigDecimal rate = e.getValue();

                // Skip self-rate and null values.
                if (targetCode == null || rate == null) {
                    continue;
                }

                if (base.getCode().equalsIgnoreCase(targetCode)) {
                    continue;
                }

                try {
                    // Persist each rate in its own transaction. This prevents a single failure from
                    // rolling back the whole refresh operation.
                    exchangeRateService.saveExchangeRate(
                        base.getCode(),
                        targetCode.toUpperCase(),
                        rate,
                        timestamp
                    );
                    ratesSaved++;
                } catch (RuntimeException ex) {
                    failures.add("Failed to save rate " + base.getCode() + "->" + targetCode + ": " + ex.getMessage());
                }
            }
        }

        return new RefreshSummary(currencies.size(), currenciesProcessed, providersWithData, ratesSaved, failures);
    }

    @Value
    public static class RefreshSummary {
        int currenciesInSystem;
        int currenciesProcessed;
        int providersWithData;
        int ratesSaved;
        List<String> failures;
    }
}
