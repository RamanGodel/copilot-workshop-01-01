package com.example.workshop.provider;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Normalized representation of a provider response.
 */
@Value
@Builder
public class ProviderRatesResponse {

    String provider;
    String base;
    LocalDateTime timestamp;

    /**
     * Map of target currency code -> exchange rate.
     */
    @Builder.Default
    Map<String, BigDecimal> rates = Collections.emptyMap();
}

