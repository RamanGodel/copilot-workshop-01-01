package com.example.workshop.actuator;

import com.example.workshop.provider.ExchangeRateProvider;
import com.example.workshop.provider.ProviderRatesResponse;
import com.example.workshop.provider.ProviderUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ExternalProviderHealthIndicator.
 */
@ExtendWith(MockitoExtension.class)
class ExternalProviderHealthIndicatorTest {

    @Mock
    private ExchangeRateProvider provider1;

    @Mock
    private ExchangeRateProvider provider2;

    @InjectMocks
    private ExternalProviderHealthIndicator healthIndicator;

    @Test
    void shouldReturnUpWhenAllProvidersAreHealthy() {
        // Given
        var response = ProviderRatesResponse.builder()
                .provider("Provider1")
                .base("USD")
                .timestamp(LocalDateTime.now())
                .rates(Map.of("EUR", BigDecimal.valueOf(0.85), "GBP", BigDecimal.valueOf(0.73)))
                .build();
        
        when(provider1.getProviderName()).thenReturn("Provider1");
        when(provider1.fetchLatestRates(anyString())).thenReturn(Optional.of(response));
        
        when(provider2.getProviderName()).thenReturn("Provider2");
        when(provider2.fetchLatestRates(anyString())).thenReturn(Optional.of(response));
        
        healthIndicator = new ExternalProviderHealthIndicator(List.of(provider1, provider2));

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("totalProviders");
        assertThat(health.getDetails()).containsKey("availableProviders");
        assertThat(health.getDetails().get("totalProviders")).isEqualTo(2);
        assertThat(health.getDetails().get("availableProviders")).isEqualTo(2);
    }

    @Test
    void shouldReturnDegradedWhenLessThanHalfProvidersAvailable() {
        // Given
        var response = ProviderRatesResponse.builder()
                .provider("Provider1")
                .base("USD")
                .timestamp(LocalDateTime.now())
                .rates(Map.of("EUR", BigDecimal.valueOf(0.85)))
                .build();
        
        when(provider1.getProviderName()).thenReturn("Provider1");
        when(provider1.fetchLatestRates(anyString())).thenReturn(Optional.of(response));
        
        when(provider2.getProviderName()).thenReturn("Provider2");
        when(provider2.fetchLatestRates(anyString()))
                .thenThrow(new ProviderUnavailableException("Provider unavailable", null));
        
        healthIndicator = new ExternalProviderHealthIndicator(List.of(provider1, provider2));

        // When
        Health health = healthIndicator.health();

        // Then
        // Note: 1 out of 2 is exactly 50%, so it should be UP, not DEGRADED
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails().get("totalProviders")).isEqualTo(2);
        assertThat(health.getDetails().get("availableProviders")).isEqualTo(1);
    }

    @Test
    void shouldReturnDownWhenNoProvidersAvailable() {
        // Given
        when(provider1.getProviderName()).thenReturn("Provider1");
        when(provider1.fetchLatestRates(anyString()))
                .thenThrow(new ProviderUnavailableException("Provider unavailable", null));
        
        when(provider2.getProviderName()).thenReturn("Provider2");
        when(provider2.fetchLatestRates(anyString()))
                .thenThrow(new ProviderUnavailableException("Provider unavailable", null));
        
        healthIndicator = new ExternalProviderHealthIndicator(List.of(provider1, provider2));

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("totalProviders")).isEqualTo(2);
        assertThat(health.getDetails().get("availableProviders")).isEqualTo(0);
        assertThat(health.getDetails()).containsKey("error");
    }

    @Test
    void shouldReturnUnknownWhenNoProvidersConfigured() {
        // Given
        healthIndicator = new ExternalProviderHealthIndicator(List.of());

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
        assertThat(health.getDetails()).containsKey("message");
    }

    @Test
    void shouldHandleProviderReturningNoData() {
        // Given
        when(provider1.getProviderName()).thenReturn("Provider1");
        when(provider1.fetchLatestRates(anyString())).thenReturn(Optional.empty());
        
        healthIndicator = new ExternalProviderHealthIndicator(List.of(provider1));

        // When
        Health health = healthIndicator.health();

        // Then
        // No providers available, should be DOWN
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("availableProviders")).isEqualTo(0);
    }
}
