package com.example.workshop.config;

import com.example.workshop.model.Role;
import com.example.workshop.model.User;
import com.example.workshop.repository.RoleRepository;
import com.example.workshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Configuration class for initializing default users and roles in the database.
 * Creates admin, premium user, and regular user accounts with encrypted passwords.
 * Only runs when NOT in test profile.
 */
@Slf4j
@Configuration
@Profile("!test")  // Do not run in test profile
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * CommandLineRunner that initializes default users on application startup.
     * Creates three users with different roles:
     * - admin / admin123 (ADMIN role)
     * - premium / premium123 (PREMIUM_USER role)
     * - user / user123 (USER role)
     *
     * @return CommandLineRunner that performs initialization
     */
    @Bean
    public CommandLineRunner initializeDefaultUsers() {
        return args -> {
            log.info("Starting default users initialization...");

            // Check if users already exist
            if (userRepository.count() > 0) {
                log.info("Users already exist in database. Skipping initialization.");
                return;
            }

            // Get roles from database (they should be created by Liquibase migration)
            Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found in database"));
            Role premiumUserRole = roleRepository.findByName("PREMIUM_USER")
                .orElseThrow(() -> new IllegalStateException("PREMIUM_USER role not found in database"));
            Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("USER role not found in database"));

            // Create admin user
            User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@example.com")
                .enabled(true)
                .roles(Set.of(adminRole))
                .build();
            userRepository.save(admin);
            log.info("Created admin user: username='admin', email='admin@example.com'");

            // Create premium user
            User premiumUser = User.builder()
                .username("premium")
                .password(passwordEncoder.encode("premium123"))
                .email("premium@example.com")
                .enabled(true)
                .roles(Set.of(premiumUserRole))
                .build();
            userRepository.save(premiumUser);
            log.info("Created premium user: username='premium', email='premium@example.com'");

            // Create regular user
            User regularUser = User.builder()
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .email("user@example.com")
                .enabled(true)
                .roles(Set.of(userRole))
                .build();
            userRepository.save(regularUser);
            log.info("Created regular user: username='user', email='user@example.com'");

            log.info("Default users initialization completed successfully.");
        };
    }
}

