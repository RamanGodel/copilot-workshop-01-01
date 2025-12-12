package com.example.workshop.repository;

import com.example.workshop.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Currency entity.
 * Provides database access methods for currency operations.
 */
@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    /**
     * Find a currency by its ISO code (e.g., USD, EUR).
     *
     * @param code the currency code
     * @return Optional containing the currency if found
     */
    Optional<Currency> findByCode(String code);

    /**
     * Check if a currency exists by its code.
     *
     * @param code the currency code
     * @return true if currency exists, false otherwise
     */
    boolean existsByCode(String code);
}

