package com.example.workshop.dto;

import com.example.workshop.validation.ValidPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for currency trend analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for currency trend analysis over a period")
public class TrendRequestDTO {

    @Schema(description = "Source currency code (ISO 4217)", example = "USD", required = true)
    @NotBlank(message = "Source currency code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    private String from;

    @Schema(description = "Target currency code (ISO 4217)", example = "EUR", required = true)
    @NotBlank(message = "Target currency code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    private String to;

    @Schema(description = "Period for trend analysis (e.g., 12H, 10D, 3M, 1Y)", example = "10D", required = true)
    @NotBlank(message = "Period is required")
    @ValidPeriod
    private String period;
}

