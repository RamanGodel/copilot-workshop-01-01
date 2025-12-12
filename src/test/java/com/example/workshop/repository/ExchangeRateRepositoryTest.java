package com.example.workshop.repository;

import com.example.workshop.model.Currency;
import com.example.workshop.model.ExchangeRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for ExchangeRateRepository.
 */
@DataJpaTest
@DisplayName("ExchangeRateRepository Tests")
class ExchangeRateRepositoryTest {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Currency usd;
    private Currency eur;
    private Currency gbp;

    @BeforeEach
    void setUp() {
        usd = Currency.builder().code("USD").name("US Dollar").build();
        eur = Currency.builder().code("EUR").name("Euro").build();
        gbp = Currency.builder().code("GBP").name("British Pound").build();

        entityManager.persist(usd);
        entityManager.persist(eur);
        entityManager.persist(gbp);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should save and retrieve exchange rate")
    void shouldSaveAndRetrieveExchangeRate() {
        // Given
        ExchangeRate rate = ExchangeRate.builder()
                .baseCurrency(usd)
                .targetCurrency(eur)
                .rate(BigDecimal.valueOf(0.85))
                .timestamp(LocalDateTime.now())
                .build();

        // When
        ExchangeRate saved = exchangeRateRepository.save(rate);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBaseCurrency().getCode()).isEqualTo("USD");
        assertThat(saved.getTargetCurrency().getCode()).isEqualTo("EUR");
        assertThat(saved.getRate()).isEqualByComparingTo(BigDecimal.valueOf(0.85));
    }

    @Test
    @DisplayName("Should find exchange rates by base and target currency")
    void shouldFindByBaseCurrencyAndTargetCurrency() {
        // Given
        ExchangeRate rate1 = createRate(usd, eur, 0.85, LocalDateTime.now().minusHours(2));
        ExchangeRate rate2 = createRate(usd, eur, 0.86, LocalDateTime.now().minusHours(1));
        ExchangeRate rate3 = createRate(usd, gbp, 0.75, LocalDateTime.now());

        entityManager.persist(rate1);
        entityManager.persist(rate2);
        entityManager.persist(rate3);
        entityManager.flush();

        // When
        List<ExchangeRate> rates = exchangeRateRepository
                .findByBaseCurrencyAndTargetCurrency(usd, eur);

        // Then
        assertThat(rates).hasSize(2);
        assertThat(rates).allMatch(r ->
                r.getBaseCurrency().getCode().equals("USD") &&
                r.getTargetCurrency().getCode().equals("EUR"));
    }

    @Test
    @DisplayName("Should find top rate ordered by timestamp desc")
    void shouldFindTopRateOrderedByTimestampDesc() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ExchangeRate oldRate = createRate(usd, eur, 0.85, now.minusHours(2));
        ExchangeRate newRate = createRate(usd, eur, 0.87, now);

        entityManager.persist(oldRate);
        entityManager.persist(newRate);
        entityManager.flush();

        // When
        Optional<ExchangeRate> latest = exchangeRateRepository
                .findTopByBaseCurrencyAndTargetCurrencyOrderByTimestampDesc(usd, eur);

        // Then
        assertThat(latest).isPresent();
        assertThat(latest.get().getRate()).isEqualByComparingTo(BigDecimal.valueOf(0.87));
        assertThat(latest.get().getTimestamp()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should find rates after timestamp")
    void shouldFindRatesAfterTimestamp() {
        // Given
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);
        ExchangeRate oldRate = createRate(usd, eur, 0.85, threshold.minusMinutes(30));
        ExchangeRate newRate1 = createRate(usd, eur, 0.86, threshold.plusMinutes(30));
        ExchangeRate newRate2 = createRate(usd, eur, 0.87, threshold.plusMinutes(60));

        entityManager.persist(oldRate);
        entityManager.persist(newRate1);
        entityManager.persist(newRate2);
        entityManager.flush();

        // When
        List<ExchangeRate> rates = exchangeRateRepository
                .findByBaseCurrencyAndTargetCurrencyAndTimestampAfter(usd, eur, threshold);

        // Then
        assertThat(rates).hasSize(2);
        assertThat(rates).allMatch(r -> r.getTimestamp().isAfter(threshold));
    }

    @Test
    @DisplayName("Should find rates by base currency")
    void shouldFindRatesByBaseCurrency() {
        // Given
        ExchangeRate rate1 = createRate(usd, eur, 0.85, LocalDateTime.now());
        ExchangeRate rate2 = createRate(usd, gbp, 0.75, LocalDateTime.now());
        ExchangeRate rate3 = createRate(eur, gbp, 0.88, LocalDateTime.now());

        entityManager.persist(rate1);
        entityManager.persist(rate2);
        entityManager.persist(rate3);
        entityManager.flush();

        // When
        List<ExchangeRate> rates = exchangeRateRepository.findByBaseCurrency(usd);

        // Then
        assertThat(rates).hasSize(2);
        assertThat(rates).allMatch(r -> r.getBaseCurrency().getCode().equals("USD"));
    }

    @Test
    @DisplayName("Should find rates in time range")
    void shouldFindRatesInTimeRange() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now();

        ExchangeRate beforeRange = createRate(usd, eur, 0.84, start.minusHours(1));
        ExchangeRate inRange1 = createRate(usd, eur, 0.85, start.plusHours(1));
        ExchangeRate inRange2 = createRate(usd, eur, 0.86, start.plusHours(2));
        ExchangeRate afterRange = createRate(usd, eur, 0.87, end.plusHours(1));

        entityManager.persist(beforeRange);
        entityManager.persist(inRange1);
        entityManager.persist(inRange2);
        entityManager.persist(afterRange);
        entityManager.flush();

        // When
        List<ExchangeRate> rates = exchangeRateRepository
                .findRatesInTimeRange(usd, eur, start, end);

        // Then
        assertThat(rates).hasSize(2);
        assertThat(rates).allMatch(r ->
                !r.getTimestamp().isBefore(start) && !r.getTimestamp().isAfter(end));
    }

    @Test
    @DisplayName("Should count rates by base and target currency")
    void shouldCountRatesByBaseCurrencyAndTargetCurrency() {
        // Given
        entityManager.persist(createRate(usd, eur, 0.85, LocalDateTime.now().minusHours(3)));
        entityManager.persist(createRate(usd, eur, 0.86, LocalDateTime.now().minusHours(2)));
        entityManager.persist(createRate(usd, eur, 0.87, LocalDateTime.now().minusHours(1)));
        entityManager.persist(createRate(usd, gbp, 0.75, LocalDateTime.now()));
        entityManager.flush();

        // When
        long count = exchangeRateRepository.countByBaseCurrencyAndTargetCurrency(usd, eur);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find latest rates for base currency")
    void shouldFindLatestRatesForBaseCurrency() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // USD -> EUR: multiple rates
        entityManager.persist(createRate(usd, eur, 0.85, now.minusHours(2)));
        entityManager.persist(createRate(usd, eur, 0.87, now)); // Latest

        // USD -> GBP: multiple rates
        entityManager.persist(createRate(usd, gbp, 0.74, now.minusHours(1)));
        entityManager.persist(createRate(usd, gbp, 0.76, now)); // Latest

        entityManager.flush();

        // When
        List<ExchangeRate> latestRates = exchangeRateRepository
                .findLatestRatesForBaseCurrency(usd.getId());

        // Then
        assertThat(latestRates).hasSize(2);

        // Verify we got the latest rate for each target currency
        ExchangeRate eurRate = latestRates.stream()
                .filter(r -> r.getTargetCurrency().getCode().equals("EUR"))
                .findFirst()
                .orElseThrow();
        assertThat(eurRate.getRate()).isEqualByComparingTo(BigDecimal.valueOf(0.87));

        ExchangeRate gbpRate = latestRates.stream()
                .filter(r -> r.getTargetCurrency().getCode().equals("GBP"))
                .findFirst()
                .orElseThrow();
        assertThat(gbpRate.getRate()).isEqualByComparingTo(BigDecimal.valueOf(0.76));
    }

    private ExchangeRate createRate(Currency base, Currency target, double rate, LocalDateTime timestamp) {
        return ExchangeRate.builder()
                .baseCurrency(base)
                .targetCurrency(target)
                .rate(BigDecimal.valueOf(rate))
                .timestamp(timestamp)
                .build();
    }
}

