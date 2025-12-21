package com.example.workshop.security;

import com.example.workshop.model.Role;
import com.example.workshop.model.User;
import com.example.workshop.repository.CurrencyRepository;
import com.example.workshop.repository.RoleRepository;
import com.example.workshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security integration tests for endpoint access control.
 * Tests authentication and authorization rules defined in SecurityConfig.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CurrencyRepository currencyRepository;

    private Role adminRole;
    private Role premiumUserRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Clean up users and flush to database
        userRepository.deleteAll();
        userRepository.flush();

        // Get existing roles from database (created by Liquibase migration)
        adminRole = roleRepository.findByName("ADMIN")
            .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));
        premiumUserRole = roleRepository.findByName("PREMIUM_USER")
            .orElseThrow(() -> new IllegalStateException("PREMIUM_USER role not found"));
        userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new IllegalStateException("USER role not found"));

        // Create test users
        userRepository.saveAndFlush(User.builder()
            .username("admin")
            .password(passwordEncoder.encode("admin123"))
            .email("admin@test.com")
            .enabled(true)
            .roles(Set.of(adminRole))
            .build());

        userRepository.saveAndFlush(User.builder()
            .username("premium")
            .password(passwordEncoder.encode("premium123"))
            .email("premium@test.com")
            .enabled(true)
            .roles(Set.of(premiumUserRole))
            .build());

        userRepository.saveAndFlush(User.builder()
            .username("user")
            .password(passwordEncoder.encode("user123"))
            .email("user@test.com")
            .enabled(true)
            .roles(Set.of(userRole))
            .build());

        // Ensure the currency code used in tests doesn't already exist
        currencyRepository.findByCode("ZZZ").ifPresent(currencyRepository::delete);
        currencyRepository.flush();
    }

    // ===== Public Endpoints Tests =====

    @Test
    @DisplayName("GET /api/v1/currencies - should be accessible without authentication")
    @WithAnonymousUser
    void testGetAllCurrencies_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/currencies"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/currencies/exchange-rates - should be accessible without authentication")
    @WithAnonymousUser
    void testGetExchangeRates_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/exchange-rates")
                .param("from", "USD")
                .param("to", "EUR")
                .param("amount", "100"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Swagger UI - should be accessible without authentication")
    @WithAnonymousUser
    void testSwaggerUI_withoutAuth() throws Exception {
        // springdoc commonly redirects /swagger-ui.html -> /swagger-ui/index.html
        mockMvc.perform(get("/swagger-ui.html"))
            .andExpect(status().isFound());
    }

    // ===== Admin-only Endpoints Tests =====

    @Test
    @DisplayName("POST /api/v1/currencies - should require ADMIN role")
    @WithAnonymousUser
    void testAddCurrency_withoutAuth_shouldRedirect() throws Exception {
        mockMvc.perform(post("/api/v1/currencies")
                .param("currency", "ZZZ"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("POST /api/v1/currencies - regular USER should be forbidden")
    @WithMockUser(username = "user", roles = {"USER"})
    void testAddCurrency_withUserRole_shouldBeForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/currencies")
                .param("currency", "ZZZ"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/currencies - PREMIUM_USER should be forbidden")
    @WithMockUser(username = "premium", roles = {"PREMIUM_USER"})
    void testAddCurrency_withPremiumRole_shouldBeForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/currencies")
                .param("currency", "ZZZ"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/currencies - ADMIN should have access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAddCurrency_withAdminRole_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/v1/currencies")
                .param("currency", "ZZZ"))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/currencies/refresh - should require ADMIN role")
    @WithAnonymousUser
    void testRefreshRates_withoutAuth_shouldRedirect() throws Exception {
        mockMvc.perform(post("/api/v1/currencies/refresh"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("POST /api/v1/currencies/refresh - regular USER should be forbidden")
    @WithMockUser(username = "user", roles = {"USER"})
    void testRefreshRates_withUserRole_shouldBeForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/currencies/refresh"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/currencies/refresh - ADMIN should have access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testRefreshRates_withAdminRole_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/v1/currencies/refresh"))
            .andExpect(status().isOk());
    }

    // ===== Premium/Admin Endpoints Tests =====

    @Test
    @DisplayName("GET /api/v1/currencies/trends - should require authentication")
    @WithAnonymousUser
    void testGetTrends_withoutAuth_shouldRedirect() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "USD")
                .param("to", "EUR")
                .param("period", "1D"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("GET /api/v1/currencies/trends - regular USER should be forbidden")
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetTrends_withUserRole_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "USD")
                .param("to", "EUR")
                .param("period", "1D"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/currencies/trends - PREMIUM_USER should have access")
    @WithMockUser(username = "premium", roles = {"PREMIUM_USER"})
    void testGetTrends_withPremiumRole_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "USD")
                .param("to", "EUR")
                .param("period", "1D"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/currencies/trends - ADMIN should have access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetTrends_withAdminRole_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/v1/currencies/trends")
                .param("from", "USD")
                .param("to", "EUR")
                .param("period", "1D"))
            .andExpect(status().isOk());
    }

    // ===== Actuator Endpoints Tests =====

    @Test
    @DisplayName("Actuator health - should be publicly accessible (Docker healthcheck)")
    @WithAnonymousUser
    void testActuatorHealth_withoutAuth_shouldSucceed() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Actuator health - regular USER should be allowed")
    @WithMockUser(username = "user", roles = {"USER"})
    void testActuatorHealth_withUserRole_shouldSucceed() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }
}
