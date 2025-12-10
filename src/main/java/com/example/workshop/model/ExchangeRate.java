package com.example.workshop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing an exchange rate between two currencies.
 * Contains the rate, base currency, target currency, and timestamp.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {

    private Long id;

    private Currency baseCurrency;

    private Currency targetCurrency;

    private BigDecimal rate;

    private LocalDateTime timestamp;
}

