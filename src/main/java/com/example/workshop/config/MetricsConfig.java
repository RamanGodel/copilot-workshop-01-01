package com.example.workshop.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for application metrics and monitoring.
 * Configures custom metrics, tags, and Prometheus integration.
 */
@Slf4j
@Configuration
public class MetricsConfig {

    /**
     * Customize meter registry with common tags.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            registry.config().commonTags(
                    List.of(
                            Tag.of("application", "currency-exchange-service"),
                            Tag.of("environment", getEnvironment())
                    )
            );
            log.info("Metrics registry configured with common tags");
        };
    }

    private String getEnvironment() {
        var env = System.getProperty("spring.profiles.active");
        return env != null ? env : "default";
    }
}
