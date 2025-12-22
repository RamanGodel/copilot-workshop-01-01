package com.example.workshop.provider.mock;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Provider adapter for mock-exchange-service-1.
 * Endpoint: GET {baseUrl}/rates?base={currency}
 */
@Slf4j
@Component
public class MockProvider1Client implements ExchangeRateProvider {

    public static final String PROVIDER_NAME = "mock-provider-1";

    private final RestTemplate restTemplate;
    private final URI baseUri;

    public MockProvider1Client(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${mock.service1.url}") String baseUrl
    ) {
        this.restTemplate = restTemplateBuilder.build();
        this.baseUri = validateBaseUri(baseUrl);
    }

    private static URI validateBaseUri(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("mock.service1.url must not be blank");
        }

        URI uri = URI.create(baseUrl);
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("mock.service1.url must use http/https");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("mock.service1.url must be an absolute URL with a host");
        }

        return uri;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public Optional<ProviderRatesResponse> fetchLatestRates(String baseCurrencyCode) {
        String base = baseCurrencyCode == null ? null : baseCurrencyCode.toUpperCase();

        if (base == null || base.isBlank()) {
            throw new IllegalArgumentException("baseCurrencyCode must not be blank");
        }

        String url = UriComponentsBuilder.fromUri(baseUri)
                .pathSegment("rates")
                .queryParam("base", base)
                .toUriString();

        try {
            ResponseEntity<MockProvider1RatesDto> entity = restTemplate.getForEntity(url, MockProvider1RatesDto.class);
            MockProvider1RatesDto body = entity.getBody();

            if (!entity.getStatusCode().is2xxSuccessful()) {
                throw new ProviderUnavailableException(
                        "Provider " + getProviderName() + " returned status " + entity.getStatusCode());
            }

            if (body == null) {
                throw new ProviderUnavailableException("Provider " + getProviderName() + " returned empty body");
            }

            if (body.getRates() == null || body.getRates().isEmpty()) {
                return Optional.empty();
            }

            LocalDateTime ts;
            try {
                ts = LocalDateTime.parse(body.getTimestamp());
            } catch (DateTimeParseException e) {
                throw new ProviderUnavailableException(
                        "Provider " + getProviderName() + " returned invalid timestamp: " + body.getTimestamp(), e);
            }

            return Optional.of(ProviderRatesResponse.builder()
                    .provider(getProviderName())
                    .base(body.getBase() == null ? base : body.getBase())
                    .timestamp(ts)
                    .rates(body.getRates())
                    .build());
        } catch (RestClientException e) {
            log.warn("Provider {} call failed: {}", getProviderName(), e.getMessage());
            throw new ProviderUnavailableException("Provider " + getProviderName() + " call failed", e);
        }
    }
}
