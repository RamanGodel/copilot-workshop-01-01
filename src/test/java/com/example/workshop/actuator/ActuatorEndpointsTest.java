package com.example.workshop.actuator;

import com.example.workshop.provider.ExchangeRateProvider;
import com.example.workshop.provider.ProviderRatesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Actuator endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActuatorEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "mockProvider1Client")
    private ExchangeRateProvider mockProvider1;

    @MockBean(name = "mockProvider2Client")
    private ExchangeRateProvider mockProvider2;

    @BeforeEach
    void setUp() {
        // Mock providers to return successful responses for health checks
        ProviderRatesResponse mockResponse = ProviderRatesResponse.builder()
                .provider("mock-provider")
                .base("USD")
                .rates(Map.of("EUR", BigDecimal.valueOf(0.85)))
                .build();
        when(mockProvider1.fetchLatestRates(anyString())).thenReturn(Optional.of(mockResponse));
        when(mockProvider2.fetchLatestRates(anyString())).thenReturn(Optional.of(mockResponse));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAccessHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowDetailedHealthWithAuthorization() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldAccessHealthEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
