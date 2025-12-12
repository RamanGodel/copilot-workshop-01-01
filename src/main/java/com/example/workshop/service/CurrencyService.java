package com.example.workshop.service;

import com.example.workshop.dto.CurrencyDTO;
import com.example.workshop.exception.InvalidCurrencyCodeException;
import com.example.workshop.model.Currency;
import com.example.workshop.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for Currency operations.
 * Handles business logic and coordinates between controller and repository.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    /**
     * Get all currencies from the database.
     *
     * @return list of all currencies, sorted by code
     */
    @Transactional(readOnly = true)
    public List<Currency> getAllCurrencies() {
        log.debug("Fetching all currencies from database");

        return currencyRepository.findAll().stream()
                .sorted((c1, c2) -> c1.getCode().compareTo(c2.getCode()))
                .collect(Collectors.toList());
    }

    /**
     * Find a currency by its ISO code.
     *
     * @param code the currency code (e.g., USD, EUR)
     * @return Optional containing the currency if found
     */
    @Transactional(readOnly = true)
    public Optional<Currency> findByCode(String code) {
        log.debug("Finding currency by code: {}", code);

        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        return currencyRepository.findByCode(code.toUpperCase());
    }

    /**
     * Add a new currency to the system.
     * Validates the currency code and checks for duplicates.
     *
     * @param code the currency code to add
     * @return the created currency
     * @throws InvalidCurrencyCodeException if the code is invalid
     * @throws IllegalStateException if the currency already exists
     */
    @Transactional
    public Currency addCurrency(String code) {
        log.info("Adding new currency: {}", code);

        // Validate currency code format
        if (code == null || !code.matches("^[A-Z]{3}$")) {
            throw new InvalidCurrencyCodeException("Currency code must be 3 uppercase letters");
        }

        String currencyCode = code.toUpperCase();

        // Check for duplicates
        if (currencyRepository.existsByCode(currencyCode)) {
            log.warn("Attempted to add existing currency: {}", currencyCode);
            throw new IllegalStateException("Currency " + currencyCode + " already exists");
        }

        // Create and save currency
        Currency currency = Currency.builder()
                .code(currencyCode)
                .name(getCurrencyName(currencyCode))
                .build();

        Currency savedCurrency = currencyRepository.save(currency);
        log.info("Successfully added currency: {}", savedCurrency.getCode());

        return savedCurrency;
    }

    /**
     * Add a new currency with a specific name.
     *
     * @param code the currency code
     * @param name the currency name
     * @return the created currency
     * @throws InvalidCurrencyCodeException if the code is invalid
     * @throws IllegalStateException if the currency already exists
     */
    @Transactional
    public Currency addCurrency(String code, String name) {
        log.info("Adding new currency: {} with name: {}", code, name);

        // Validate currency code format
        if (code == null || !code.matches("^[A-Z]{3}$")) {
            throw new InvalidCurrencyCodeException("Currency code must be 3 uppercase letters");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Currency name cannot be blank");
        }

        String currencyCode = code.toUpperCase();

        // Check for duplicates
        if (currencyRepository.existsByCode(currencyCode)) {
            log.warn("Attempted to add existing currency: {}", currencyCode);
            throw new IllegalStateException("Currency " + currencyCode + " already exists");
        }

        // Create and save currency
        Currency currency = Currency.builder()
                .code(currencyCode)
                .name(name)
                .build();

        Currency savedCurrency = currencyRepository.save(currency);
        log.info("Successfully added currency: {} - {}", savedCurrency.getCode(), savedCurrency.getName());

        return savedCurrency;
    }

    /**
     * Convert Currency entity to DTO.
     *
     * @param currency the currency entity
     * @return the currency DTO
     */
    public CurrencyDTO toDTO(Currency currency) {
        return CurrencyDTO.builder()
                .id(currency.getId())
                .code(currency.getCode())
                .name(currency.getName())
                .build();
    }

    /**
     * Convert list of Currency entities to DTOs.
     *
     * @param currencies list of currency entities
     * @return list of currency DTOs
     */
    public List<CurrencyDTO> toDTOList(List<Currency> currencies) {
        return currencies.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get currency name by code (fallback for unknown currencies).
     *
     * @param code the currency code
     * @return the currency name
     */
    private String getCurrencyName(String code) {
        return switch (code) {
            case "USD" -> "US Dollar";
            case "EUR" -> "Euro";
            case "GBP" -> "British Pound";
            case "JPY" -> "Japanese Yen";
            case "CHF" -> "Swiss Franc";
            case "CAD" -> "Canadian Dollar";
            case "AUD" -> "Australian Dollar";
            case "CNY" -> "Chinese Yuan";
            case "INR" -> "Indian Rupee";
            case "PLN" -> "Polish Zloty";
            default -> code; // Return code if name is unknown
        };
    }
}

