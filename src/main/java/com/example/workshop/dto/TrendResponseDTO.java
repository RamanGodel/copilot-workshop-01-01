package com.example.workshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for currency trend analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing currency trend information")
public class TrendResponseDTO {

    @Schema(description = "Source currency code", example = "USD")
    private String from;

    @Schema(description = "Target currency code", example = "EUR")
    private String to;

    @Schema(description = "Period analyzed", example = "10D")
    private String period;

    @Schema(description = "Change percentage over the period", example = "2.5")
    private BigDecimal changePercentage;
}

