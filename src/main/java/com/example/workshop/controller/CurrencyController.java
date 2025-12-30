package com.example.workshop.controller;

import com.example.workshop.dto.*;
import com.example.workshop.model.Currency;
import com.example.workshop.model.ExchangeRate;
import com.example.workshop.service.CurrencyService;
import com.example.workshop.service.ExchangeRateRefreshService;
import com.example.workshop.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * REST Controller for currency exchange rate operations.
 * Provides endpoints for managing currencies, fetching exchange rates, and analyzing trends.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
@Tag(name = "Currency Exchange", description = "APIs for currency exchange rates and trends")
public class CurrencyController {

    private final CurrencyService currencyService;
    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateRefreshService exchangeRateRefreshService;

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

        List<Currency> currencies = currencyService.getAllCurrencies();
        List<CurrencyDTO> currencyDTOs = currencyService.toDTOList(currencies);

        return ResponseEntity.ok(currencyDTOs);
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

        Currency savedCurrency = currencyService.addCurrency(currency.toUpperCase());
        CurrencyDTO dto = currencyService.toDTO(savedCurrency);

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

        ExchangeRateRefreshService.RefreshSummary summary = exchangeRateRefreshService.refreshAll();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Exchange rates refresh triggered");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("currenciesInSystem", String.valueOf(summary.getCurrenciesInSystem()));
        // For compatibility with tests that expect this key
        response.put("currenciesProcessed", String.valueOf(summary.getCurrenciesProcessed()));
        response.put("providersWithData", String.valueOf(summary.getProvidersWithData()));
        response.put("ratesSaved", String.valueOf(summary.getRatesSaved()));

        if (summary.getFailures() != null && !summary.getFailures().isEmpty()) {
            response.put("failures", String.valueOf(summary.getFailures().size()));
        }

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

        ExchangeRateResponseDTO response = exchangeRateService.getExchangeRate(request);

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

        // Calculate time range based on period
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = calculateStartTime(request.getPeriod(), endTime);

        // Get historical rates
        List<ExchangeRate> rates = exchangeRateService.getRatesInTimeRange(
                request.getFrom(), request.getTo(), startTime, endTime);

        // Calculate trend
        BigDecimal changePercentage = calculateTrend(rates);

        TrendResponseDTO response = TrendResponseDTO.builder()
            .from(request.getFrom())
            .to(request.getTo())
            .period(request.getPeriod())
            .changePercentage(changePercentage)
            .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get paginated exchange rates",
        description = "Retrieves exchange rates with pagination and sorting support (Phase 7.2: Performance Optimizations)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved paginated exchange rates",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PageResponseDTO.class)
            )
        )
    })
    @GetMapping("/exchange-rates/paginated")
    public ResponseEntity<PageResponseDTO<ExchangeRateDTO>> getPaginatedExchangeRates(
        @Parameter(description = "Base currency code (optional)", example = "EUR")
        @RequestParam(required = false) String base,
        @Parameter(description = "Page number (zero-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Sort field", example = "timestamp")
        @RequestParam(defaultValue = "timestamp") String sortBy,
        @Parameter(description = "Sort direction (asc or desc)", example = "desc")
        @RequestParam(defaultValue = "desc") String direction
    ) {
        log.info("Fetching paginated exchange rates (base: {}, page: {}, size: {}, sort: {} {})",
                base, page, size, sortBy, direction);

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ExchangeRate> ratePage;
        if (base != null && !base.isBlank()) {
            ratePage = exchangeRateService.getRatesForBaseCurrencyPaginated(base.toUpperCase(), pageable);
        } else {
            ratePage = exchangeRateService.getAllRatesPaginated(pageable);
        }

        // Convert to DTOs
        List<ExchangeRateDTO> rateDTOs = ratePage.getContent().stream()
                .map(this::convertToDTO)
                .toList();

        PageResponseDTO<ExchangeRateDTO> response = PageResponseDTO.<ExchangeRateDTO>builder()
                .content(rateDTOs)
                .pageNumber(ratePage.getNumber())
                .pageSize(ratePage.getSize())
                .totalElements(ratePage.getTotalElements())
                .totalPages(ratePage.getTotalPages())
                .first(ratePage.isFirst())
                .last(ratePage.isLast())
                .hasNext(ratePage.hasNext())
                .hasPrevious(ratePage.hasPrevious())
                .build();

        return ResponseEntity.ok(response);
    }

    // Helper methods

    private ExchangeRateDTO convertToDTO(ExchangeRate rate) {
        return ExchangeRateDTO.builder()
                .id(rate.getId())
                .baseCurrencyCode(rate.getBaseCurrency().getCode())
                .baseCurrencyName(rate.getBaseCurrency().getName())
                .targetCurrencyCode(rate.getTargetCurrency().getCode())
                .targetCurrencyName(rate.getTargetCurrency().getName())
                .rate(rate.getRate())
                .timestamp(rate.getTimestamp())
                .build();
    }

    private LocalDateTime calculateStartTime(String period, LocalDateTime endTime) {
        String unit = period.substring(period.length() - 1);
        int value = Integer.parseInt(period.substring(0, period.length() - 1));

        return switch (unit) {
            case "H" -> endTime.minusHours(value);
            case "D" -> endTime.minusDays(value);
            case "M" -> endTime.minusMonths(value);
            case "Y" -> endTime.minusYears(value);
            default -> throw new IllegalArgumentException("Invalid period unit: " + unit);
        };
    }

    private BigDecimal calculateTrend(List<ExchangeRate> rates) {
        if (rates.isEmpty() || rates.size() < 2) {
            return BigDecimal.ZERO;
        }

        // Get oldest and newest rates
        ExchangeRate oldestRate = rates.get(rates.size() - 1);
        ExchangeRate newestRate = rates.get(0);

        return exchangeRateService.calculateTrendPercentage(oldestRate, newestRate);
    }
}
