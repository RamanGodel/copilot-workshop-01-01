package com.example.workshop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "providers")
public class ProvidersProperties {

    /**
     * Provider precedence order. First provider returning non-empty rates wins.
     */
    private List<String> order = new ArrayList<>();

    /**
     * Timeout applied to each provider invocation.
     */
    private Duration callTimeout = Duration.ofSeconds(2);

    /**
     * Retry attempts for a provider call.
     */
    private int retryMaxAttempts = 2;
}
