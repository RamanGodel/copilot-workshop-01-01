package com.example.workshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for exchange rate conversion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing exchange rate conversion result")
public class ExchangeRateResponseDTO {

    @Schema(description = "Original amount", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Source currency code", example = "USD")
    private String from;

    @Schema(description = "Target currency code", example = "EUR")
    private String to;

    @Schema(description = "Converted amount", example = "85.50")
    private BigDecimal result;

    @Schema(description = "Exchange rate used", example = "0.855")
    private BigDecimal rate;

    @Schema(description = "Timestamp of the exchange rate", example = "2025-12-10T14:30:00")
    private LocalDateTime timestamp;
}

