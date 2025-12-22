package com.example.workshop.provider;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderUnavailableExceptionTest {

    @Test
    void shouldKeepMessage() {
        ProviderUnavailableException ex = new ProviderUnavailableException("boom");
        assertThat(ex.getMessage()).isEqualTo("boom");
    }

    @Test
    void shouldKeepCause() {
        RuntimeException root = new RuntimeException("root");
        ProviderUnavailableException ex = new ProviderUnavailableException("boom", root);
        assertThat(ex.getCause()).isSameAs(root);
    }
}

