package com.example.workshop.service;

import com.example.workshop.model.Role;
import com.example.workshop.model.User;
import com.example.workshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomUserDetailsService.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder()
            .id(1L)
            .name("ADMIN")
            .description("Administrator role")
            .build();

        userRole = Role.builder()
            .id(2L)
            .name("USER")
            .description("Regular user role")
            .build();

        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .password("encodedPassword123")
            .email("test@example.com")
            .enabled(true)
            .roles(Set.of(adminRole, userRole))
            .build();
    }

    @Test
    @DisplayName("loadUserByUsername - should load user successfully")
    void testLoadUserByUsername_success() {
        // Arrange
        when(userRepository.findByUsernameWithRoles("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword123");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();

        // Verify authorities
        assertThat(userDetails.getAuthorities()).hasSize(2);
        assertThat(userDetails.getAuthorities())
            .extracting(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");

        verify(userRepository, times(1)).findByUsernameWithRoles("testuser");
    }

    @Test
    @DisplayName("loadUserByUsername - should throw exception when user not found")
    void testLoadUserByUsername_userNotFound() {
        // Arrange
        when(userRepository.findByUsernameWithRoles("nonexistent"))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found with username: nonexistent");

        verify(userRepository, times(1)).findByUsernameWithRoles("nonexistent");
    }

    @Test
    @DisplayName("loadUserByUsername - should handle disabled user")
    void testLoadUserByUsername_disabledUser() {
        // Arrange
        testUser.setEnabled(false);
        when(userRepository.findByUsernameWithRoles("testuser"))
            .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("loadUserByUsername - should map single role correctly")
    void testLoadUserByUsername_singleRole() {
        // Arrange
        User userWithSingleRole = User.builder()
            .id(2L)
            .username("simpleuser")
            .password("password")
            .email("simple@example.com")
            .enabled(true)
            .roles(Set.of(userRole))
            .build();

        when(userRepository.findByUsernameWithRoles("simpleuser"))
            .thenReturn(Optional.of(userWithSingleRole));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("simpleuser");

        // Assert
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("loadUserByUsername - should handle user with no roles")
    void testLoadUserByUsername_noRoles() {
        // Arrange
        User userWithNoRoles = User.builder()
            .id(3L)
            .username("noroleuser")
            .password("password")
            .email("norole@example.com")
            .enabled(true)
            .roles(Set.of())
            .build();

        when(userRepository.findByUsernameWithRoles("noroleuser"))
            .thenReturn(Optional.of(userWithNoRoles));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("noroleuser");

        // Assert
        assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("loadUserByUsername - should add ROLE_ prefix to authorities")
    void testLoadUserByUsername_rolePrefix() {
        // Arrange
        Role premiumRole = Role.builder()
            .id(3L)
            .name("PREMIUM_USER")
            .description("Premium user role")
            .build();

        User premiumUser = User.builder()
            .id(4L)
            .username("premiumuser")
            .password("password")
            .email("premium@example.com")
            .enabled(true)
            .roles(Set.of(premiumRole))
            .build();

        when(userRepository.findByUsernameWithRoles("premiumuser"))
            .thenReturn(Optional.of(premiumUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("premiumuser");

        // Assert
        assertThat(userDetails.getAuthorities())
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_PREMIUM_USER");
    }
}

