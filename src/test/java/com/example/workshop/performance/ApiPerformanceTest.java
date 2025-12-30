package com.example.workshop.performance;

import com.example.workshop.provider.ProviderRatesResponse;
import com.example.workshop.provider.external.ExchangeRatesApiProvider;
import com.example.workshop.provider.external.FixerIoProvider;
import com.example.workshop.provider.mock.MockProvider1Client;
import com.example.workshop.provider.mock.MockProvider2Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance tests for API endpoints.
 * Measures response times and throughput for critical endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("API Performance Tests")
class ApiPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeRatesApiProvider exchangeRatesApiProvider;

    @MockBean
    private FixerIoProvider fixerIoProvider;

    @MockBean
    private MockProvider1Client mockProvider1Client;

    @MockBean
    private MockProvider2Client mockProvider2Client;

    @BeforeEach
    void setUp() {
        // Mock providers to return successful responses for health checks
        ProviderRatesResponse mockResponse = ProviderRatesResponse.builder()
            .provider("Mock")
            .base("USD")
            .timestamp(LocalDateTime.now())
            .rates(Map.of("EUR", BigDecimal.valueOf(0.85)))
            .build();

        when(exchangeRatesApiProvider.fetchLatestRates(any())).thenReturn(Optional.of(mockResponse));
        when(fixerIoProvider.fetchLatestRates(any())).thenReturn(Optional.of(mockResponse));
        when(mockProvider1Client.fetchLatestRates(any())).thenReturn(Optional.of(mockResponse));
        when(mockProvider2Client.fetchLatestRates(any())).thenReturn(Optional.of(mockResponse));
    }

    @Test
    @DisplayName("Currency list endpoint should respond within 100ms")
    @WithMockUser(roles = "USER")
    void testCurrencyListPerformance() throws Exception {
        List<Long> responseTimes = new ArrayList<>();

        // Make 50 requests
        for (int i = 0; i < 50; i++) {
            Instant start = Instant.now();
            
            mockMvc.perform(get("/api/v1/currencies"))
                .andExpect(status().isOk());
            
            Duration responseTime = Duration.between(start, Instant.now());
            responseTimes.add(responseTime.toMillis());
        }

        // Calculate statistics
        double average = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long min = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        long max = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        double p95 = calculatePercentile(responseTimes, 95);

        System.out.println("Currency List Endpoint Performance:");
        System.out.println("  Average: " + String.format("%.2f", average) + "ms");
        System.out.println("  Min: " + min + "ms");
        System.out.println("  Max: " + max + "ms");
        System.out.println("  95th percentile: " + String.format("%.2f", p95) + "ms");

        // Assertions
        assertThat(average).isLessThan(100);
        assertThat(p95).isLessThan(150);
    }

    @Test
    @DisplayName("Health endpoint should respond within 50ms")
    void testHealthEndpointPerformance() throws Exception {
        List<Long> responseTimes = new ArrayList<>();

        // Make 100 requests
        for (int i = 0; i < 100; i++) {
            Instant start = Instant.now();
            
            mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
            
            Duration responseTime = Duration.between(start, Instant.now());
            responseTimes.add(responseTime.toMillis());
        }

        // Calculate statistics
        double average = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long max = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        double p95 = calculatePercentile(responseTimes, 95);

        System.out.println("Health Endpoint Performance:");
        System.out.println("  Average: " + String.format("%.2f", average) + "ms");
        System.out.println("  Max: " + max + "ms");
        System.out.println("  95th percentile: " + String.format("%.2f", p95) + "ms");

        // Health check should be very fast
        assertThat(average).isLessThan(50);
        assertThat(max).isLessThan(100);
    }

    @Test
    @Disabled("Actuator metrics endpoint not properly initialized in test context - covered by ActuatorEndpointsTest")
    @DisplayName("Metrics endpoint should respond within 100ms")
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void testMetricsEndpointPerformance() throws Exception {
        List<Long> responseTimes = new ArrayList<>();

        // Make 30 requests
        for (int i = 0; i < 30; i++) {
            Instant start = Instant.now();
            
            mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk());
            
            Duration responseTime = Duration.between(start, Instant.now());
            responseTimes.add(responseTime.toMillis());
        }

        // Calculate statistics
        double average = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double p95 = calculatePercentile(responseTimes, 95);

        System.out.println("Metrics Endpoint Performance:");
        System.out.println("  Average: " + String.format("%.2f", average) + "ms");
        System.out.println("  95th percentile: " + String.format("%.2f", p95) + "ms");

        // Metrics should be relatively fast
        assertThat(average).isLessThan(100);
        assertThat(p95).isLessThan(150);
    }

    @Test
    @DisplayName("API throughput - handle 100 requests efficiently")
    @WithMockUser(roles = "USER")
    void testApiThroughput() throws Exception {
        int requestCount = 100;
        
        Instant start = Instant.now();
        
        // Make 100 sequential requests
        for (int i = 0; i < requestCount; i++) {
            mockMvc.perform(get("/api/v1/currencies"))
                .andExpect(status().isOk());
        }
        
        Duration totalTime = Duration.between(start, Instant.now());
        double requestsPerSecond = (double) requestCount / (totalTime.toMillis() / 1000.0);

        System.out.println("API Throughput:");
        System.out.println("  Total time for " + requestCount + " requests: " + totalTime.toMillis() + "ms");
        System.out.println("  Throughput: " + String.format("%.2f", requestsPerSecond) + " req/s");
        System.out.println("  Average time per request: " + 
            String.format("%.2f", (double)totalTime.toMillis()/requestCount) + "ms");

        // Should handle at least 10 requests per second
        assertThat(requestsPerSecond).isGreaterThan(10);
    }

    /**
     * Calculate percentile value from sorted list of response times
     */
    private double calculatePercentile(List<Long> values, int percentile) {
        values.sort(Long::compareTo);
        int index = (int) Math.ceil(percentile / 100.0 * values.size()) - 1;
        return values.get(Math.max(0, Math.min(index, values.size() - 1)));
    }
}
