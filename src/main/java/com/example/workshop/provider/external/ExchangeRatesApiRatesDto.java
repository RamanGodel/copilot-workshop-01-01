package com.example.workshop.provider.external;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Minimal DTO for exchangeratesapi.io "latest" endpoint.
 *
 * Example:
 * {
 *   "success": true,
 *   "timestamp": 1735689600,
 *   "base": "USD",
 *   "date": "2025-01-01",
 *   "rates": {"EUR": 0.9}
 * }
 */
@Data
public class ExchangeRatesApiRatesDto {

    private boolean success;
    private Long timestamp;
    private String base;
    private Map<String, BigDecimal> rates;

    private ApiError error;

    @Data
    public static class ApiError {
        private Integer code;
        private String type;
        private String info;
    }
}

