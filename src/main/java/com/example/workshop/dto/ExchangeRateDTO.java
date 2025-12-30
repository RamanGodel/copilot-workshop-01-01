package com.example.workshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for exchange rate information.
 * Used to return exchange rate details without exposing the full entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Exchange rate information")
public class ExchangeRateDTO {

    @Schema(description = "Exchange rate ID", example = "1")
    private Long id;

    @Schema(description = "Base currency code", example = "USD")
    private String baseCurrencyCode;

    @Schema(description = "Base currency name", example = "United States Dollar")
    private String baseCurrencyName;

    @Schema(description = "Target currency code", example = "EUR")
    private String targetCurrencyCode;

    @Schema(description = "Target currency name", example = "Euro")
    private String targetCurrencyName;

    @Schema(description = "Exchange rate value", example = "0.85")
    private BigDecimal rate;

    @Schema(description = "Timestamp when the rate was recorded", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;
}
