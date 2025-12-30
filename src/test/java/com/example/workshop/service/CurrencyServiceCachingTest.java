package com.example.workshop.service;

import com.example.workshop.config.CacheConfig;
import com.example.workshop.model.Currency;
import com.example.workshop.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Integration test for caching functionality in CurrencyService.
 * Tests that @Cacheable and @CacheEvict annotations work correctly.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.cache.type=caffeine",
    "logging.level.com.example.workshop.service.CurrencyService=DEBUG"
})
class CurrencyServiceCachingTest {

    @Autowired
    private CurrencyService currencyService;

    @SpyBean
    private CurrencyRepository currencyRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
        
        // Reset mock invocation counts
        clearInvocations(currencyRepository);
    }

    @Test
    void testGetAllCurrencies_UsesCache() {
        // First call - should hit the database
        List<Currency> currencies1 = currencyService.getAllCurrencies();
        assertThat(currencies1).isNotEmpty();
        verify(currencyRepository, times(1)).findAll();

        // Second call - should use cache, no database hit
        List<Currency> currencies2 = currencyService.getAllCurrencies();
        assertThat(currencies2).isNotEmpty();
        assertThat(currencies2).hasSize(currencies1.size());
        verify(currencyRepository, times(1)).findAll(); // Still only 1 call

        // Third call - should still use cache
        List<Currency> currencies3 = currencyService.getAllCurrencies();
        assertThat(currencies3).hasSize(currencies1.size());
        verify(currencyRepository, times(1)).findAll(); // Still only 1 call
    }

    @Test
    void testFindByCode_UsesCache() {
        String testCode = "USD";

        // First call - should hit the database
        Optional<Currency> currency1 = currencyService.findByCode(testCode);
        assertThat(currency1).isPresent();
        verify(currencyRepository, times(1)).findByCode(testCode);

        // Second call - should use cache
        Optional<Currency> currency2 = currencyService.findByCode(testCode);
        assertThat(currency2).isPresent();
        assertThat(currency2.get().getCode()).isEqualTo(currency1.get().getCode());
        verify(currencyRepository, times(1)).findByCode(testCode); // Still only 1 call

        // Different code - should hit database
        currencyService.findByCode("EUR");
        verify(currencyRepository, times(1)).findByCode("EUR");
    }

    @Test
    void testAddCurrency_EvictsCache() {
        // First, populate the cache
        List<Currency> currencies1 = currencyService.getAllCurrencies();
        assertThat(currencies1).isNotEmpty();
        verify(currencyRepository, times(1)).findAll();

        // Verify cache is being used
        currencyService.getAllCurrencies();
        verify(currencyRepository, times(1)).findAll(); // Still 1 call

        // Add a new currency - should evict cache
        try {
            currencyService.addCurrency("TST", "Test Currency");
        } catch (Exception e) {
            // May fail if currency already exists - that's okay for this test
        }

        // Next call should hit database again because cache was evicted
        currencyService.getAllCurrencies();
        verify(currencyRepository, times(2)).findAll(); // Now 2 calls
    }

    @Test
    void testCacheConfiguration() {
        // Verify all expected caches are configured
        assertThat(cacheManager.getCacheNames())
                .contains(
                        CacheConfig.CURRENCIES_CACHE,
                        CacheConfig.CURRENCY_BY_CODE_CACHE,
                        CacheConfig.LATEST_RATES_CACHE
                );
    }

    @Test
    void testCacheSeparation() {
        // Cache for getAllCurrencies should be separate from findByCode cache
        
        // Populate getAllCurrencies cache
        currencyService.getAllCurrencies();
        verify(currencyRepository, times(1)).findAll();

        // Populate findByCode cache for USD
        currencyService.findByCode("USD");
        verify(currencyRepository, times(1)).findByCode("USD");

        // Call getAllCurrencies again - should use cache
        currencyService.getAllCurrencies();
        verify(currencyRepository, times(1)).findAll(); // Still 1 call

        // Call findByCode("USD") again - should use cache
        currencyService.findByCode("USD");
        verify(currencyRepository, times(1)).findByCode("USD"); // Still 1 call
    }
}
