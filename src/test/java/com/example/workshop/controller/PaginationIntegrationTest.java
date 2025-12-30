package com.example.workshop.controller;

import com.example.workshop.dto.ExchangeRateDTO;
import com.example.workshop.dto.PageResponseDTO;
import com.example.workshop.model.Currency;
import com.example.workshop.model.ExchangeRate;
import com.example.workshop.repository.CurrencyRepository;
import com.example.workshop.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for pagination endpoints.
 * Tests Phase 7.2: Performance Optimizations - Pagination support.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Pagination Integration Tests")
class PaginationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    private Currency usd;
    private Currency eur;
    private Currency gbp;

    @BeforeEach
    void setUp() {
        // Clean up
        exchangeRateRepository.deleteAll();

        // Get or create test currencies
        usd = currencyRepository.findByCode("USD")
                .orElseGet(() -> currencyRepository.save(Currency.builder()
                        .code("USD").name("United States Dollar").build()));
        eur = currencyRepository.findByCode("EUR")
                .orElseGet(() -> currencyRepository.save(Currency.builder()
                        .code("EUR").name("Euro").build()));
        gbp = currencyRepository.findByCode("GBP")
                .orElseGet(() -> currencyRepository.save(Currency.builder()
                        .code("GBP").name("British Pound").build()));

        // Create test data - 50 exchange rates
        List<ExchangeRate> rates = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 50; i++) {
            rates.add(ExchangeRate.builder()
                    .baseCurrency(usd)
                    .targetCurrency(i % 2 == 0 ? eur : gbp)
                    .rate(BigDecimal.valueOf(0.85 + (i * 0.001)))
                    .timestamp(now.minusHours(i))
                    .build());
        }

        exchangeRateRepository.saveAll(rates);
    }

    @Test
    @DisplayName("Should return first page of exchange rates with default parameters")
    void testGetPaginatedExchangeRates_DefaultParameters() {
        // When
        ResponseEntity<PageResponseDTO<ExchangeRateDTO>> response = restTemplate.exchange(
                "/api/v1/currencies/exchange-rates/paginated",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponseDTO<ExchangeRateDTO> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(20); // default page size
        assertThat(page.getPageNumber()).isEqualTo(0);
        assertThat(page.getPageSize()).isEqualTo(20);
        assertThat(page.getTotalElements()).isEqualTo(50);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isFalse();
        assertThat(page.isHasNext()).isTrue();
        assertThat(page.isHasPrevious()).isFalse();
    }

    @Test
    @DisplayName("Should return second page of exchange rates")
    void testGetPaginatedExchangeRates_SecondPage() {
        // When
        ResponseEntity<PageResponseDTO<ExchangeRateDTO>> response = restTemplate.exchange(
                "/api/v1/currencies/exchange-rates/paginated?page=1&size=20",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponseDTO<ExchangeRateDTO> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(20);
        assertThat(page.getPageNumber()).isEqualTo(1);
        assertThat(page.isFirst()).isFalse();
        assertThat(page.isLast()).isFalse();
        assertThat(page.isHasNext()).isTrue();
        assertThat(page.isHasPrevious()).isTrue();
    }

    @Test
    @DisplayName("Should return last page of exchange rates")
    void testGetPaginatedExchangeRates_LastPage() {
        // When
        ResponseEntity<PageResponseDTO<ExchangeRateDTO>> response = restTemplate.exchange(
                "/api/v1/currencies/exchange-rates/paginated?page=2&size=20",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponseDTO<ExchangeRateDTO> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(10); // only 10 items on last page
        assertThat(page.getPageNumber()).isEqualTo(2);
        assertThat(page.isFirst()).isFalse();
        assertThat(page.isLast()).isTrue();
        assertThat(page.isHasNext()).isFalse();
        assertThat(page.isHasPrevious()).isTrue();
    }

    @Test
    @DisplayName("Should return custom page size")
    void testGetPaginatedExchangeRates_CustomPageSize() {
        // When
        ResponseEntity<PageResponseDTO<ExchangeRateDTO>> response = restTemplate.exchange(
                "/api/v1/currencies/exchange-rates/paginated?page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponseDTO<ExchangeRateDTO> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getPageSize()).isEqualTo(10);
        assertThat(page.getTotalPages()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should sort exchange rates by timestamp in ascending order")
    void testGetPaginatedExchangeRates_SortAscending() {
        // When
        ResponseEntity<PageResponseDTO<ExchangeRateDTO>> response = restTemplate.exchange(
                "/api/v1/currencies/exchange-rates/paginated?page=0&size=5&sortBy=timestamp&direction=asc",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponseDTO<ExchangeRateDTO> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(5);

        // Verify ascending order
        List<LocalDateTime> timestamps = page.getContent().stream()
                .map(ExchangeRateDTO::getTimestamp)
                .toList();

        for (int i = 0; i < timestamps.size() - 1; i++) {
            assertThat(timestamps.get(i)).isBeforeOrEqualTo(timestamps.get(i + 1));
        }
    }

    @Test
    @DisplayName("Should sort exchange rates by timestamp in descending order")
    void testGetPaginatedExchangeRates_SortDescending() {
        // When
        ResponseEntity<PageResponseDTO<ExchangeRateDTO>> response = restTemplate.exchange(
                "/api/v1/currencies/exchange-rates/paginated?page=0&size=5&sortBy=timestamp&direction=desc",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponseDTO<ExchangeRateDTO> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(5);

        // Verify descending order
        List<LocalDateTime> timestamps = page.getContent().stream()
                .map(ExchangeRateDTO::getTimestamp)
                .toList();

        for (int i = 0; i < timestamps.size() - 1; i++) {
            assertThat(timestamps.get(i)).isAfterOrEqualTo(timestamps.get(i + 1));
        }
    }

    @Test
    @DisplayName("Should filter exchange rates by base currency")
    void testGetPaginatedExchangeRates_FilterByBaseCurrency() {
        // When
        ResponseEntity<PageResponseDTO<ExchangeRateDTO>> response = restTemplate.exchange(
                "/api/v1/currencies/exchange-rates/paginated?base=USD&page=0&size=50",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponseDTO<ExchangeRateDTO> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(page.getContent()).allMatch(dto -> dto.getBaseCurrencyCode().equals("USD"));
    }

    @Test
    @DisplayName("Should return empty page when page number exceeds total pages")
    void testGetPaginatedExchangeRates_EmptyPage() {
        // When
        ResponseEntity<PageResponseDTO<ExchangeRateDTO>> response = restTemplate.exchange(
                "/api/v1/currencies/exchange-rates/paginated?page=100&size=20",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponseDTO<ExchangeRateDTO> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(50);
    }

    @Test
    @DisplayName("Should include complete exchange rate information in DTOs")
    void testGetPaginatedExchangeRates_DTOContent() {
        // When
        ResponseEntity<PageResponseDTO<ExchangeRateDTO>> response = restTemplate.exchange(
                "/api/v1/currencies/exchange-rates/paginated?page=0&size=1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponseDTO<ExchangeRateDTO> page = response.getBody();
        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(1);

        ExchangeRateDTO dto = page.getContent().get(0);
        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getBaseCurrencyCode()).isNotNull();
        assertThat(dto.getBaseCurrencyName()).isNotNull();
        assertThat(dto.getTargetCurrencyCode()).isNotNull();
        assertThat(dto.getTargetCurrencyName()).isNotNull();
        assertThat(dto.getRate()).isNotNull();
        assertThat(dto.getTimestamp()).isNotNull();
    }
}
