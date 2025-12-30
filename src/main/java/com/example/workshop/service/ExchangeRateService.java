package com.example.workshop.service;

import com.example.workshop.config.CacheConfig;
import com.example.workshop.dto.ExchangeRateRequestDTO;
import com.example.workshop.dto.ExchangeRateResponseDTO;
import com.example.workshop.exception.CurrencyNotFoundException;
import com.example.workshop.exception.ExchangeRateNotFoundException;
import com.example.workshop.model.Currency;
import com.example.workshop.model.ExchangeRate;
import com.example.workshop.repository.CurrencyRepository;
import com.example.workshop.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for ExchangeRate operations.
 * Handles business logic for exchange rate calculations and historical data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;

    /**
     * Get exchange rate and calculate conversion.
     *
     * @param request the exchange rate request containing amount, from, and to currencies
     * @return the exchange rate response with calculated result
     * @throws CurrencyNotFoundException if either currency is not found
     * @throws ExchangeRateNotFoundException if no exchange rate is available
     */
    @Cacheable(value = CacheConfig.LATEST_RATES_CACHE, key = "#request.from + '_' + #request.to")
    @Transactional(readOnly = true)
    public ExchangeRateResponseDTO getExchangeRate(ExchangeRateRequestDTO request) {
        log.info("Getting exchange rate from {} to {} for amount {} (cache miss)",
                request.getFrom(), request.getTo(), request.getAmount());

        // Find currencies
        Currency baseCurrency = currencyRepository.findByCode(request.getFrom())
                .orElseThrow(() -> new CurrencyNotFoundException("Currency not found: " + request.getFrom()));

        Currency targetCurrency = currencyRepository.findByCode(request.getTo())
                .orElseThrow(() -> new CurrencyNotFoundException("Currency not found: " + request.getTo()));

        // Find latest exchange rate
        ExchangeRate exchangeRate = exchangeRateRepository
                .findTopByBaseCurrencyAndTargetCurrencyOrderByTimestampDesc(baseCurrency, targetCurrency)
                .orElseThrow(() -> new ExchangeRateNotFoundException(
                        "No exchange rate found for " + request.getFrom() + " to " + request.getTo()));

        // Calculate result
        BigDecimal result = request.getAmount()
                .multiply(exchangeRate.getRate())
                .setScale(2, RoundingMode.HALF_UP);

        return ExchangeRateResponseDTO.builder()
                .amount(request.getAmount())
                .from(request.getFrom())
                .to(request.getTo())
                .rate(exchangeRate.getRate())
                .result(result)
                .timestamp(exchangeRate.getTimestamp())
                .build();
    }

    /**
     * Save a new exchange rate to the database.
     *
     * @param rate the exchange rate to save
     * @return the saved exchange rate
     */
    @CacheEvict(value = CacheConfig.LATEST_RATES_CACHE, allEntries = true)
    @Transactional
    public ExchangeRate saveExchangeRate(ExchangeRate rate) {
        log.debug("Saving exchange rate: {} -> {}, rate: {} (cache evicted)",
                rate.getBaseCurrency().getCode(),
                rate.getTargetCurrency().getCode(),
                rate.getRate());

        return exchangeRateRepository.save(rate);
    }

    /**
     * Save a new exchange rate with currency codes.
     *
     * @param baseCurrencyCode the base currency code
     * @param targetCurrencyCode the target currency code
     * @param rate the exchange rate value
     * @param timestamp the timestamp of the rate
     * @return the saved exchange rate
     * @throws CurrencyNotFoundException if either currency is not found
     */
    @CacheEvict(value = CacheConfig.LATEST_RATES_CACHE, allEntries = true)
    @Transactional
    public ExchangeRate saveExchangeRate(String baseCurrencyCode, String targetCurrencyCode,
                                         BigDecimal rate, LocalDateTime timestamp) {
        log.info("Saving exchange rate: {} -> {}, rate: {} (cache evicted)", baseCurrencyCode, targetCurrencyCode, rate);

        Currency baseCurrency = currencyRepository.findByCode(baseCurrencyCode)
                .orElseThrow(() -> new CurrencyNotFoundException("Base currency not found: " + baseCurrencyCode));

        Currency targetCurrency = currencyRepository.findByCode(targetCurrencyCode)
                .orElseThrow(() -> new CurrencyNotFoundException("Target currency not found: " + targetCurrencyCode));

        ExchangeRate exchangeRate = ExchangeRate.builder()
                .baseCurrency(baseCurrency)
                .targetCurrency(targetCurrency)
                .rate(rate)
                .timestamp(timestamp)
                .build();

        return exchangeRateRepository.save(exchangeRate);
    }

    /**
     * Get historical exchange rates between two currencies since a specific date.
     *
     * @param fromCode the base currency code
     * @param toCode the target currency code
     * @param since the start date/time
     * @return list of historical exchange rates
     * @throws CurrencyNotFoundException if either currency is not found
     */
    @Transactional(readOnly = true)
    public List<ExchangeRate> getHistoricalRates(String fromCode, String toCode, LocalDateTime since) {
        log.info("Getting historical rates from {} to {} since {}", fromCode, toCode, since);

        Currency baseCurrency = currencyRepository.findByCode(fromCode)
                .orElseThrow(() -> new CurrencyNotFoundException("Currency not found: " + fromCode));

        Currency targetCurrency = currencyRepository.findByCode(toCode)
                .orElseThrow(() -> new CurrencyNotFoundException("Currency not found: " + toCode));

        return exchangeRateRepository
                .findByBaseCurrencyAndTargetCurrencyAndTimestampAfter(baseCurrency, targetCurrency, since)
                .stream()
                .sorted((r1, r2) -> r2.getTimestamp().compareTo(r1.getTimestamp())) // Latest first
                .collect(Collectors.toList());
    }

    /**
     * Get historical exchange rates within a time range.
     *
     * @param fromCode the base currency code
     * @param toCode the target currency code
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of exchange rates in the time range
     * @throws CurrencyNotFoundException if either currency is not found
     */
    @Transactional(readOnly = true)
    public List<ExchangeRate> getRatesInTimeRange(String fromCode, String toCode,
                                                   LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Getting rates from {} to {} between {} and {}", fromCode, toCode, startTime, endTime);

        Currency baseCurrency = currencyRepository.findByCode(fromCode)
                .orElseThrow(() -> new CurrencyNotFoundException("Currency not found: " + fromCode));

        Currency targetCurrency = currencyRepository.findByCode(toCode)
                .orElseThrow(() -> new CurrencyNotFoundException("Currency not found: " + toCode));

        return exchangeRateRepository.findRatesInTimeRange(baseCurrency, targetCurrency, startTime, endTime);
    }

    /**
     * Get the latest exchange rate between two currencies.
     *
     * @param fromCode the base currency code
     * @param toCode the target currency code
     * @return Optional containing the latest exchange rate if found
     */
    @Transactional(readOnly = true)
    public Optional<ExchangeRate> getLatestRate(String fromCode, String toCode) {
        log.debug("Getting latest rate from {} to {}", fromCode, toCode);

        Optional<Currency> baseCurrency = currencyRepository.findByCode(fromCode);
        Optional<Currency> targetCurrency = currencyRepository.findByCode(toCode);

        if (baseCurrency.isEmpty() || targetCurrency.isEmpty()) {
            return Optional.empty();
        }

        return exchangeRateRepository
                .findTopByBaseCurrencyAndTargetCurrencyOrderByTimestampDesc(
                        baseCurrency.get(), targetCurrency.get());
    }

    /**
     * Get all exchange rates for a base currency.
     *
     * @param baseCurrencyCode the base currency code
     * @return list of exchange rates
     * @throws CurrencyNotFoundException if the currency is not found
     */
    @Transactional(readOnly = true)
    public List<ExchangeRate> getRatesForBaseCurrency(String baseCurrencyCode) {
        log.debug("Getting all rates for base currency: {}", baseCurrencyCode);

        Currency baseCurrency = currencyRepository.findByCode(baseCurrencyCode)
                .orElseThrow(() -> new CurrencyNotFoundException("Currency not found: " + baseCurrencyCode));

        return exchangeRateRepository.findByBaseCurrency(baseCurrency);
    }

    /**
     * Get the latest exchange rates for all target currencies from a base currency.
     *
     * @param baseCurrencyCode the base currency code
     * @return list of latest exchange rates
     * @throws CurrencyNotFoundException if the currency is not found
     */
    @Transactional(readOnly = true)
    public List<ExchangeRate> getLatestRatesForBaseCurrency(String baseCurrencyCode) {
        log.info("Getting latest rates for base currency: {}", baseCurrencyCode);

        Currency baseCurrency = currencyRepository.findByCode(baseCurrencyCode)
                .orElseThrow(() -> new CurrencyNotFoundException("Currency not found: " + baseCurrencyCode));

        return exchangeRateRepository.findLatestRatesForBaseCurrency(baseCurrency.getId());
    }

    /**
     * Calculate trend percentage between two exchange rates.
     *
     * @param oldRate the older exchange rate
     * @param newRate the newer exchange rate
     * @return the percentage change
     */
    public BigDecimal calculateTrendPercentage(ExchangeRate oldRate, ExchangeRate newRate) {
        if (oldRate == null || newRate == null) {
            throw new IllegalArgumentException("Exchange rates cannot be null");
        }

        BigDecimal oldValue = oldRate.getRate();
        BigDecimal newValue = newRate.getRate();

        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Old rate cannot be zero");
        }

        // Calculate percentage change: ((new - old) / old) * 100
        return newValue.subtract(oldValue)
                .divide(oldValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Count exchange rates between two currencies.
     *
     * @param fromCode the base currency code
     * @param toCode the target currency code
     * @return the count of exchange rates
     */
    @Transactional(readOnly = true)
    public long countRates(String fromCode, String toCode) {
        Optional<Currency> baseCurrency = currencyRepository.findByCode(fromCode);
        Optional<Currency> targetCurrency = currencyRepository.findByCode(toCode);

        if (baseCurrency.isEmpty() || targetCurrency.isEmpty()) {
            return 0;
        }

        return exchangeRateRepository.countByBaseCurrencyAndTargetCurrency(
                baseCurrency.get(), targetCurrency.get());
    }
}

