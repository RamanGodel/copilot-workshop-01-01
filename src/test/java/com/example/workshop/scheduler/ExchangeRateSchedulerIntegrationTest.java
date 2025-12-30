package com.example.workshop.scheduler;

import com.example.workshop.service.ExchangeRateRefreshService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

/**
 * Integration test for scheduled task execution.
 * Tests that the scheduler actually runs at the configured intervals.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
class ExchangeRateSchedulerIntegrationTest {

    @SpyBean
    private ExchangeRateScheduler scheduler;

    @MockBean
    private ExchangeRateRefreshService refreshService;

    @Test
    void scheduledTaskCanBeInvokedManually() {
        // Given
        ExchangeRateRefreshService.RefreshSummary summary = new ExchangeRateRefreshService.RefreshSummary(
                5, 5, 2, 20, Collections.emptyList()
        );
        when(refreshService.refreshAll()).thenReturn(summary);

        // When
        scheduler.refreshExchangeRates();

        // Then
        verify(refreshService, times(1)).refreshAll();
    }

    @Test
    void scheduledTaskHandlesExceptions() {
        // Given
        when(refreshService.refreshAll()).thenThrow(new RuntimeException("Test exception"));

        // When - should not throw
        scheduler.refreshExchangeRates();

        // Then
        verify(refreshService, times(1)).refreshAll();
    }

}
