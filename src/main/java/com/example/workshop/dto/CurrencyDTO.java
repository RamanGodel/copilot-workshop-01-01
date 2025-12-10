package com.example.workshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Currency information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Currency information")
public class CurrencyDTO {

    @Schema(description = "Unique identifier of the currency", example = "1")
    private Long id;

    @Schema(description = "ISO 4217 currency code", example = "USD", required = true)
    private String code;

    @Schema(description = "Full name of the currency", example = "US Dollar")
    private String name;
}

