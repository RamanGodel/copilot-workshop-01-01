package com.example.workshop.repository;

import com.example.workshop.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Role entity.
 * Provides database access methods for role operations.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by its name.
     *
     * @param name the role name (e.g., "USER", "PREMIUM_USER", "ADMIN")
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(String name);

    /**
     * Check if a role exists by name.
     *
     * @param name the role name
     * @return true if role exists, false otherwise
     */
    boolean existsByName(String name);
}

