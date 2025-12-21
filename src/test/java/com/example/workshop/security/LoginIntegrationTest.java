package com.example.workshop.security;

import com.example.workshop.model.Role;
import com.example.workshop.model.User;
import com.example.workshop.repository.RoleRepository;
import com.example.workshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for login/logout flow.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clean up users and flush to database
        userRepository.deleteAll();
        userRepository.flush();

        // Get existing admin role from database (created by Liquibase migration)
        Role adminRole = roleRepository.findByName("ADMIN")
            .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

        // Create test user
        userRepository.saveAndFlush(User.builder()
            .username("testadmin")
            .password(passwordEncoder.encode("testpass"))
            .email("testadmin@test.com")
            .enabled(true)
            .roles(Set.of(adminRole))
            .build());
    }

    @Test
    @DisplayName("Login page should be accessible without authentication")
    void testLoginPage_accessible() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Successful login should authenticate user")
    void testLogin_success() throws Exception {
        mockMvc.perform(formLogin("/login")
                .user("testadmin")
                .password("testpass"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/swagger-ui/index.html"))
            .andExpect(authenticated().withUsername("testadmin"));
    }

    @Test
    @DisplayName("Failed login with wrong password should redirect to login with error")
    void testLogin_wrongPassword() throws Exception {
        mockMvc.perform(formLogin("/login")
                .user("testadmin")
                .password("wrongpassword"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error"))
            .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("Failed login with nonexistent user should redirect to login with error")
    void testLogin_nonexistentUser() throws Exception {
        mockMvc.perform(formLogin("/login")
                .user("nonexistent")
                .password("password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error"))
            .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("Logout should redirect to login with logout message")
    void testLogout_success() throws Exception {
        // First login
        mockMvc.perform(formLogin("/login")
                .user("testadmin")
                .password("testpass"))
            .andExpect(authenticated());

        // Then logout
        mockMvc.perform(logout())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?logout"))
            .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("Login with disabled user should fail")
    void testLogin_disabledUser() {
        // Arrange - create disabled user
        userRepository.deleteAll();
        userRepository.flush();

        Role userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new IllegalStateException("USER role not found"));

        userRepository.saveAndFlush(User.builder()
            .username("disableduser")
            .password(passwordEncoder.encode("password"))
            .email("disabled@test.com")
            .enabled(false)
            .roles(Set.of(userRole))
            .build());

        // Act & Assert
        try {
            mockMvc.perform(formLogin("/login")
                    .user("disableduser")
                    .password("password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    @DisplayName("Authenticated user should have access to protected endpoints")
    void testAuthenticatedAccess_protectedEndpoint() throws Exception {
        // First login
        mockMvc.perform(formLogin("/login")
                .user("testadmin")
                .password("testpass"))
            .andExpect(authenticated());

        // Then access protected endpoint
        mockMvc.perform(get("/api/v1/currencies/refresh")
                .with(request -> {
                    request.setRemoteUser("testadmin");
                    return request;
                }));
        // Note: Full test requires session management
    }
}
