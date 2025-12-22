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

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

/**
 * External provider adapter for exchangeratesapi.io.
 */
@Slf4j
@Component
public class ExchangeRatesApiProvider implements ExchangeRateProvider {

    public static final String PROVIDER_NAME = "exchangeratesapi";

    private final RestTemplate restTemplate;
    private final URI baseUri;
    private final String accessKey;

    public ExchangeRatesApiProvider(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${provider.exchangeratesapi.base-url:https://api.exchangeratesapi.io/v1}") String baseUrl,
            @Value("${provider.exchangeratesapi.access-key:}") String accessKey
    ) {
        this.restTemplate = restTemplateBuilder.build();
        this.baseUri = validateBaseUri(baseUrl);
        this.accessKey = accessKey;
    }

    private static URI validateBaseUri(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("provider.exchangeratesapi.base-url must not be blank");
        }

        URI uri = URI.create(baseUrl);
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("provider.exchangeratesapi.base-url must use http/https");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("provider.exchangeratesapi.base-url must be an absolute URL with a host");
        }

        return uri;
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

        String url = UriComponentsBuilder.fromUri(baseUri)
                .pathSegment("latest")
                .queryParam("access_key", accessKey)
                .queryParam("base", base)
                .toUriString();

        try {
            ResponseEntity<ExchangeRatesApiRatesDto> entity =
                    restTemplate.getForEntity(url, ExchangeRatesApiRatesDto.class);
            ExchangeRatesApiRatesDto body = entity.getBody();

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

            LocalDateTime ts = body.getTimestamp() == null
                    ? LocalDateTime.now(ZoneOffset.UTC)
                    : LocalDateTime.ofInstant(Instant.ofEpochSecond(body.getTimestamp()), ZoneOffset.UTC);

            return Optional.of(ProviderRatesResponse.builder()
                    .provider(getProviderName())
                    .base(body.getBase() == null ? base : body.getBase())
                    .timestamp(ts)
                    .rates(rates)
                    .build());
        } catch (RestClientException e) {
            log.warn("Provider {} call failed: {}", getProviderName(), e.getMessage());
            throw new ProviderUnavailableException("Provider " + getProviderName() + " call failed", e);
        }
    }
}
