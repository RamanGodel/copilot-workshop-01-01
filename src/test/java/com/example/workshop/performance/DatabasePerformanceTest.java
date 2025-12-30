package com.example.workshop.performance;

import com.example.workshop.model.Currency;
import com.example.workshop.model.ExchangeRate;
import com.example.workshop.repository.CurrencyRepository;
import com.example.workshop.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance tests for database operations.
 * Verifies that queries execute efficiently and pagination works correctly.
 */
@SpringBootTest
@DisplayName("Database Performance Tests")
class DatabasePerformanceTest {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private Currency usd;
    private Currency eur;
    private Currency gbp;

    @BeforeEach
    void setUp() {
        // Clean up exchange rates only (keep currencies from Liquibase)
        exchangeRateRepository.deleteAll();
        exchangeRateRepository.flush(); // Ensure deletion is committed

        // Get or create test currencies (Liquibase may have already created them)
        usd = currencyRepository.findByCode("USD")
            .orElseGet(() -> currencyRepository.save(Currency.builder().code("USD").name("US Dollar").build()));
        eur = currencyRepository.findByCode("EUR")
            .orElseGet(() -> currencyRepository.save(Currency.builder().code("EUR").name("Euro").build()));
        gbp = currencyRepository.findByCode("GBP")
            .orElseGet(() -> currencyRepository.save(Currency.builder().code("GBP").name("British Pound").build()));

        // Create test exchange rates - 1000 records
        List<ExchangeRate> rates = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(100);

        for (int i = 0; i < 1000; i++) {
            ExchangeRate rate = new ExchangeRate();
            rate.setBaseCurrency(usd);
            rate.setTargetCurrency(eur);
            rate.setRate(BigDecimal.valueOf(0.85 + (i % 10) * 0.01));
            rate.setTimestamp(baseTime.plusHours(i));
            rates.add(rate);
        }

        exchangeRateRepository.saveAll(rates);
    }

    @Test
    @DisplayName("Large dataset pagination should be efficient")
    @Transactional(readOnly = true)
    void testPaginationPerformance() {
        List<Long> pageTimes = new ArrayList<>();

        // Test first 10 pages
        for (int page = 0; page < 10; page++) {
            Pageable pageable = PageRequest.of(page, 50);
            
            Instant start = Instant.now();
            Page<ExchangeRate> result = exchangeRateRepository.findAll(pageable);
            Duration queryTime = Duration.between(start, Instant.now());
            
            pageTimes.add(queryTime.toMillis());
            
            assertThat(result.getContent()).hasSize(50);
        }

        double average = pageTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long max = pageTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        System.out.println("Pagination Performance (1000 records, 50 per page):");
        System.out.println("  Average page query time: " + String.format("%.2f", average) + "ms");
        System.out.println("  Max page query time: " + max + "ms");

        // Each page query should complete in under 100ms
        assertThat(average).isLessThan(100);
        assertThat(max).isLessThan(200);
    }

    @Test
    @DisplayName("Query by currency should use indexes efficiently")
    @Transactional(readOnly = true)
    void testIndexedQueryPerformance() {
        List<Long> queryTimes = new ArrayList<>();

        // Run query 50 times
        for (int i = 0; i < 50; i++) {
            Instant start = Instant.now();
            
            List<ExchangeRate> rates = exchangeRateRepository.findByBaseCurrency(usd);
            
            Duration queryTime = Duration.between(start, Instant.now());
            queryTimes.add(queryTime.toMillis());
            
            assertThat(rates).hasSize(1000);
        }

        double average = queryTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long max = queryTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        System.out.println("Indexed Query Performance (find by base currency):");
        System.out.println("  Average query time: " + String.format("%.2f", average) + "ms");
        System.out.println("  Max query time: " + max + "ms");

        // Indexed query should be fast
        assertThat(average).isLessThan(100);
    }

    @Test
    @DisplayName("Latest rates query should be optimized")
    @Transactional(readOnly = true)
    void testLatestRatesQueryPerformance() {
        List<Long> queryTimes = new ArrayList<>();

        // Run query 30 times
        for (int i = 0; i < 30; i++) {
            Instant start = Instant.now();
            
            List<ExchangeRate> rates = exchangeRateRepository.findLatestRatesForBaseCurrency(usd.getId());
            
            Duration queryTime = Duration.between(start, Instant.now());
            queryTimes.add(queryTime.toMillis());
            
            assertThat(rates).isNotEmpty();
        }

        double average = queryTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long max = queryTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        System.out.println("Latest Rates Query Performance:");
        System.out.println("  Average query time: " + String.format("%.2f", average) + "ms");
        System.out.println("  Max query time: " + max + "ms");

        // Complex query with grouping should still be reasonably fast
        assertThat(average).isLessThan(150);
    }

    @Test
    @DisplayName("Bulk insert performance should be acceptable")
    @Transactional
    void testBulkInsertPerformance() {
        // Create 500 new rates
        List<ExchangeRate> newRates = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now();

        for (int i = 0; i < 500; i++) {
            ExchangeRate rate = new ExchangeRate();
            rate.setBaseCurrency(gbp);
            rate.setTargetCurrency(eur);
            rate.setRate(BigDecimal.valueOf(1.15 + (i % 10) * 0.01));
            rate.setTimestamp(baseTime.plusMinutes(i));
            newRates.add(rate);
        }

        // Measure bulk insert time
        Instant start = Instant.now();
        exchangeRateRepository.saveAll(newRates);
        Duration insertTime = Duration.between(start, Instant.now());

        System.out.println("Bulk Insert Performance (500 records):");
        System.out.println("  Total time: " + insertTime.toMillis() + "ms");
        System.out.println("  Average per record: " + 
            String.format("%.2f", (double)insertTime.toMillis()/500) + "ms");

        // Bulk insert should be efficient
        assertThat(insertTime.toMillis()).isLessThan(2000); // Under 2 seconds for 500 records
        
        // Verify all records were inserted
        long count = exchangeRateRepository.count();
        assertThat(count).isEqualTo(1500); // 1000 from setUp + 500 new
    }

    @Test
    @DisplayName("Currency lookup by code should be instant")
    @Transactional(readOnly = true)
    void testCurrencyLookupPerformance() {
        List<Long> lookupTimes = new ArrayList<>();

        // Perform 100 lookups
        for (int i = 0; i < 100; i++) {
            Instant start = Instant.now();
            
            currencyRepository.findByCode("USD");
            
            Duration lookupTime = Duration.between(start, Instant.now());
            lookupTimes.add(lookupTime.toMillis());
        }

        double average = lookupTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long max = lookupTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        System.out.println("Currency Lookup Performance (by code):");
        System.out.println("  Average lookup time: " + String.format("%.2f", average) + "ms");
        System.out.println("  Max lookup time: " + max + "ms");

        // Single record lookup should be extremely fast
        assertThat(average).isLessThan(10);
    }
}
