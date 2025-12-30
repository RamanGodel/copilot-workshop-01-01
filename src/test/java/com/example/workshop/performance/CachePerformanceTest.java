package com.example.workshop.performance;

import com.example.workshop.model.Currency;
import com.example.workshop.repository.CurrencyRepository;
import com.example.workshop.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance tests for cache behavior.
 * Verifies that caching significantly improves response times.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.cache.type=caffeine",
    "spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=300s"
})
@DisplayName("Cache Performance Tests")
class CachePerformanceTest {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CurrencyRepository currencyRepository;

    @BeforeEach
    void setUp() {
        // Clear all caches
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });

        // Ensure test data exists
        if (currencyRepository.count() == 0) {
            currencyRepository.save(Currency.builder().code("USD").name("US Dollar").build());
            currencyRepository.save(Currency.builder().code("EUR").name("Euro").build());
            currencyRepository.save(Currency.builder().code("GBP").name("British Pound").build());
        }
    }

    @Test
    @DisplayName("Cache should reduce response time by at least 50%")
    void testCachePerformanceImprovement() {
        // Warm up - first call populates cache
        currencyService.getAllCurrencies();

        // Measure first uncached call
        cacheManager.getCache("currencies").clear();
        Instant start1 = Instant.now();
        currencyService.getAllCurrencies();
        Duration uncachedTime = Duration.between(start1, Instant.now());

        // Measure second cached call
        Instant start2 = Instant.now();
        currencyService.getAllCurrencies();
        Duration cachedTime = Duration.between(start2, Instant.now());

        System.out.println("Uncached call time: " + uncachedTime.toMillis() + "ms");
        System.out.println("Cached call time: " + cachedTime.toMillis() + "ms");
        System.out.println("Performance improvement: " + 
            String.format("%.1f%%", (1 - (double)cachedTime.toMillis()/uncachedTime.toMillis()) * 100));

        // Cache should be at least 50% faster (or cached time should be very small)
        assertThat(cachedTime.toMillis()).isLessThanOrEqualTo(uncachedTime.toMillis() / 2);
    }

    @Test
    @DisplayName("Multiple concurrent cache reads should be fast")
    void testConcurrentCacheReads() {
        // Populate cache
        currencyService.getAllCurrencies();

        // Measure 100 consecutive cached reads
        Instant start = Instant.now();
        for (int i = 0; i < 100; i++) {
            currencyService.getAllCurrencies();
        }
        Duration totalTime = Duration.between(start, Instant.now());

        System.out.println("100 cached reads total time: " + totalTime.toMillis() + "ms");
        System.out.println("Average time per cached read: " + totalTime.toMillis() / 100.0 + "ms");

        // 100 cached reads should complete in under 100ms (1ms avg per call)
        assertThat(totalTime.toMillis()).isLessThan(100);
    }

    @Test
    @DisplayName("Cache miss penalty should be acceptable")
    void testCacheMissPenalty() {
        List<Long> times = new ArrayList<>();

        // Measure 10 uncached calls
        for (int i = 0; i < 10; i++) {
            cacheManager.getCache("currencies").clear();
            
            Instant start = Instant.now();
            currencyService.getAllCurrencies();
            Duration callTime = Duration.between(start, Instant.now());
            
            times.add(callTime.toMillis());
        }

        // Calculate average
        double average = times.stream().mapToLong(Long::longValue).average().orElse(0);
        long max = times.stream().mapToLong(Long::longValue).max().orElse(0);

        System.out.println("Average uncached call time: " + average + "ms");
        System.out.println("Max uncached call time: " + max + "ms");

        // Cache miss should complete in under 100ms on average
        assertThat(average).isLessThan(100);
        // Max time should be under 200ms
        assertThat(max).isLessThan(200);
    }

    @Test
    @DisplayName("Cache eviction should not impact performance significantly")
    void testCacheEvictionPerformance() {
        // Populate cache
        currencyService.getAllCurrencies();

        // Measure time for cache eviction (by adding a new currency which evicts cache)
        Instant start = Instant.now();
        // We'll just clear the cache manually since addCurrency would persist data
        cacheManager.getCache("currencies").clear();
        Duration evictionTime = Duration.between(start, Instant.now());

        System.out.println("Cache eviction time: " + evictionTime.toMillis() + "ms");

        // Cache eviction should be fast (under 50ms)
        assertThat(evictionTime.toMillis()).isLessThan(50);

        // Verify cache can be repopulated
        var currencies = currencyService.getAllCurrencies();
        assertThat(currencies).isNotEmpty();
    }
}
