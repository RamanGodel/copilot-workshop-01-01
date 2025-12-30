package com.example.workshop.actuator;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom actuator endpoint for monitoring cache statistics.
 * Provides detailed statistics about cache performance including hit/miss rates,
 * eviction counts, and current cache sizes.
 *
 * Accessible at: /actuator/cacheStats
 */
@Component
@Endpoint(id = "cacheStats")
@RequiredArgsConstructor
public class CacheStatsEndpoint {

    private final CacheManager cacheManager;

    /**
     * Get statistics for all configured caches.
     *
     * @return Map containing cache statistics for each cache
     */
    @ReadOperation
    public Map<String, Object> cacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache caffeineCache) {
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = 
                        caffeineCache.getNativeCache();
                CacheStats cacheStats = nativeCache.stats();
                
                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("hitCount", cacheStats.hitCount());
                cacheInfo.put("missCount", cacheStats.missCount());
                cacheInfo.put("hitRate", String.format("%.2f%%", cacheStats.hitRate() * 100));
                cacheInfo.put("missRate", String.format("%.2f%%", cacheStats.missRate() * 100));
                cacheInfo.put("evictionCount", cacheStats.evictionCount());
                cacheInfo.put("loadSuccessCount", cacheStats.loadSuccessCount());
                cacheInfo.put("loadFailureCount", cacheStats.loadFailureCount());
                cacheInfo.put("totalLoadTime", cacheStats.totalLoadTime());
                cacheInfo.put("estimatedSize", nativeCache.estimatedSize());
                
                stats.put(cacheName, cacheInfo);
            }
        });
        
        // Add summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCaches", cacheManager.getCacheNames().size());
        summary.put("cacheNames", cacheManager.getCacheNames());
        stats.put("summary", summary);
        
        return stats;
    }
}
