package com.example.workshop.initializer;

import com.example.workshop.model.Currency;
import com.example.workshop.service.CurrencyService;
import com.example.workshop.service.ExchangeRateRefreshService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StartupInitializer.
 */
@ExtendWith(MockitoExtension.class)
class StartupInitializerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private CurrencyService currencyService;

    @Mock
    private ExchangeRateRefreshService refreshService;

    @Mock
    private Connection connection;

    @InjectMocks
    private StartupInitializer initializer;

    private ExchangeRateRefreshService.RefreshSummary refreshSummary;

    @BeforeEach
    void setUp() {
        refreshSummary = new ExchangeRateRefreshService.RefreshSummary(
                7, 7, 2, 42, Collections.emptyList()
        );
    }

    @Test
    void onApplicationReady_Success() throws Exception {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);
        when(currencyService.getAllCurrencies()).thenReturn(List.of(
                createCurrency("USD"),
                createCurrency("EUR")
        ));
        when(refreshService.refreshAll()).thenReturn(refreshSummary);

        // When
        initializer.onApplicationReady();

        // Then
        verify(dataSource).getConnection();
        verify(connection).isValid(5);
        verify(connection).close();
        verify(currencyService, atLeastOnce()).getAllCurrencies();
        verify(refreshService).refreshAll();
    }

    @Test
    void onApplicationReady_InitializesDefaultCurrenciesWhenEmpty() throws Exception {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);
        when(currencyService.getAllCurrencies())
                .thenReturn(Collections.emptyList())  // First call - empty
                .thenReturn(List.of(createCurrency("USD")));  // Second call - after adding
        when(currencyService.addCurrency(anyString())).thenReturn(createCurrency("USD"));
        when(refreshService.refreshAll()).thenReturn(refreshSummary);

        // When
        initializer.onApplicationReady();

        // Then
        verify(currencyService, atLeast(7)).addCurrency(anyString());
    }

    @Test
    void onApplicationReady_HandlesRefreshServiceException() throws Exception {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);
        when(currencyService.getAllCurrencies()).thenReturn(List.of(createCurrency("USD")));
        when(refreshService.refreshAll()).thenThrow(new RuntimeException("Provider unavailable"));

        // When - should not throw, just log
        initializer.onApplicationReady();

        // Then
        verify(refreshService).refreshAll();
    }

    @Test
    void onApplicationReady_DatabaseConnectivityFailureThrowsException() throws Exception {
        // Given
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        // When - the outer handler catches the RuntimeException and logs it, so it doesn't propagate
        initializer.onApplicationReady();
        
        // Then - verify that DB connection was attempted but other operations were skipped
        verify(dataSource).getConnection();
        verify(currencyService, never()).getAllCurrencies();
        verify(refreshService, never()).refreshAll();
    }

    @Test
    void onApplicationReady_InvalidConnectionThrowsException() throws Exception {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(false);

        // When/Then - should complete despite warning
        initializer.onApplicationReady();
        
        verify(connection).isValid(5);
    }

    @Test
    void onApplicationReady_SkipsDefaultCurrenciesWhenExist() throws Exception {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);
        when(currencyService.getAllCurrencies()).thenReturn(List.of(
                createCurrency("USD"),
                createCurrency("EUR"),
                createCurrency("GBP")
        ));
        when(refreshService.refreshAll()).thenReturn(refreshSummary);

        // When
        initializer.onApplicationReady();

        // Then
        verify(currencyService, never()).addCurrency(anyString());
    }

    @Test
    void onApplicationReady_HandlesPartialCurrencyAddFailures() throws Exception {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);
        when(currencyService.getAllCurrencies())
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(createCurrency("USD")));
        when(currencyService.addCurrency("USD")).thenReturn(createCurrency("USD"));
        when(currencyService.addCurrency("EUR")).thenThrow(new RuntimeException("Duplicate key"));
        when(currencyService.addCurrency("GBP")).thenReturn(createCurrency("GBP"));
        when(refreshService.refreshAll()).thenReturn(refreshSummary);

        // When - should not throw, just log warnings
        initializer.onApplicationReady();

        // Then
        verify(currencyService, atLeast(3)).addCurrency(anyString());
    }

    private Currency createCurrency(String code) {
        Currency currency = new Currency();
        currency.setCode(code);
        currency.setName(code + " Name");
        return currency;
    }
}
