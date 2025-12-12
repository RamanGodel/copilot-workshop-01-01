package com.example.workshop.service;

import com.example.workshop.dto.CurrencyDTO;
import com.example.workshop.exception.InvalidCurrencyCodeException;
import com.example.workshop.model.Currency;
import com.example.workshop.repository.CurrencyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CurrencyService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CurrencyService Tests")
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    @DisplayName("Should get all currencies sorted by code")
    void shouldGetAllCurrenciesSorted() {
        // Given
        Currency usd = Currency.builder().id(1L).code("USD").name("US Dollar").build();
        Currency eur = Currency.builder().id(2L).code("EUR").name("Euro").build();
        Currency gbp = Currency.builder().id(3L).code("GBP").name("British Pound").build();

        when(currencyRepository.findAll()).thenReturn(Arrays.asList(gbp, usd, eur));

        // When
        List<Currency> result = currencyService.getAllCurrencies();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Currency::getCode)
                .containsExactly("EUR", "GBP", "USD");
        verify(currencyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find currency by code")
    void shouldFindByCode() {
        // Given
        Currency currency = Currency.builder()
                .id(1L)
                .code("USD")
                .name("US Dollar")
                .build();

        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(currency));

        // When
        Optional<Currency> result = currencyService.findByCode("USD");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("USD");
        verify(currencyRepository, times(1)).findByCode("USD");
    }

    @Test
    @DisplayName("Should return empty when currency not found")
    void shouldReturnEmptyWhenNotFound() {
        // Given
        when(currencyRepository.findByCode(anyString())).thenReturn(Optional.empty());

        // When
        Optional<Currency> result = currencyService.findByCode("XYZ");

        // Then
        assertThat(result).isEmpty();
        verify(currencyRepository, times(1)).findByCode("XYZ");
    }

    @Test
    @DisplayName("Should return empty for null code")
    void shouldReturnEmptyForNullCode() {
        // When
        Optional<Currency> result = currencyService.findByCode(null);

        // Then
        assertThat(result).isEmpty();
        verify(currencyRepository, never()).findByCode(anyString());
    }

    @Test
    @DisplayName("Should add new currency successfully")
    void shouldAddCurrency() {
        // Given
        Currency currency = Currency.builder()
                .id(1L)
                .code("USD")
                .name("US Dollar")
                .build();

        when(currencyRepository.existsByCode("USD")).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        // When
        Currency result = currencyService.addCurrency("USD");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("USD");
        verify(currencyRepository, times(1)).existsByCode("USD");
        verify(currencyRepository, times(1)).save(any(Currency.class));
    }

    @Test
    @DisplayName("Should throw exception when adding invalid currency code")
    void shouldThrowExceptionForInvalidCode() {
        // When & Then
        assertThatThrownBy(() -> currencyService.addCurrency("US"))
                .isInstanceOf(InvalidCurrencyCodeException.class)
                .hasMessageContaining("3 uppercase letters");

        assertThatThrownBy(() -> currencyService.addCurrency("USDD"))
                .isInstanceOf(InvalidCurrencyCodeException.class);

        assertThatThrownBy(() -> currencyService.addCurrency("usd"))
                .isInstanceOf(InvalidCurrencyCodeException.class);

        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    @DisplayName("Should throw exception when adding duplicate currency")
    void shouldThrowExceptionForDuplicateCurrency() {
        // Given
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> currencyService.addCurrency("USD"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");

        verify(currencyRepository, times(1)).existsByCode("USD");
        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    @DisplayName("Should add currency with custom name")
    void shouldAddCurrencyWithCustomName() {
        // Given
        Currency currency = Currency.builder()
                .id(1L)
                .code("USD")
                .name("United States Dollar")
                .build();

        when(currencyRepository.existsByCode("USD")).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);

        // When
        Currency result = currencyService.addCurrency("USD", "United States Dollar");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("USD");
        assertThat(result.getName()).isEqualTo("United States Dollar");
        verify(currencyRepository, times(1)).save(any(Currency.class));
    }

    @Test
    @DisplayName("Should throw exception when name is blank")
    void shouldThrowExceptionForBlankName() {
        // When & Then
        assertThatThrownBy(() -> currencyService.addCurrency("USD", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name cannot be blank");

        assertThatThrownBy(() -> currencyService.addCurrency("USD", null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    @DisplayName("Should convert currency to DTO")
    void shouldConvertToDTO() {
        // Given
        Currency currency = Currency.builder()
                .id(1L)
                .code("USD")
                .name("US Dollar")
                .build();

        // When
        CurrencyDTO dto = currencyService.toDTO(currency);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCode()).isEqualTo("USD");
        assertThat(dto.getName()).isEqualTo("US Dollar");
    }

    @Test
    @DisplayName("Should convert list of currencies to DTOs")
    void shouldConvertListToDTOs() {
        // Given
        List<Currency> currencies = Arrays.asList(
                Currency.builder().id(1L).code("USD").name("US Dollar").build(),
                Currency.builder().id(2L).code("EUR").name("Euro").build()
        );

        // When
        List<CurrencyDTO> dtos = currencyService.toDTOList(currencies);

        // Then
        assertThat(dtos).hasSize(2);
        assertThat(dtos).extracting(CurrencyDTO::getCode)
                .containsExactly("USD", "EUR");
    }

    @Test
    @DisplayName("Should use Stream API for filtering")
    void shouldUseStreamAPIForFiltering() {
        // Given
        List<Currency> currencies = Arrays.asList(
                Currency.builder().id(1L).code("USD").name("US Dollar").build(),
                Currency.builder().id(2L).code("EUR").name("Euro").build(),
                Currency.builder().id(3L).code("GBP").name("British Pound").build()
        );

        when(currencyRepository.findAll()).thenReturn(currencies);

        // When
        List<Currency> result = currencyService.getAllCurrencies();

        // Then - verify Stream API is used (implicitly through sorting)
        assertThat(result).isSortedAccordingTo((c1, c2) -> c1.getCode().compareTo(c2.getCode()));
    }
}

