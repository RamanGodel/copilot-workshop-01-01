package com.example.workshop.exception;

import com.example.workshop.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for GlobalExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    private static final String TEST_PATH = "/api/v1/currencies";

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn(TEST_PATH);
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void testHandleMethodArgumentNotValidException() {
        // Create mock validation errors
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "amount", "must be positive");
        FieldError fieldError2 = new FieldError("object", "from", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Create a real MethodParameter with proper executable to avoid NPE
        MethodParameter methodParameter;
        try {
            methodParameter = new MethodParameter(
                this.getClass().getDeclaredMethod("testHandleMethodArgumentNotValidException"), -1);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleValidationExceptions(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("amount", "from");
        assertThat(response.getBody().getPath()).isEqualTo(TEST_PATH);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should handle CurrencyNotFoundException")
    void testHandleCurrencyNotFoundException() {
        CurrencyNotFoundException exception = new CurrencyNotFoundException("Currency USD not found");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleCurrencyNotFoundException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("Currency USD not found");
        assertThat(response.getBody().getPath()).isEqualTo(TEST_PATH);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should handle InvalidCurrencyCodeException")
    void testHandleInvalidCurrencyCodeException() {
        InvalidCurrencyCodeException exception = new InvalidCurrencyCodeException("US", "must be 3 letters");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleInvalidCurrencyCodeException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("US", "3 letters");
        assertThat(response.getBody().getPath()).isEqualTo(TEST_PATH);
    }

    @Test
    @DisplayName("Should handle InvalidPeriodException")
    void testHandleInvalidPeriodException() {
        InvalidPeriodException exception = new InvalidPeriodException("10X", "number followed by H, D, M, or Y");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleInvalidPeriodException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("10X");
        assertThat(response.getBody().getPath()).isEqualTo(TEST_PATH);
    }

    @Test
    @DisplayName("Should handle ExchangeRateNotFoundException")
    void testHandleExchangeRateNotFoundException() {
        ExchangeRateNotFoundException exception = new ExchangeRateNotFoundException("USD", "XXX");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleExchangeRateNotFoundException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("USD", "XXX");
        assertThat(response.getBody().getPath()).isEqualTo(TEST_PATH);
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException")
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input parameter");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleIllegalArgumentException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("Invalid input parameter");
        assertThat(response.getBody().getPath()).isEqualTo(TEST_PATH);
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void testHandleGenericException() {
        Exception exception = new RuntimeException("Unexpected error occurred");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleGenericException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).contains("unexpected error");
        assertThat(response.getBody().getPath()).isEqualTo(TEST_PATH);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should handle exception with null message")
    void testHandleExceptionWithNullMessage() {
        CurrencyNotFoundException exception = new CurrencyNotFoundException((String) null);

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleCurrencyNotFoundException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }
}

