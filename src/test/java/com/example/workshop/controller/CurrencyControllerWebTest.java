package com.example.workshop.controller;

import com.example.workshop.exception.GlobalExceptionHandler;
import com.example.workshop.service.CurrencyService;
import com.example.workshop.service.ExchangeRateService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for CurrencyController using MockMvc.
 * TODO: Add proper mock setup for all tests
 */
@WebMvcTest(CurrencyController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@Disabled("WebMvcTest disabled - needs proper mock setup")
class CurrencyControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyService currencyService;

    @MockBean
    private ExchangeRateService exchangeRateService;


    @Test
    @DisplayName("GET /api/v1/currencies - Should return list of currencies")
    void testGetAllCurrencies_Success() throws Exception {
        // Mock the service to return empty list for now
        when(currencyService.getAllCurrencies()).thenReturn(Collections.emptyList());
        when(currencyService.toDTOList(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/currencies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/currencies - Should add new currency")
    void testAddCurrency_Success() throws Exception {
        mockMvc.perform(post("/api/v1/currencies")
                .param("currency", "PLN")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("PLN"))
                .andExpect(jsonPath("$.name").value("Polish Zloty"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("POST /api/v1/currencies - Should fail with invalid currency code (too short)")
    void testAddCurrency_InvalidFormatShort() throws Exception {
        mockMvc.perform(post("/api/v1/currencies")
                .param("currency", "US")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    @DisplayName("POST /api/v1/currencies - Should fail with invalid currency code (lowercase)")
    void testAddCurrency_InvalidFormatLowercase() throws Exception {
        mockMvc.perform(post("/api/v1/currencies")
                .param("currency", "usd")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("3 uppercase letters")));
    }

    @Test
    @DisplayName("POST /api/v1/currencies - Should fail when currency already exists")
    void testAddCurrency_AlreadyExists() throws Exception {
        // USD already exists in the default set
        mockMvc.perform(post("/api/v1/currencies")
                .param("currency", "USD")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("POST /api/v1/currencies/refresh - Should refresh exchange rates")
    void testRefreshExchangeRates_Success() throws Exception {
        mockMvc.perform(post("/api/v1/currencies/refresh")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.currenciesProcessed").exists());
    }

    @Test
    @DisplayName("GET /api/v1/currencies/exchange-rates - Should return exchange rate")
    void testGetExchangeRate_Success() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                .param("amount", "100")
                .param("from", "USD")
                .param("to", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.from").value("USD"))
                .andExpect(jsonPath("$.to").value("EUR"))
                .andExpect(jsonPath("$.rate").exists())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/v1/currencies/exchange-rates - Should fail with negative amount")
    void testGetExchangeRate_NegativeAmount() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                .param("amount", "-100")
                .param("from", "USD")
                .param("to", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("amount")));
    }

    @Test
    @DisplayName("GET /api/v1/currencies/exchange-rates - Should fail with invalid from currency")
    void testGetExchangeRate_InvalidFromCurrency() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                .param("amount", "100")
                .param("from", "US")
                .param("to", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/v1/currencies/exchange-rates - Should fail with missing parameters")
    void testGetExchangeRate_MissingParameters() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                .param("amount", "100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/currencies/exchange-rates - Should fail with non-existent currency")
    void testGetExchangeRate_NonExistentCurrency() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                .param("amount", "100")
                .param("from", "XXX")
                .param("to", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("GET /api/v1/currencies/trends - Should return currency trend")
    void testGetCurrencyTrend_Success() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "USD")
                .param("to", "EUR")
                .param("period", "10D")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("USD"))
                .andExpect(jsonPath("$.to").value("EUR"))
                .andExpect(jsonPath("$.period").value("10D"))
                .andExpect(jsonPath("$.changePercentage").exists());
    }

    @Test
    @DisplayName("GET /api/v1/currencies/trends - Should accept valid period formats")
    void testGetCurrencyTrend_ValidPeriodFormats() throws Exception {
        // Test Hours
        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "USD")
                .param("to", "EUR")
                .param("period", "12H")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test Days
        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "USD")
                .param("to", "EUR")
                .param("period", "30D")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test Months
        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "USD")
                .param("to", "EUR")
                .param("period", "3M")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Test Years
        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "USD")
                .param("to", "EUR")
                .param("period", "1Y")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/currencies/trends - Should fail with invalid period format")
    void testGetCurrencyTrend_InvalidPeriodFormat() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "USD")
                .param("to", "EUR")
                .param("period", "10X")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("period")));
    }

    @Test
    @DisplayName("GET /api/v1/currencies/trends - Should fail with non-existent currency")
    void testGetCurrencyTrend_NonExistentCurrency() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "XXX")
                .param("to", "EUR")
                .param("period", "10D")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("GET /api/v1/currencies/trends - Should fail with missing parameters")
    void testGetCurrencyTrend_MissingParameters() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "USD")
                .param("to", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}

