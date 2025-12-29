package com.example.workshop.provider.external;

import com.example.workshop.provider.ExchangeRateProvider;
import com.example.workshop.provider.ProviderRatesResponse;
import com.example.workshop.provider.ProviderUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

/**
 * External provider adapter for fixer.io.
 *
 * Uses the fixer "latest" endpoint.
 * API details vary by fixer plan (base currency may be restricted).
 */
@Slf4j
@Component
public class FixerIoProvider implements ExchangeRateProvider {

    public static final String PROVIDER_NAME = "fixer-io";

    private final RestTemplate restTemplate;
    private final URI baseUri;
    private final String accessKey;

    public FixerIoProvider(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${provider.fixer.base-url:https://data.fixer.io/api}") String baseUrl,
            @Value("${provider.fixer.access-key:}") String accessKey
    ) {
        this.restTemplate = restTemplateBuilder.build();
        this.baseUri = validateBaseUri(baseUrl);
        this.accessKey = accessKey;
    }

    private static URI validateBaseUri(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("provider.fixer.base-url must not be blank");
        }

        URI uri = URI.create(baseUrl);
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("provider.fixer.base-url must use http/https");
        }

        // Require an absolute URL (host present) to avoid SSRF/open-redirect foot-guns.
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("provider.fixer.base-url must be an absolute URL with a host");
        }

        return uri;
    }

    @PostConstruct
    void logConfiguration() {
        boolean enabled = accessKey != null && !accessKey.isBlank();
        log.info("Provider {} configured: enabled={}, baseUrl={}", getProviderName(), enabled, baseUri);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public Optional<ProviderRatesResponse> fetchLatestRates(String baseCurrencyCode) {
        if (accessKey == null || accessKey.isBlank()) {
            // Not configured: treat as "disabled".
            return Optional.empty();
        }

        String base = baseCurrencyCode == null ? null : baseCurrencyCode.toUpperCase();
        if (base == null || base.isBlank()) {
            throw new IllegalArgumentException("baseCurrencyCode must not be blank");
        }

        // IMPORTANT: Free plan only supports EUR as base currency
        // Always request EUR and convert to requested base if needed
        boolean needsConversion = !"EUR".equalsIgnoreCase(base);

        String url = UriComponentsBuilder.fromUri(baseUri)
                .pathSegment("latest")
                .queryParam("access_key", accessKey)
                .queryParam("base", "EUR")  // Always use EUR for free plan
                .toUriString();

        try {
            ResponseEntity<FixerIoRatesDto> entity = restTemplate.getForEntity(url, FixerIoRatesDto.class);
            FixerIoRatesDto body = entity.getBody();

            if (!entity.getStatusCode().is2xxSuccessful()) {
                throw new ProviderUnavailableException(
                        "Provider " + getProviderName() + " returned status " + entity.getStatusCode());
            }

            if (body == null) {
                throw new ProviderUnavailableException("Provider " + getProviderName() + " returned empty body");
            }

            if (!body.isSuccess()) {
                String detail = body.getError() == null ? "unknown error" : body.getError().getInfo();
                throw new ProviderUnavailableException("Provider " + getProviderName() + " error: " + detail);
            }

            Map<String, java.math.BigDecimal> rates = body.getRates();
            if (rates == null || rates.isEmpty()) {
                return Optional.empty();
            }

            // Convert rates from EUR base to requested base if needed
            if (needsConversion) {
                rates = convertRatesFromEurTo(base, rates);
                if (rates.isEmpty()) {
                    return Optional.empty();
                }
            }

            LocalDateTime ts = body.getTimestamp() == null
                    ? LocalDateTime.now(ZoneOffset.UTC)
                    : LocalDateTime.ofInstant(Instant.ofEpochSecond(body.getTimestamp()), ZoneOffset.UTC);

            return Optional.of(ProviderRatesResponse.builder()
                    .provider(getProviderName())
                    .base(base)  // Use requested base, not EUR
                    .timestamp(ts)
                    .rates(rates)
                    .build());
        } catch (RestClientException e) {
            log.warn("Provider {} call failed: {}", getProviderName(), e.getMessage());
            throw new ProviderUnavailableException("Provider " + getProviderName() + " call failed", e);
        }
    }

    /**
     * Convert rates from EUR base to another base currency.
     * Formula: newRate = oldRate / baseRate
     * 
     * @param newBase Target base currency
     * @param eurRates Rates with EUR as base
     * @return Converted rates with newBase as base
     */
    private Map<String, java.math.BigDecimal> convertRatesFromEurTo(String newBase, Map<String, java.math.BigDecimal> eurRates) {
        // Get the rate of the new base currency against EUR
        java.math.BigDecimal baseRate = eurRates.get(newBase.toUpperCase());
        
        if (baseRate == null || baseRate.compareTo(java.math.BigDecimal.ZERO) == 0) {
            log.warn("Cannot convert to base {}: rate not found or zero", newBase);
            return Map.of();
        }
        
        // Convert all rates
        Map<String, java.math.BigDecimal> convertedRates = new java.util.HashMap<>();
        
        // Add the base currency itself with rate 1.0
        convertedRates.put(newBase.toUpperCase(), java.math.BigDecimal.ONE);
        
        // Convert other currencies: newRate = oldRate / baseRate
        for (Map.Entry<String, java.math.BigDecimal> entry : eurRates.entrySet()) {
            String currency = entry.getKey();
            if (!currency.equalsIgnoreCase(newBase)) {
                java.math.BigDecimal newRate = entry.getValue()
                        .divide(baseRate, 6, java.math.RoundingMode.HALF_UP);
                convertedRates.put(currency, newRate);
            }
        }
        
        log.debug("Converted {} rates from EUR to {} base", convertedRates.size(), newBase);
        return convertedRates;
    }
}
