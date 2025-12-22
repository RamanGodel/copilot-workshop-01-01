package com.example.workshop.provider.mock;

import com.example.workshop.provider.ProviderRatesResponse;
import com.example.workshop.provider.ProviderUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class MockProvider2ClientTest {

    @Test
    void fetchLatestRates_shouldMapSuccessfulResponse() {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        String baseUrl = "http://mock2";
        MockProvider2Client client = new MockProvider2Client(new RestTemplateBuilder() {
            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        }, baseUrl);

        server.expect(requestTo("http://mock2/api/rates?from=USD"))
                .andRespond(withSuccess(
                        "{\"success\":true,\"source\":\"USD\",\"lastUpdate\":1735689600,\"data\":[" +
                                "{\"currencyCode\":\"EUR\",\"exchangeRate\":0.91,\"description\":\"Euro\"}," +
                                "{\"currencyCode\":\"JPY\",\"exchangeRate\":140.123456,\"description\":\"Japanese Yen\"}" +
                                "]}",
                        MediaType.APPLICATION_JSON));

        Optional<ProviderRatesResponse> resp = client.fetchLatestRates("usd");

        assertThat(resp).isPresent();
        assertThat(resp.get().getProvider()).isEqualTo(MockProvider2Client.PROVIDER_NAME);
        assertThat(resp.get().getBase()).isEqualTo("USD");
        assertThat(resp.get().getRates()).containsKey("EUR");

        server.verify();
    }

    @Test
    void fetchLatestRates_shouldReturnEmpty_whenDataEmpty() {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        String baseUrl = "http://mock2";
        MockProvider2Client client = new MockProvider2Client(new RestTemplateBuilder() {
            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        }, baseUrl);

        server.expect(requestTo("http://mock2/api/rates?from=USD"))
                .andRespond(withSuccess(
                        "{\"success\":true,\"source\":\"USD\",\"lastUpdate\":1735689600,\"data\":[]}",
                        MediaType.APPLICATION_JSON));

        assertThat(client.fetchLatestRates("USD")).isEmpty();

        server.verify();
    }

    @Test
    void fetchLatestRates_shouldThrowProviderUnavailable_whenSuccessFalse() {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        String baseUrl = "http://mock2";
        MockProvider2Client client = new MockProvider2Client(new RestTemplateBuilder() {
            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        }, baseUrl);

        server.expect(requestTo("http://mock2/api/rates?from=USD"))
                .andRespond(withSuccess(
                        "{\"success\":false,\"source\":\"USD\",\"lastUpdate\":1735689600,\"data\":[]}",
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchLatestRates("USD"))
                .isInstanceOf(ProviderUnavailableException.class);

        server.verify();
    }
}
