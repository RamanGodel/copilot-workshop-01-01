package com.example.workshop.exception;

import com.example.workshop.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Handles all exceptions and returns appropriate HTTP responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("Validation error on path {}: {}", request.getRequestURI(), ex.getMessage());

        // Collect all field errors
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        String message = errorMessage.isEmpty() ? "Validation failed" : errorMessage;

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, message, request));
    }

    /**
     * Handles CurrencyNotFoundException.
     */
    @ExceptionHandler(CurrencyNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCurrencyNotFoundException(
            CurrencyNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Currency not found on path {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request));
    }

    /**
     * Handles InvalidCurrencyCodeException.
     */
    @ExceptionHandler(InvalidCurrencyCodeException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCurrencyCodeException(
            InvalidCurrencyCodeException ex,
            HttpServletRequest request) {

        log.warn("Invalid currency code on path {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request));
    }

    /**
     * Handles InvalidPeriodException.
     */
    @ExceptionHandler(InvalidPeriodException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidPeriodException(
            InvalidPeriodException ex,
            HttpServletRequest request) {

        log.warn("Invalid period on path {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request));
    }

    /**
     * Handles ExchangeRateNotFoundException.
     */
    @ExceptionHandler(ExchangeRateNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleExchangeRateNotFoundException(
            ExchangeRateNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Exchange rate not found on path {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request));
    }

    /**
     * Handles IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Illegal argument on path {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request));
    }

    /**
     * Handles IllegalStateException (e.g., duplicate currency).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request) {

        log.warn("Illegal state on path {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request));
    }

    /**
     * Handles all other uncaught exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error on path {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Please try again later.", request));
    }

    /**
     * Helper to build a standardized ErrorResponseDTO.
     */
    private ErrorResponseDTO buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        return ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
    }
}
