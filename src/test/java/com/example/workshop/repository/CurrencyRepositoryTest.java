package com.example.workshop.repository;

import com.example.workshop.model.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for CurrencyRepository.
 * Uses @DataJpaTest for lightweight JPA testing with H2 in-memory database.
 */
@DataJpaTest
@DisplayName("CurrencyRepository Tests")
class CurrencyRepositoryTest {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void cleanDb() {
        currencyRepository.deleteAll();
        currencyRepository.flush();
    }

    @Test
    @DisplayName("Should save and find currency by code")
    void shouldSaveAndFindByCode() {
        // Given
        Currency currency = Currency.builder()
                .code("AAA")
                .name("Test Currency AAA")
                .build();

        // When
        entityManager.persistAndFlush(currency);
        Optional<Currency> found = currencyRepository.findByCode("AAA");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("AAA");
        assertThat(found.get().getName()).isEqualTo("Test Currency AAA");
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should return empty when currency code not found")
    void shouldReturnEmptyWhenCodeNotFound() {
        // When
        Optional<Currency> found = currencyRepository.findByCode("XYZ");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if currency exists by code")
    void shouldCheckExistsByCode() {
        // Given
        Currency currency = Currency.builder()
                .code("BBB")
                .name("Test Currency BBB")
                .build();
        entityManager.persistAndFlush(currency);

        // When & Then
        assertThat(currencyRepository.existsByCode("BBB")).isTrue();
        assertThat(currencyRepository.existsByCode("CCC")).isFalse();
    }

    @Test
    @DisplayName("Should find all currencies")
    void shouldFindAllCurrencies() {
        // Given
        Currency c1 = Currency.builder().code("CCC").name("Test Currency CCC").build();
        Currency c2 = Currency.builder().code("DDD").name("Test Currency DDD").build();
        Currency c3 = Currency.builder().code("EEE").name("Test Currency EEE").build();

        entityManager.persist(c1);
        entityManager.persist(c2);
        entityManager.persist(c3);
        entityManager.flush();

        // When
        var currencies = currencyRepository.findAll();

        // Then
        assertThat(currencies).extracting(Currency::getCode)
                .contains("CCC", "DDD", "EEE");
    }

    @Test
    @DisplayName("Should enforce unique constraint on code")
    void shouldEnforceUniqueConstraintOnCode() {
        // Given
        Currency currency1 = Currency.builder()
                .code("FFF")
                .name("Test Currency FFF")
                .build();
        entityManager.persistAndFlush(currency1);

        // When & Then
        Currency currency2 = Currency.builder()
                .code("FFF")
                .name("Test Currency FFF Duplicate")
                .build();

        // This should throw an exception due to unique constraint
        try {
            entityManager.persistAndFlush(currency2);
            // Force flush to trigger constraint violation
            assertThat(false).as("Should have thrown constraint violation").isTrue();
        } catch (Exception e) {
            // Expected exception
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("Should save currency with timestamps")
    void shouldSaveCurrencyWithTimestamps() {
        // Given
        Currency currency = Currency.builder()
                .code("GGG")
                .name("Test Currency GGG")
                .build();

        // When
        Currency saved = currencyRepository.save(currency);
        entityManager.flush();

        // Then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
    }
}
