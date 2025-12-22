package com.example.workshop.provider.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Minimal DTO for fixer.io latest rates endpoint.
 *
 * Example:
 * {
 *   "success": true,
 *   "timestamp": 1519296206,
 *   "base": "EUR",
 *   "date": "2025-01-01",
 *   "rates": {"USD": 1.23}
 * }
 */
@Data
public class FixerIoRatesDto {

    private boolean success;
    private Long timestamp;
    private String base;

    @JsonProperty("rates")
    private Map<String, BigDecimal> rates;

    // Some fixer responses include these on error
    private FixerError error;

    @Data
    public static class FixerError {
        private Integer code;
        private String type;
        private String info;
    }
}

