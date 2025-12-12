package com.example.workshop.repository;

import com.example.workshop.model.Currency;
import com.example.workshop.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ExchangeRate entity.
 * Provides database access methods for exchange rate operations.
 */
@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    /**
     * Find all exchange rates between two currencies.
     *
     * @param baseCurrency   the base currency
     * @param targetCurrency the target currency
     * @return list of exchange rates
     */
    List<ExchangeRate> findByBaseCurrencyAndTargetCurrency(Currency baseCurrency, Currency targetCurrency);

    /**
     * Find the most recent exchange rate between two currencies.
     *
     * @param baseCurrency   the base currency
     * @param targetCurrency the target currency
     * @return Optional containing the most recent exchange rate if found
     */
    Optional<ExchangeRate> findTopByBaseCurrencyAndTargetCurrencyOrderByTimestampDesc(
            Currency baseCurrency, Currency targetCurrency);

    /**
     * Find all exchange rates between two currencies after a specific timestamp.
     *
     * @param baseCurrency   the base currency
     * @param targetCurrency the target currency
     * @param since          the timestamp to start from
     * @return list of exchange rates
     */
    List<ExchangeRate> findByBaseCurrencyAndTargetCurrencyAndTimestampAfter(
            Currency baseCurrency, Currency targetCurrency, LocalDateTime since);

    /**
     * Find all exchange rates for a base currency.
     *
     * @param baseCurrency the base currency
     * @return list of exchange rates
     */
    List<ExchangeRate> findByBaseCurrency(Currency baseCurrency);

    /**
     * Find the latest exchange rate for each target currency from a given base currency.
     *
     * @param baseCurrencyId the base currency ID
     * @return list of latest exchange rates
     */
    @Query("""
            SELECT e FROM ExchangeRate e
            WHERE e.baseCurrency.id = :baseCurrencyId
            AND e.timestamp = (
                SELECT MAX(e2.timestamp)
                FROM ExchangeRate e2
                WHERE e2.baseCurrency.id = e.baseCurrency.id
                AND e2.targetCurrency.id = e.targetCurrency.id
            )
            ORDER BY e.targetCurrency.code
            """)
    List<ExchangeRate> findLatestRatesForBaseCurrency(@Param("baseCurrencyId") Long baseCurrencyId);

    /**
     * Find exchange rates within a time range.
     *
     * @param baseCurrency   the base currency
     * @param targetCurrency the target currency
     * @param startTime      the start of the time range
     * @param endTime        the end of the time range
     * @return list of exchange rates
     */
    @Query("""
            SELECT e FROM ExchangeRate e
            WHERE e.baseCurrency = :baseCurrency
            AND e.targetCurrency = :targetCurrency
            AND e.timestamp BETWEEN :startTime AND :endTime
            ORDER BY e.timestamp DESC
            """)
    List<ExchangeRate> findRatesInTimeRange(
            @Param("baseCurrency") Currency baseCurrency,
            @Param("targetCurrency") Currency targetCurrency,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Count exchange rates between two currencies.
     *
     * @param baseCurrency   the base currency
     * @param targetCurrency the target currency
     * @return count of exchange rates
     */
    long countByBaseCurrencyAndTargetCurrency(Currency baseCurrency, Currency targetCurrency);
}

