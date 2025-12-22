package com.example.workshop.provider.external;

import com.example.workshop.provider.ProviderRatesResponse;
import com.example.workshop.provider.ProviderUnavailableException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRatesApiProviderWireMockTest {

    private WireMockServer server;

    @BeforeEach
    void setUp() {
        server = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void fetchLatestRates_shouldMapSuccessResponse() {
        server.stubFor(get(urlPathEqualTo("/latest"))
                .withQueryParam("access_key", equalTo("key"))
                .withQueryParam("base", equalTo("USD"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true,\"timestamp\":1735689600,\"base\":\"USD\",\"rates\":{\"EUR\":0.9}}")));

        ExchangeRatesApiProvider provider = new ExchangeRatesApiProvider(new RestTemplateBuilder(), server.baseUrl(), "key");
        Optional<ProviderRatesResponse> resp = provider.fetchLatestRates("USD");

        assertThat(resp).isPresent();
        assertThat(resp.get().getBase()).isEqualTo("USD");
        assertThat(resp.get().getRates()).containsKey("EUR");
    }

    @Test
    void fetchLatestRates_shouldThrowProviderUnavailable_onServerError() {
        server.stubFor(get(urlPathEqualTo("/latest"))
                .withQueryParam("access_key", equalTo("key"))
                .withQueryParam("base", equalTo("USD"))
                .willReturn(aResponse().withStatus(500)));

        ExchangeRatesApiProvider provider = new ExchangeRatesApiProvider(new RestTemplateBuilder(), server.baseUrl(), "key");

        assertThatThrownBy(() -> provider.fetchLatestRates("USD"))
                .isInstanceOf(ProviderUnavailableException.class);
    }

    @Test
    void fetchLatestRates_shouldThrowProviderUnavailable_onInvalidJson() {
        server.stubFor(get(urlPathEqualTo("/latest"))
                .withQueryParam("access_key", equalTo("key"))
                .withQueryParam("base", equalTo("USD"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("not-json")));

        ExchangeRatesApiProvider provider = new ExchangeRatesApiProvider(new RestTemplateBuilder(), server.baseUrl(), "key");

        assertThatThrownBy(() -> provider.fetchLatestRates("USD"))
                .isInstanceOf(ProviderUnavailableException.class);
    }
}
