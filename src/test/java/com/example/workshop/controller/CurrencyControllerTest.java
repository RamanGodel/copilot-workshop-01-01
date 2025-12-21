package com.example.workshop.controller;

import com.example.workshop.dto.CurrencyDTO;
import com.example.workshop.dto.ExchangeRateRequestDTO;
import com.example.workshop.dto.ExchangeRateResponseDTO;
import com.example.workshop.dto.TrendRequestDTO;
import com.example.workshop.dto.TrendResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for CurrencyController.
 */
@SpringBootTest
class CurrencyControllerTest {

    @Autowired
    private CurrencyController currencyController;

    @Test
    void testGetAllCurrencies() {
        ResponseEntity<List<CurrencyDTO>> response = currencyController.getAllCurrencies();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(4);
        assertThat(response.getBody()).extracting(CurrencyDTO::getCode)
            .contains("USD", "EUR", "GBP", "PLN");
    }

    @Test
    void testAddCurrency() {
        // Use a deterministic retry loop to avoid clashes with seeded or previously-created data.
        // CurrencyService requires exactly 3 uppercase letters (^[A-Z]{3}$).
        String code = null;
        ResponseEntity<CurrencyDTO> response = null;

        String alphabet = "QWERTYUIOPASDFGHJKLZXCVBNM";
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = "" +
                alphabet.charAt((int) (Math.random() * alphabet.length())) +
                alphabet.charAt((int) (Math.random() * alphabet.length())) +
                alphabet.charAt((int) (Math.random() * alphabet.length()));

            try {
                response = currencyController.addCurrency(candidate);
                code = candidate;
                break;
            } catch (IllegalStateException ignored) {
                // Currency already exists - try another code
            }
        }

        assertThat(response).as("Expected to create a unique currency after retries").isNotNull();
        assertThat(code).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(code);
        assertThat(response.getBody().getName()).isNotBlank();
    }

    @Test
    void testAddCurrencyInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            currencyController.addCurrency("US");
        });
    }

    @Test
    void testRefreshExchangeRates() {
        ResponseEntity<Map<String, String>> response = currencyController.refreshExchangeRates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("message");
        assertThat(response.getBody()).containsKey("timestamp");
        assertThat(response.getBody()).containsKey("currenciesProcessed");
    }

    @Test
    void testGetExchangeRate() {
        ExchangeRateRequestDTO request = ExchangeRateRequestDTO.builder()
            .amount(BigDecimal.valueOf(100))
            .from("USD")
            .to("EUR")
            .build();

        ResponseEntity<ExchangeRateResponseDTO> response = currencyController.getExchangeRate(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(response.getBody().getFrom()).isEqualTo("USD");
        assertThat(response.getBody().getTo()).isEqualTo("EUR");
        assertThat(response.getBody().getRate()).isNotNull();
        assertThat(response.getBody().getResult()).isNotNull();
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void testGetExchangeRateInvalidCurrency() {
        ExchangeRateRequestDTO request = ExchangeRateRequestDTO.builder()
            .amount(BigDecimal.valueOf(100))
            .from("XXX")
            .to("EUR")
            .build();

        assertThrows(IllegalArgumentException.class, () -> {
            currencyController.getExchangeRate(request);
        });
    }

    @Test
    void testGetCurrencyTrend() {
        TrendRequestDTO request = TrendRequestDTO.builder()
            .from("USD")
            .to("EUR")
            .period("10D")
            .build();

        ResponseEntity<TrendResponseDTO> response = currencyController.getCurrencyTrend(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFrom()).isEqualTo("USD");
        assertThat(response.getBody().getTo()).isEqualTo("EUR");
        assertThat(response.getBody().getPeriod()).isEqualTo("10D");
        assertThat(response.getBody().getChangePercentage()).isNotNull();
    }

    @Test
    void testGetCurrencyTrendInvalidCurrency() {
        TrendRequestDTO request = TrendRequestDTO.builder()
            .from("XXX")
            .to("EUR")
            .period("10D")
            .build();

        assertThrows(IllegalArgumentException.class, () -> {
            currencyController.getCurrencyTrend(request);
        });
    }
}
