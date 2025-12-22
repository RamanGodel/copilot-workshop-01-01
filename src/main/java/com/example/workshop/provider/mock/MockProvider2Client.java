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

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provider adapter for mock-exchange-service-2.
 * Endpoint: GET {baseUrl}/api/rates?from={currency}
 */
@Slf4j
@Component
public class MockProvider2Client implements ExchangeRateProvider {

    public static final String PROVIDER_NAME = "mock-provider-2";

    private final RestTemplate restTemplate;
    private final URI baseUri;

    public MockProvider2Client(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${mock.service2.url}") String baseUrl
    ) {
        this.restTemplate = restTemplateBuilder.build();
        this.baseUri = validateBaseUri(baseUrl);
    }

    private static URI validateBaseUri(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("mock.service2.url must not be blank");
        }

        URI uri = URI.create(baseUrl);
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("mock.service2.url must use http/https");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("mock.service2.url must be an absolute URL with a host");
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
                .pathSegment("api", "rates")
                .queryParam("from", base)
                .toUriString();

        try {
            ResponseEntity<MockProvider2RatesDto> entity = restTemplate.getForEntity(url, MockProvider2RatesDto.class);
            MockProvider2RatesDto body = entity.getBody();

            if (!entity.getStatusCode().is2xxSuccessful()) {
                throw new ProviderUnavailableException(
                        "Provider " + getProviderName() + " returned status " + entity.getStatusCode());
            }

            if (body == null) {
                throw new ProviderUnavailableException("Provider " + getProviderName() + " returned empty body");
            }

            if (!body.isSuccess()) {
                throw new ProviderUnavailableException("Provider " + getProviderName() + " indicated failure");
            }

            List<MockProvider2RatesDto.RateData> data = body.getData();
            if (data == null || data.isEmpty()) {
                return Optional.empty();
            }

            Map<String, BigDecimal> rates = new LinkedHashMap<>();
            for (MockProvider2RatesDto.RateData d : data) {
                if (d != null && d.getCurrencyCode() != null && d.getExchangeRate() != null) {
                    rates.put(d.getCurrencyCode().toUpperCase(), d.getExchangeRate());
                }
            }

            if (rates.isEmpty()) {
                return Optional.empty();
            }

            LocalDateTime ts = LocalDateTime.ofInstant(Instant.ofEpochSecond(body.getLastUpdate()), ZoneOffset.UTC);

            return Optional.of(ProviderRatesResponse.builder()
                    .provider(getProviderName())
                    .base(body.getSource() == null ? base : body.getSource())
                    .timestamp(ts)
                    .rates(rates)
                    .build());
        } catch (RestClientException e) {
            log.warn("Provider {} call failed: {}", getProviderName(), e.getMessage());
            throw new ProviderUnavailableException("Provider " + getProviderName() + " call failed", e);
        }
    }
}

