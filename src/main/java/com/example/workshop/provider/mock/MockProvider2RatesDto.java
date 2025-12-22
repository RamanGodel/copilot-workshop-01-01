package com.example.workshop.provider.mock;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO matching mock-exchange-service-2 response.
 */
@Data
public class MockProvider2RatesDto {

    private boolean success;
    private String source;
    private long lastUpdate;
    private List<RateData> data;

    @Data
    public static class RateData {
        private String currencyCode;
        private BigDecimal exchangeRate;
        private String description;
    }
}

