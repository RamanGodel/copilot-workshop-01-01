package com.example.workshop.scheduler;

import com.example.workshop.service.ExchangeRateRefreshService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Unit tests for ExchangeRateScheduler.
 */
@ExtendWith(MockitoExtension.class)
class ExchangeRateSchedulerTest {

    @Mock
    private ExchangeRateRefreshService refreshService;

    @InjectMocks
    private ExchangeRateScheduler scheduler;

    private ExchangeRateRefreshService.RefreshSummary successSummary;
    private ExchangeRateRefreshService.RefreshSummary summaryWithFailures;

    @BeforeEach
    void setUp() {
        successSummary = new ExchangeRateRefreshService.RefreshSummary(
                7, 7, 2, 42, Collections.emptyList()
        );

        summaryWithFailures = new ExchangeRateRefreshService.RefreshSummary(
                7, 7, 2, 40, List.of("Failed to save EUR->XYZ", "Failed to save USD->ABC")
        );
    }

    @Test
    void refreshExchangeRates_Success() {
        // Given
        when(refreshService.refreshAll()).thenReturn(successSummary);

        // When
        scheduler.refreshExchangeRates();

        // Then
        verify(refreshService, times(1)).refreshAll();
    }

    @Test
    void refreshExchangeRates_WithFailures() {
        // Given
        when(refreshService.refreshAll()).thenReturn(summaryWithFailures);

        // When
        scheduler.refreshExchangeRates();

        // Then
        verify(refreshService, times(1)).refreshAll();
    }

    @Test
    void refreshExchangeRates_HandlesException() {
        // Given
        when(refreshService.refreshAll()).thenThrow(new RuntimeException("Database connection failed"));

        // When - should not throw, just log
        scheduler.refreshExchangeRates();

        // Then
        verify(refreshService, times(1)).refreshAll();
    }
}
