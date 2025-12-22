package com.example.workshop.provider.mock;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO matching mock-exchange-service-1 response.
 */
@Data
public class MockProvider1RatesDto {
    private String base;
    private String timestamp;
    private Map<String, BigDecimal> rates;
}

