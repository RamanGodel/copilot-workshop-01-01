package com.example.workshop.controller;

import com.example.workshop.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST Controller for currency exchange rate operations.
 * Provides endpoints for managing currencies, fetching exchange rates, and analyzing trends.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/currencies")
@Tag(name = "Currency Exchange", description = "APIs for currency exchange rates and trends")
public class CurrencyController {

    // In-memory storage for stub implementation
    private final Set<String> currencies = ConcurrentHashMap.newKeySet();

    public CurrencyController() {
        // Initialize with some default currencies
        currencies.addAll(Arrays.asList("USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD"));
    }

    @Operation(
        summary = "Get all currencies",
        description = "Retrieves a list of all available currencies in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved currency list",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CurrencyDTO.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<List<CurrencyDTO>> getAllCurrencies() {
        log.info("Fetching all currencies");

        List<CurrencyDTO> currencyList = currencies.stream()
            .sorted()
            .map(code -> CurrencyDTO.builder()
                .id((long) code.hashCode())
                .code(code)
                .name(getCurrencyName(code))
                .build())
            .toList();

        return ResponseEntity.ok(currencyList);
    }

    @Operation(
        summary = "Add a new currency",
        description = "Adds a new currency to the system by its ISO 4217 code"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Currency successfully added",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CurrencyDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid currency code",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Currency already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        )
    })
    @PostMapping
    public ResponseEntity<CurrencyDTO> addCurrency(
        @Parameter(description = "ISO 4217 currency code (3 uppercase letters)", example = "PLN")
        @RequestParam String currency
    ) {
        log.info("Adding currency: {}", currency);

        // Validate currency code format before converting
        if (!currency.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Currency code must be 3 uppercase letters");
        }

        String currencyCode = currency.toUpperCase();

        // Check if already exists
        if (currencies.contains(currencyCode)) {
            log.warn("Currency already exists: {}", currencyCode);
            throw new IllegalStateException("Currency " + currencyCode + " already exists");
        }

        currencies.add(currencyCode);

        CurrencyDTO dto = CurrencyDTO.builder()
            .id((long) currencyCode.hashCode())
            .code(currencyCode)
            .name(getCurrencyName(currencyCode))
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(
        summary = "Refresh exchange rates",
        description = "Triggers a manual refresh of exchange rates from external providers"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exchange rates refreshed successfully"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Failed to refresh exchange rates",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshExchangeRates() {
        log.info("Refreshing exchange rates for all currencies");

        // Stub implementation - simulate refresh
        Map<String, String> response = new HashMap<>();
        response.put("message", "Exchange rates refreshed successfully");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("currenciesProcessed", String.valueOf(currencies.size()));

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get exchange rate",
        description = "Calculates the exchange rate and converts an amount from one currency to another"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Exchange rate calculated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ExchangeRateResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Currency not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        )
    })
    @GetMapping("/exchange-rates")
    public ResponseEntity<ExchangeRateResponseDTO> getExchangeRate(
        @Valid @ModelAttribute ExchangeRateRequestDTO request
    ) {
        log.info("Getting exchange rate from {} to {} for amount {}",
            request.getFrom(), request.getTo(), request.getAmount());

        // Validate currencies exist
        if (!currencies.contains(request.getFrom())) {
            throw new IllegalArgumentException("Source currency not found: " + request.getFrom());
        }
        if (!currencies.contains(request.getTo())) {
            throw new IllegalArgumentException("Target currency not found: " + request.getTo());
        }

        // Stub implementation - generate mock exchange rate
        BigDecimal rate = generateStubExchangeRate(request.getFrom(), request.getTo());
        BigDecimal result = request.getAmount().multiply(rate);

        ExchangeRateResponseDTO response = ExchangeRateResponseDTO.builder()
            .amount(request.getAmount())
            .from(request.getFrom())
            .to(request.getTo())
            .rate(rate)
            .result(result.setScale(2, RoundingMode.HALF_UP))
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get currency trend",
        description = "Analyzes the trend of exchange rate between two currencies over a specified period"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Trend calculated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TrendResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Currency not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDTO.class)
            )
        )
    })
    @GetMapping("/trends")
    public ResponseEntity<TrendResponseDTO> getCurrencyTrend(
        @Valid @ModelAttribute TrendRequestDTO request
    ) {
        log.info("Getting trend from {} to {} for period {}",
            request.getFrom(), request.getTo(), request.getPeriod());

        // Validate currencies exist
        if (!currencies.contains(request.getFrom())) {
            throw new IllegalArgumentException("Source currency not found: " + request.getFrom());
        }
        if (!currencies.contains(request.getTo())) {
            throw new IllegalArgumentException("Target currency not found: " + request.getTo());
        }

        // Stub implementation - generate mock trend data
        BigDecimal changePercentage = generateStubTrend(request.getFrom(), request.getTo(), request.getPeriod());

        TrendResponseDTO response = TrendResponseDTO.builder()
            .from(request.getFrom())
            .to(request.getTo())
            .period(request.getPeriod())
            .changePercentage(changePercentage)
            .build();

        return ResponseEntity.ok(response);
    }

    // Helper methods for stub implementation

    private String getCurrencyName(String code) {
        return switch (code) {
            case "USD" -> "US Dollar";
            case "EUR" -> "Euro";
            case "GBP" -> "British Pound";
            case "JPY" -> "Japanese Yen";
            case "CHF" -> "Swiss Franc";
            case "CAD" -> "Canadian Dollar";
            case "AUD" -> "Australian Dollar";
            case "PLN" -> "Polish Zloty";
            case "INR" -> "Indian Rupee";
            case "CNY" -> "Chinese Yuan";
            default -> code + " Currency";
        };
    }

    private BigDecimal generateStubExchangeRate(String from, String to) {
        // Generate a deterministic but realistic-looking rate based on currency codes
        if (from.equals(to)) {
            return BigDecimal.ONE;
        }

        // Simple stub logic: use hashcode to generate consistent rates
        int hash = (from + to).hashCode();
        double rate = 0.5 + (Math.abs(hash) % 1000) / 500.0; // Range: 0.5 to 2.5

        return BigDecimal.valueOf(rate).setScale(6, RoundingMode.HALF_UP);
    }

    private BigDecimal generateStubTrend(String from, String to, String period) {
        // Generate a deterministic trend percentage
        int hash = (from + to + period).hashCode();
        double trend = -10.0 + (Math.abs(hash) % 2000) / 100.0; // Range: -10% to +10%

        return BigDecimal.valueOf(trend).setScale(2, RoundingMode.HALF_UP);
    }
}

