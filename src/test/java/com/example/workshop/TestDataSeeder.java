package com.example.workshop;

import com.example.workshop.model.Currency;
import com.example.workshop.model.ExchangeRate;
import com.example.workshop.repository.CurrencyRepository;
import com.example.workshop.repository.ExchangeRateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Test-only data seeder that populates the in-memory H2 database with common currencies and sample rates.
 */
@Component
@Profile("test")
public class TestDataSeeder implements CommandLineRunner {

    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public TestDataSeeder(CurrencyRepository currencyRepository,
                          ExchangeRateRepository exchangeRateRepository) {
        this.currencyRepository = currencyRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Map<String, String> currencies = new HashMap<>();
        currencies.put("USD", "US Dollar");
        currencies.put("EUR", "Euro");
        currencies.put("GBP", "British Pound");
        currencies.put("JPY", "Japanese Yen");
        currencies.put("CHF", "Swiss Franc");
        currencies.put("CAD", "Canadian Dollar");
        currencies.put("AUD", "Australian Dollar");
        // Note: PLN intentionally omitted so tests that add PLN can run without conflict
        currencies.put("CNY", "Chinese Yuan");
        currencies.put("INR", "Indian Rupee");

        // Insert currencies if not present
        currencies.forEach((code, name) -> {
            if (!currencyRepository.existsByCode(code)) {
                Currency c = Currency.builder().code(code).name(name).build();
                currencyRepository.save(c);
            }
        });

        // Insert a few exchange rates for USD->EUR and USD->GBP
        Currency usd = currencyRepository.findByCode("USD").orElseThrow();
        Currency eur = currencyRepository.findByCode("EUR").orElseThrow();
        Currency gbp = currencyRepository.findByCode("GBP").orElseThrow();

        LocalDateTime now = LocalDateTime.now();

        ExchangeRate r1 = ExchangeRate.builder()
                .baseCurrency(usd)
                .targetCurrency(eur)
                .rate(BigDecimal.valueOf(0.85))
                .timestamp(now.minusDays(9))
                .build();

        ExchangeRate r2 = ExchangeRate.builder()
                .baseCurrency(usd)
                .targetCurrency(eur)
                .rate(BigDecimal.valueOf(0.86))
                .timestamp(now.minusDays(5))
                .build();

        ExchangeRate r3 = ExchangeRate.builder()
                .baseCurrency(usd)
                .targetCurrency(eur)
                .rate(BigDecimal.valueOf(0.88))
                .timestamp(now.minusDays(1))
                .build();

        ExchangeRate r4 = ExchangeRate.builder()
                .baseCurrency(usd)
                .targetCurrency(gbp)
                .rate(BigDecimal.valueOf(0.75))
                .timestamp(now.minusDays(1))
                .build();

        exchangeRateRepository.save(r1);
        exchangeRateRepository.save(r2);
        exchangeRateRepository.save(r3);
        exchangeRateRepository.save(r4);
    }
}
