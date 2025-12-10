package com.example.workshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Error response DTO for API error handling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response for API failures")
public class ErrorResponseDTO {

    @Schema(description = "Timestamp when the error occurred", example = "2025-12-10T14:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type", example = "Bad Request")
    private String error;

    @Schema(description = "Detailed error message", example = "Currency code must be 3 uppercase letters")
    private String message;

    @Schema(description = "API path where the error occurred", example = "/api/v1/currencies/exchange-rates")
    private String path;
}

