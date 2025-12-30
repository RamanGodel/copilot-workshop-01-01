package com.example.workshop.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for caching using Caffeine.
 * Defines cache names, TTL, and automatic cache eviction.
 */
@Slf4j
@Configuration
public class CacheConfig {

    /**
     * Cache name for storing list of all currencies
     */
    public static final String CURRENCIES_CACHE = "currencies";

    /**
     * Cache name for storing individual currency lookups
     */
    public static final String CURRENCY_BY_CODE_CACHE = "currencyByCode";

    /**
     * Cache name for storing latest exchange rates
     */
    public static final String LATEST_RATES_CACHE = "latestRates";

    /**
     * Configure Caffeine cache manager with specific cache settings.
     *
     * @return configured cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CURRENCIES_CACHE,
                CURRENCY_BY_CODE_CACHE,
                LATEST_RATES_CACHE
        );

        cacheManager.setCaffeine(caffeineCacheBuilder());
        log.info("Caffeine cache manager configured with caches: {}, {}, {}",
                CURRENCIES_CACHE, CURRENCY_BY_CODE_CACHE, LATEST_RATES_CACHE);

        return cacheManager;
    }

    /**
     * Build Caffeine cache with specific configuration.
     * - Initial capacity: 100 entries
     * - Maximum size: 500 entries
     * - Expire after write: 1 hour
     * - Record statistics for monitoring
     *
     * @return Caffeine cache builder
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats();
    }

    /**
     * Scheduled task to evict all caches every hour.
     * This ensures that cached data doesn't become stale.
     * Runs at the top of every hour (0 minutes, 0 seconds).
     */
    @Scheduled(cron = "0 0 * * * *")
    @CacheEvict(value = {CURRENCIES_CACHE, CURRENCY_BY_CODE_CACHE, LATEST_RATES_CACHE}, allEntries = true)
    public void evictAllCachesScheduled() {
        log.info("Scheduled cache eviction: All caches evicted");
    }
}
