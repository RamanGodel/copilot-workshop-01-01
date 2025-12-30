package com.example.workshop.provider.external;

import com.example.workshop.provider.ProviderRatesResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class FixerIoProviderWireMockTest {

    private WireMockServer server;

    @BeforeEach
    void setUp() {
        server = new WireMockServer(0);
        server.start();
        configureFor("localhost", server.port());
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void fetchLatestRates_shouldMapSuccessResponse() {
        server.stubFor(get(urlPathEqualTo("/latest"))
                .withQueryParam("access_key", equalTo("key"))
                .withQueryParam("base", equalTo("EUR"))  // Provider always uses EUR for free plan
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true,\"timestamp\":1735689600,\"base\":\"EUR\",\"rates\":{\"USD\":1.1}}")));

        FixerIoProvider provider = new FixerIoProvider(new RestTemplateBuilder(), server.baseUrl(), "key");
        Optional<ProviderRatesResponse> resp = provider.fetchLatestRates("USD");

        assertThat(resp).isPresent();
        assertThat(resp.get().getBase()).isEqualTo("USD");
        // Since provider converts from EUR to USD, the response should have EUR as target
        assertThat(resp.get().getRates()).isNotEmpty();
    }
}

