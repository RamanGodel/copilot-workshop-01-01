package com.example.workshop.service;

import com.example.workshop.dto.ExchangeRateRequestDTO;
import com.example.workshop.dto.ExchangeRateResponseDTO;
import com.example.workshop.exception.CurrencyNotFoundException;
import com.example.workshop.exception.ExchangeRateNotFoundException;
import com.example.workshop.model.Currency;
import com.example.workshop.model.ExchangeRate;
import com.example.workshop.repository.CurrencyRepository;
import com.example.workshop.repository.ExchangeRateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExchangeRateService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRateService Tests")
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    @DisplayName("Should get exchange rate successfully")
    void shouldGetExchangeRate() {
        // Given
        Currency usd = Currency.builder().id(1L).code("USD").name("US Dollar").build();
        Currency eur = Currency.builder().id(2L).code("EUR").name("Euro").build();

        ExchangeRate rate = ExchangeRate.builder()
                .id(1L)
                .baseCurrency(usd)
                .targetCurrency(eur)
                .rate(BigDecimal.valueOf(0.85))
                .timestamp(LocalDateTime.now())
                .build();

        ExchangeRateRequestDTO request = ExchangeRateRequestDTO.builder()
                .amount(BigDecimal.valueOf(100))
                .from("USD")
                .to("EUR")
                .build();

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usd));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(exchangeRateRepository.findTopByBaseCurrencyAndTargetCurrencyOrderByTimestampDesc(usd, eur))
                .thenReturn(Optional.of(rate));

        // When
        ExchangeRateResponseDTO response = exchangeRateService.getExchangeRate(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(response.getFrom()).isEqualTo("USD");
        assertThat(response.getTo()).isEqualTo("EUR");
        assertThat(response.getRate()).isEqualByComparingTo(BigDecimal.valueOf(0.85));
        assertThat(response.getResult()).isEqualByComparingTo(BigDecimal.valueOf(85.00));

        verify(currencyRepository, times(1)).findByCode("USD");
        verify(currencyRepository, times(1)).findByCode("EUR");
        verify(exchangeRateRepository, times(1))
                .findTopByBaseCurrencyAndTargetCurrencyOrderByTimestampDesc(usd, eur);
    }

    @Test
    @DisplayName("Should throw exception when base currency not found")
    void shouldThrowExceptionWhenBaseCurrencyNotFound() {
        // Given
        ExchangeRateRequestDTO request = ExchangeRateRequestDTO.builder()
                .amount(BigDecimal.valueOf(100))
                .from("XYZ")
                .to("EUR")
                .build();

        when(currencyRepository.findByCode("XYZ")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> exchangeRateService.getExchangeRate(request))
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessageContaining("XYZ");

        verify(currencyRepository, times(1)).findByCode("XYZ");
        verify(exchangeRateRepository, never())
                .findTopByBaseCurrencyAndTargetCurrencyOrderByTimestampDesc(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when target currency not found")
    void shouldThrowExceptionWhenTargetCurrencyNotFound() {
        // Given
        Currency usd = Currency.builder().id(1L).code("USD").name("US Dollar").build();

        ExchangeRateRequestDTO request = ExchangeRateRequestDTO.builder()
                .amount(BigDecimal.valueOf(100))
                .from("USD")
                .to("XYZ")
                .build();

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usd));
        when(currencyRepository.findByCode("XYZ")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> exchangeRateService.getExchangeRate(request))
                .isInstanceOf(CurrencyNotFoundException.class)
                .hasMessageContaining("XYZ");

        verify(currencyRepository, times(1)).findByCode("USD");
        verify(currencyRepository, times(1)).findByCode("XYZ");
    }

    @Test
    @DisplayName("Should throw exception when exchange rate not found")
    void shouldThrowExceptionWhenExchangeRateNotFound() {
        // Given
        Currency usd = Currency.builder().id(1L).code("USD").name("US Dollar").build();
        Currency eur = Currency.builder().id(2L).code("EUR").name("Euro").build();

        ExchangeRateRequestDTO request = ExchangeRateRequestDTO.builder()
                .amount(BigDecimal.valueOf(100))
                .from("USD")
                .to("EUR")
                .build();

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usd));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(exchangeRateRepository.findTopByBaseCurrencyAndTargetCurrencyOrderByTimestampDesc(usd, eur))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> exchangeRateService.getExchangeRate(request))
                .isInstanceOf(ExchangeRateNotFoundException.class)
                .hasMessageContaining("USD")
                .hasMessageContaining("EUR");
    }

    @Test
    @DisplayName("Should save exchange rate")
    void shouldSaveExchangeRate() {
        // Given
        Currency usd = Currency.builder().id(1L).code("USD").build();
        Currency eur = Currency.builder().id(2L).code("EUR").build();

        ExchangeRate rate = ExchangeRate.builder()
                .baseCurrency(usd)
                .targetCurrency(eur)
                .rate(BigDecimal.valueOf(0.85))
                .timestamp(LocalDateTime.now())
                .build();

        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenReturn(rate);

        // When
        ExchangeRate saved = exchangeRateService.saveExchangeRate(rate);

        // Then
        assertThat(saved).isNotNull();
        verify(exchangeRateRepository, times(1)).save(rate);
    }

    @Test
    @DisplayName("Should save exchange rate with currency codes")
    void shouldSaveExchangeRateWithCurrencyCodes() {
        // Given
        Currency usd = Currency.builder().id(1L).code("USD").build();
        Currency eur = Currency.builder().id(2L).code("EUR").build();

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usd));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(exchangeRateRepository.save(any(ExchangeRate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ExchangeRate saved = exchangeRateService.saveExchangeRate(
                "USD", "EUR", BigDecimal.valueOf(0.85), LocalDateTime.now());

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getBaseCurrency().getCode()).isEqualTo("USD");
        assertThat(saved.getTargetCurrency().getCode()).isEqualTo("EUR");
        assertThat(saved.getRate()).isEqualByComparingTo(BigDecimal.valueOf(0.85));

        verify(currencyRepository, times(1)).findByCode("USD");
        verify(currencyRepository, times(1)).findByCode("EUR");
        verify(exchangeRateRepository, times(1)).save(any(ExchangeRate.class));
    }

    @Test
    @DisplayName("Should get historical rates")
    void shouldGetHistoricalRates() {
        // Given
        Currency usd = Currency.builder().id(1L).code("USD").build();
        Currency eur = Currency.builder().id(2L).code("EUR").build();

        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<ExchangeRate> rates = Arrays.asList(
                createRate(usd, eur, 0.85, since.plusDays(1)),
                createRate(usd, eur, 0.86, since.plusDays(2))
        );

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usd));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(exchangeRateRepository.findByBaseCurrencyAndTargetCurrencyAndTimestampAfter(usd, eur, since))
                .thenReturn(rates);

        // When
        List<ExchangeRate> result = exchangeRateService.getHistoricalRates("USD", "EUR", since);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).isSortedAccordingTo(
                (r1, r2) -> r2.getTimestamp().compareTo(r1.getTimestamp())); // Latest first

        verify(currencyRepository, times(1)).findByCode("USD");
        verify(currencyRepository, times(1)).findByCode("EUR");
        verify(exchangeRateRepository, times(1))
                .findByBaseCurrencyAndTargetCurrencyAndTimestampAfter(usd, eur, since);
    }

    @Test
    @DisplayName("Should get rates in time range")
    void shouldGetRatesInTimeRange() {
        // Given
        Currency usd = Currency.builder().id(1L).code("USD").build();
        Currency eur = Currency.builder().id(2L).code("EUR").build();

        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        List<ExchangeRate> rates = Arrays.asList(
                createRate(usd, eur, 0.85, start.plusDays(1)),
                createRate(usd, eur, 0.86, start.plusDays(3))
        );

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usd));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(exchangeRateRepository.findRatesInTimeRange(usd, eur, start, end))
                .thenReturn(rates);

        // When
        List<ExchangeRate> result = exchangeRateService.getRatesInTimeRange("USD", "EUR", start, end);

        // Then
        assertThat(result).hasSize(2);
        verify(exchangeRateRepository, times(1)).findRatesInTimeRange(usd, eur, start, end);
    }

    @Test
    @DisplayName("Should calculate trend percentage")
    void shouldCalculateTrendPercentage() {
        // Given
        Currency usd = Currency.builder().id(1L).code("USD").build();
        Currency eur = Currency.builder().id(2L).code("EUR").build();

        ExchangeRate oldRate = createRate(usd, eur, 0.80, LocalDateTime.now().minusDays(7));
        ExchangeRate newRate = createRate(usd, eur, 0.88, LocalDateTime.now());

        // When
        BigDecimal trend = exchangeRateService.calculateTrendPercentage(oldRate, newRate);

        // Then
        // (0.88 - 0.80) / 0.80 * 100 = 10%
        assertThat(trend).isEqualByComparingTo(BigDecimal.valueOf(10.00));
    }

    @Test
    @DisplayName("Should calculate negative trend percentage")
    void shouldCalculateNegativeTrendPercentage() {
        // Given
        Currency usd = Currency.builder().id(1L).code("USD").build();
        Currency eur = Currency.builder().id(2L).code("EUR").build();

        ExchangeRate oldRate = createRate(usd, eur, 1.00, LocalDateTime.now().minusDays(7));
        ExchangeRate newRate = createRate(usd, eur, 0.90, LocalDateTime.now());

        // When
        BigDecimal trend = exchangeRateService.calculateTrendPercentage(oldRate, newRate);

        // Then
        // (0.90 - 1.00) / 1.00 * 100 = -10%
        assertThat(trend).isEqualByComparingTo(BigDecimal.valueOf(-10.00));
    }

    @Test
    @DisplayName("Should throw exception when calculating trend with null rates")
    void shouldThrowExceptionForNullRates() {
        // Given
        ExchangeRate rate = createRate(null, null, 0.85, LocalDateTime.now());

        // When & Then
        assertThatThrownBy(() -> exchangeRateService.calculateTrendPercentage(null, rate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");

        assertThatThrownBy(() -> exchangeRateService.calculateTrendPercentage(rate, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("Should get latest rate using Optional")
    void shouldGetLatestRateUsingOptional() {
        // Given
        Currency usd = Currency.builder().id(1L).code("USD").build();
        Currency eur = Currency.builder().id(2L).code("EUR").build();

        ExchangeRate rate = createRate(usd, eur, 0.85, LocalDateTime.now());

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usd));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(exchangeRateRepository.findTopByBaseCurrencyAndTargetCurrencyOrderByTimestampDesc(usd, eur))
                .thenReturn(Optional.of(rate));

        // When
        Optional<ExchangeRate> result = exchangeRateService.getLatestRate("USD", "EUR");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getRate()).isEqualByComparingTo(BigDecimal.valueOf(0.85));
    }

    @Test
    @DisplayName("Should return empty Optional when currencies not found")
    void shouldReturnEmptyOptionalWhenCurrenciesNotFound() {
        // Given
        when(currencyRepository.findByCode(anyString())).thenReturn(Optional.empty());

        // When
        Optional<ExchangeRate> result = exchangeRateService.getLatestRate("XYZ", "ABC");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should count rates")
    void shouldCountRates() {
        // Given
        Currency usd = Currency.builder().id(1L).code("USD").build();
        Currency eur = Currency.builder().id(2L).code("EUR").build();

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usd));
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(exchangeRateRepository.countByBaseCurrencyAndTargetCurrency(usd, eur))
                .thenReturn(5L);

        // When
        long count = exchangeRateService.countRates("USD", "EUR");

        // Then
        assertThat(count).isEqualTo(5L);
    }

    private ExchangeRate createRate(Currency base, Currency target, double rate, LocalDateTime timestamp) {
        return ExchangeRate.builder()
                .baseCurrency(base)
                .targetCurrency(target)
                .rate(BigDecimal.valueOf(rate))
                .timestamp(timestamp)
                .build();
    }
}

