package com.example.workshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for exchange rate conversion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for currency exchange rate conversion")
public class ExchangeRateRequestDTO {

    @Schema(description = "Amount to convert", example = "100.00", required = true)
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Schema(description = "Source currency code (ISO 4217)", example = "USD", required = true)
    @NotBlank(message = "Source currency code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    private String from;

    @Schema(description = "Target currency code (ISO 4217)", example = "EUR", required = true)
    @NotBlank(message = "Target currency code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    private String to;
}

