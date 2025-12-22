package com.example.workshop.provider.external;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FixerIoProviderTest {

    @Test
    void fetchLatestRates_shouldReturnEmpty_whenAccessKeyMissing() {
        FixerIoProvider provider = new FixerIoProvider(new RestTemplateBuilder(), "https://example", "");
        assertThat(provider.fetchLatestRates("USD")).isEmpty();
    }

    @Test
    void fetchLatestRates_shouldRejectBlankBaseCurrency() {
        FixerIoProvider provider = new FixerIoProvider(new RestTemplateBuilder(), "https://example", "key");
        assertThatThrownBy(() -> provider.fetchLatestRates("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getProviderName_isStable() {
        FixerIoProvider provider = new FixerIoProvider(new RestTemplateBuilder(), "https://example", "");
        assertThat(provider.getProviderName()).isEqualTo(FixerIoProvider.PROVIDER_NAME);
    }
}
