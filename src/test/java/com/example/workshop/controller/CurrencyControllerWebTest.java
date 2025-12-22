package com.example.workshop.controller;

import com.example.workshop.config.SecurityConfig;
import com.example.workshop.dto.CurrencyDTO;
import com.example.workshop.dto.ExchangeRateRequestDTO;
import com.example.workshop.dto.ExchangeRateResponseDTO;
import com.example.workshop.dto.TrendResponseDTO;
import com.example.workshop.exception.*;
import com.example.workshop.model.Currency;
import com.example.workshop.model.ExchangeRate;
import com.example.workshop.service.CurrencyService;
import com.example.workshop.service.CustomUserDetailsService;
import com.example.workshop.service.ExchangeRateRefreshService;
import com.example.workshop.service.ExchangeRateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for CurrencyController using MockMvc with security.
 */
@WebMvcTest(CurrencyController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
@ActiveProfiles("test")
class CurrencyControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyService currencyService;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @MockBean
    private ExchangeRateRefreshService exchangeRateRefreshService;

    @MockBean
    private CustomUserDetailsService userDetailsService;


    @Test
    @DisplayName("GET /api/v1/currencies - Should return list of currencies without authentication")
    @WithAnonymousUser
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
    @DisplayName("POST /api/v1/currencies - Should require ADMIN role")
    @WithAnonymousUser
    void testAddCurrency_WithoutAuth() throws Exception {
        mockMvc.perform(post("/api/v1/currencies")
                .param("currency", "PLN")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("POST /api/v1/currencies - Should succeed with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void testAddCurrency_Success() throws Exception {
        // Mock the service to return a Currency entity
        Currency mockCurrency = Currency.builder()
                .id(1L)
                .code("PLN")
                .name("Polish Zloty")
                .build();

        CurrencyDTO mockDTO = CurrencyDTO.builder()
                .id(1L)
                .code("PLN")
                .name("Polish Zloty")
                .build();

        when(currencyService.addCurrency("PLN")).thenReturn(mockCurrency);
        when(currencyService.toDTO(mockCurrency)).thenReturn(mockDTO);

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
    @WithMockUser(roles = "ADMIN")
    void testAddCurrency_InvalidFormatShort() throws Exception {
        when(currencyService.addCurrency("US"))
                .thenThrow(new InvalidCurrencyCodeException("Invalid currency code 'US': must be 3 uppercase letters"));

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
    @DisplayName("POST /api/v1/currencies - Should accept lowercase and convert to uppercase")
    @WithMockUser(roles = "ADMIN")
    void testAddCurrency_InvalidFormatLowercase() throws Exception {
        // Controller converts "usd" to "USD" before calling service
        Currency mockCurrency = Currency.builder()
                .id(1L)
                .code("USD")
                .name("US Dollar")
                .build();

        CurrencyDTO mockDTO = CurrencyDTO.builder()
                .id(1L)
                .code("USD")
                .name("US Dollar")
                .build();

        when(currencyService.addCurrency("USD")).thenReturn(mockCurrency);
        when(currencyService.toDTO(mockCurrency)).thenReturn(mockDTO);

        mockMvc.perform(post("/api/v1/currencies")
                .param("currency", "usd")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("USD"));
    }

    @Test
    @DisplayName("POST /api/v1/currencies - Should fail when currency already exists")
    @WithMockUser(roles = "ADMIN")
    void testAddCurrency_AlreadyExists() throws Exception {
        when(currencyService.addCurrency("USD"))
                .thenThrow(new IllegalStateException("Currency USD already exists"));

        mockMvc.perform(post("/api/v1/currencies")
                .param("currency", "USD")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("POST /api/v1/currencies/refresh - Should require ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void testRefreshExchangeRates_Success() throws Exception {
        when(exchangeRateRefreshService.refreshAll())
                .thenReturn(new ExchangeRateRefreshService.RefreshSummary(0, 0, 0, 0, List.of()));

        mockMvc.perform(post("/api/v1/currencies/refresh")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.currenciesProcessed").exists());
    }

    @Test
    @DisplayName("GET /api/v1/currencies/exchange-rates - Should be accessible without authentication")
    @WithAnonymousUser
    void testGetExchangeRate_Success() throws Exception {
        ExchangeRateResponseDTO mockResponse = ExchangeRateResponseDTO.builder()
                .amount(new BigDecimal("100"))
                .from("USD")
                .to("EUR")
                .rate(new BigDecimal("0.85"))
                .result(new BigDecimal("85.00"))
                .timestamp(LocalDateTime.now())
                .build();

        when(exchangeRateService.getExchangeRate(org.mockito.ArgumentMatchers.any(ExchangeRateRequestDTO.class)))
                .thenReturn(mockResponse);

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
    @WithAnonymousUser
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
    @WithAnonymousUser
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
    @WithAnonymousUser
    void testGetExchangeRate_MissingParameters() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                .param("amount", "100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/currencies/exchange-rates - Should fail with non-existent currency")
    @WithAnonymousUser
    void testGetExchangeRate_NonExistentCurrency() throws Exception {
        when(exchangeRateService.getExchangeRate(org.mockito.ArgumentMatchers.any(ExchangeRateRequestDTO.class)))
                .thenThrow(new CurrencyNotFoundException("Currency XXX not found"));

        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                .param("amount", "100")
                .param("from", "XXX")
                .param("to", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("GET /api/v1/currencies/trends - Should require PREMIUM_USER or ADMIN role")
    @WithAnonymousUser
    void testGetCurrencyTrend_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "USD")
                .param("to", "EUR")
                .param("period", "10D")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("GET /api/v1/currencies/trends - Should succeed with PREMIUM_USER role")
    @WithMockUser(roles = "PREMIUM_USER")
    void testGetCurrencyTrend_Success() throws Exception {
        // Mock the service methods used by the controller
        when(exchangeRateService.getRatesInTimeRange(anyString(), anyString(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(exchangeRateService.calculateTrendPercentage(any(), any()))
                .thenReturn(new BigDecimal("2.5"));

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
    @WithMockUser(roles = "PREMIUM_USER")
    void testGetCurrencyTrend_ValidPeriodFormats() throws Exception {
        // Mock the service methods
        when(exchangeRateService.getRatesInTimeRange(anyString(), anyString(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(exchangeRateService.calculateTrendPercentage(any(), any()))
                .thenReturn(BigDecimal.ZERO);

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
    @WithMockUser(roles = "PREMIUM_USER")
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
    @WithMockUser(roles = "PREMIUM_USER")
    void testGetCurrencyTrend_NonExistentCurrency() throws Exception {
        when(exchangeRateService.getRatesInTimeRange(anyString(), anyString(), any(), any()))
                .thenThrow(new CurrencyNotFoundException("Currency XXX not found"));

        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "XXX")
                .param("to", "EUR")
                .param("period", "10D")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("GET /api/v1/currencies/trends - Should fail with missing parameters")
    @WithMockUser(roles = "PREMIUM_USER")
    void testGetCurrencyTrend_MissingParameters() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "USD")
                .param("to", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}

