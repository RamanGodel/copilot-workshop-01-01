package com.example.workshop.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CorrelationIdFilter.
 */
@DisplayName("CorrelationIdFilter Tests")
class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        
        // Clear MDC before each test
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate correlation ID when not provided in header")
    void testGenerateCorrelationId() throws ServletException, IOException {
        // When
        filter.doFilter(request, response, filterChain);

        // Then
        String correlationId = response.getHeader("X-Correlation-Id");
        assertThat(correlationId).isNotNull();
        assertThat(correlationId).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    @DisplayName("Should use correlation ID from request header")
    void testUseExistingCorrelationId() throws ServletException, IOException {
        // Given
        String existingId = "test-correlation-id-123";
        request.addHeader("X-Correlation-Id", existingId);

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        String correlationId = response.getHeader("X-Correlation-Id");
        assertThat(correlationId).isEqualTo(existingId);
    }

    @Test
    @DisplayName("Should add correlation ID to response header")
    void testAddCorrelationIdToResponse() throws ServletException, IOException {
        // When
        filter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getHeader("X-Correlation-Id")).isNotNull();
    }

    @Test
    @DisplayName("Should clean up MDC after request")
    void testCleanUpMdc() throws ServletException, IOException {
        // When
        filter.doFilter(request, response, filterChain);

        // Then
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    @DisplayName("Should clean up MDC even when exception occurs")
    void testCleanUpMdcOnException() {
        // Given
        MockFilterChain errorChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) 
                    throws IOException, ServletException {
                throw new ServletException("Test exception");
            }
        };

        // When/Then
        try {
            filter.doFilter(request, response, errorChain);
        } catch (Exception e) {
            // Expected
        }

        // MDC should still be cleaned up
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    @DisplayName("Should handle empty correlation ID header")
    void testEmptyCorrelationIdHeader() throws ServletException, IOException {
        // Given
        request.addHeader("X-Correlation-Id", "");

        // When
        filter.doFilter(request, response, filterChain);

        // Then - should generate new ID
        String correlationId = response.getHeader("X-Correlation-Id");
        assertThat(correlationId).isNotNull();
        assertThat(correlationId).isNotEmpty();
        assertThat(correlationId).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    @DisplayName("Should handle whitespace-only correlation ID header")
    void testWhitespaceCorrelationIdHeader() throws ServletException, IOException {
        // Given
        request.addHeader("X-Correlation-Id", "   ");

        // When
        filter.doFilter(request, response, filterChain);

        // Then - should generate new ID
        String correlationId = response.getHeader("X-Correlation-Id");
        assertThat(correlationId).isNotNull();
        assertThat(correlationId).isNotEmpty();
        assertThat(correlationId).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }
}
