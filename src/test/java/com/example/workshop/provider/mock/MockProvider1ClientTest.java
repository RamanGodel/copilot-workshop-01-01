package com.example.workshop.provider.mock;

import com.example.workshop.provider.ProviderRatesResponse;
import com.example.workshop.provider.ProviderUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class MockProvider1ClientTest {

    @Test
    void fetchLatestRates_shouldMapSuccessfulResponse() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate restTemplate = builder.build();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        String baseUrl = "http://mock1";
        MockProvider1Client client = new MockProvider1Client(new RestTemplateBuilder() {
            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        }, baseUrl);

        server.expect(requestTo("http://mock1/rates?base=USD"))
                .andRespond(withSuccess(
                        "{\"base\":\"USD\",\"timestamp\":\"2025-01-01T00:00:00\",\"rates\":{\"EUR\":0.9,\"JPY\":140.1234}}",
                        org.springframework.http.MediaType.APPLICATION_JSON));

        Optional<ProviderRatesResponse> resp = client.fetchLatestRates("usd");

        assertThat(resp).isPresent();
        assertThat(resp.get().getProvider()).isEqualTo(MockProvider1Client.PROVIDER_NAME);
        assertThat(resp.get().getBase()).isEqualTo("USD");
        assertThat(resp.get().getRates()).containsEntry("EUR", new BigDecimal("0.9"));

        server.verify();
    }

    @Test
    void fetchLatestRates_shouldReturnEmpty_whenRatesEmpty() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate restTemplate = builder.build();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        String baseUrl = "http://mock1";
        MockProvider1Client client = new MockProvider1Client(new RestTemplateBuilder() {
            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        }, baseUrl);

        server.expect(requestTo("http://mock1/rates?base=USD"))
                .andRespond(withSuccess(
                        "{\"base\":\"USD\",\"timestamp\":\"2025-01-01T00:00:00\",\"rates\":{}}",
                        org.springframework.http.MediaType.APPLICATION_JSON));

        assertThat(client.fetchLatestRates("USD")).isEmpty();

        server.verify();
    }

    @Test
    void fetchLatestRates_shouldThrowProviderUnavailable_onServerError() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate restTemplate = builder.build();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        String baseUrl = "http://mock1";
        MockProvider1Client client = new MockProvider1Client(new RestTemplateBuilder() {
            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        }, baseUrl);

        server.expect(requestTo("http://mock1/rates?base=USD"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.fetchLatestRates("USD"))
                .isInstanceOf(ProviderUnavailableException.class);

        server.verify();
    }
}
