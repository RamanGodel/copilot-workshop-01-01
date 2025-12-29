package com.example.workshop.provider;

import com.example.workshop.config.ProvidersProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Orchestrates multiple {@link ExchangeRateProvider}s.
 *
 * Strategy (default): first provider in configured order returning non-empty rates wins.
 * Providers returning Optional.empty() are treated as "no data/disabled".
 */
@Slf4j
@Service
public class ExchangeRateProviderAggregator {

    private final List<ExchangeRateProvider> orderedProviders;

    // Resilience4j
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final ScheduledExecutorService scheduler;

    private final Duration callTimeout;

    public ExchangeRateProviderAggregator(List<ExchangeRateProvider> providers, ProvidersProperties props) {
        this.orderedProviders = orderProviders(providers, props.getOrder());

        this.callTimeout = props.getCallTimeout() == null ? Duration.ofSeconds(2) : props.getCallTimeout();

        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .build();

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(Math.max(1, props.getRetryMaxAttempts()))
                .waitDuration(Duration.ofMillis(100))
                .retryExceptions(ProviderUnavailableException.class, RuntimeException.class)
                .build();

        TimeLimiterConfig tlConfig = TimeLimiterConfig.custom()
                .timeoutDuration(this.callTimeout)
                .cancelRunningFuture(true)
                .build();

        this.circuitBreakerRegistry = CircuitBreakerRegistry.of(cbConfig);
        this.retryRegistry = RetryRegistry.of(retryConfig);
        this.timeLimiterRegistry = TimeLimiterRegistry.of(tlConfig);

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "provider-time-limiter");
            t.setDaemon(true);
            return t;
        });
    }

    public AggregationResult fetchLatestRates(String baseCurrencyCode) {
        List<ProviderAttempt> attempts = new ArrayList<>();

        for (ExchangeRateProvider provider : orderedProviders) {
            String providerName = provider.getProviderName();
            log.debug("Provider attempt start: provider={}, base={}", providerName, baseCurrencyCode);
            try {
                Optional<ProviderRatesResponse> resp = callWithResilience(provider, baseCurrencyCode);

                boolean present = resp.isPresent();
                int rateCount = present && resp.get().getRates() != null ? resp.get().getRates().size() : 0;

                if (!present) {
                    log.debug("Provider attempt result: provider={}, outcome=EMPTY (disabled/no-data)", providerName);
                    attempts.add(ProviderAttempt.success(providerName, false, null));
                    continue;
                }

                if (rateCount <= 0) {
                    log.debug("Provider attempt result: provider={}, outcome=EMPTY_RATES", providerName);
                    attempts.add(ProviderAttempt.success(providerName, false, null));
                    continue;
                }

                log.info("Provider selected: provider={}, base={}, rates={}", providerName, resp.get().getBase(), rateCount);
                attempts.add(ProviderAttempt.success(providerName, true, null));
                return AggregationResult.success(resp.get(), attempts);
            } catch (ProviderUnavailableException ex) {
                log.warn("Provider attempt failed: provider={}, error={}", providerName, ex.getMessage());
                attempts.add(ProviderAttempt.failure(providerName, ex.getMessage()));
            } catch (RuntimeException ex) {
                log.warn("Provider attempt unexpected failure: provider={}, error={}", providerName, ex.getMessage());
                attempts.add(ProviderAttempt.failure(providerName, ex.getMessage()));
            }
        }

        log.info("No provider returned rates for base={}. Attempts={}", baseCurrencyCode, attempts.size());
        return AggregationResult.noData(attempts);
    }

    private Optional<ProviderRatesResponse> callWithResilience(ExchangeRateProvider provider, String baseCurrencyCode) {
        String providerName = provider.getProviderName();

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(providerName);
        Retry retry = retryRegistry.retry(providerName);
        TimeLimiter tl = timeLimiterRegistry.timeLimiter(providerName);

        Supplier<Optional<ProviderRatesResponse>> supplier = () -> provider.fetchLatestRates(baseCurrencyCode);

        // Run provider call in a future so the TimeLimiter can enforce timeout.
        Supplier<CompletionStage<Optional<ProviderRatesResponse>>> futureSupplier = () ->
                CompletableFuture.supplyAsync(supplier, ForkJoinPool.commonPool());

        try {
            CompletionStage<Optional<ProviderRatesResponse>> timed =
                    TimeLimiter.decorateCompletionStage(tl, scheduler, futureSupplier).get();

            Supplier<Optional<ProviderRatesResponse>> blocking =
                    () -> timed.toCompletableFuture().join();

            Supplier<Optional<ProviderRatesResponse>> decorated =
                    CircuitBreaker.decorateSupplier(cb, Retry.decorateSupplier(retry, blocking));

            return decorated.get();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            if (cause != null && "java.util.concurrent.TimeoutException".equals(cause.getClass().getName())) {
                throw new ProviderUnavailableException("Provider " + providerName + " timed out after " + callTimeout, cause);
            }
            throw asProviderUnavailable(providerName, cause);
        } catch (Exception ex) {
            throw asProviderUnavailable(providerName, ex);
        }
    }

    private static ProviderUnavailableException asProviderUnavailable(String providerName, Throwable ex) {
        if (ex instanceof ProviderUnavailableException pue) {
            return pue;
        }
        return new ProviderUnavailableException("Provider " + providerName + " call failed", ex);
    }

    private static List<ExchangeRateProvider> orderProviders(List<ExchangeRateProvider> providers, List<String> order) {
        if (providers == null) {
            return List.of();
        }

        if (order == null || order.isEmpty()) {
            return List.copyOf(providers);
        }

        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < order.size(); i++) {
            index.put(order.get(i), i);
        }

        List<ExchangeRateProvider> sorted = new ArrayList<>(providers);
        sorted.sort(Comparator.comparingInt(p -> index.getOrDefault(p.getProviderName(), Integer.MAX_VALUE)));
        return Collections.unmodifiableList(sorted);
    }

    @Value
    public static class AggregationResult {
        ProviderRatesResponse chosen;
        List<ProviderAttempt> attempts;

        public static AggregationResult success(ProviderRatesResponse chosen, List<ProviderAttempt> attempts) {
            return new AggregationResult(chosen, List.copyOf(attempts));
        }

        public static AggregationResult noData(List<ProviderAttempt> attempts) {
            return new AggregationResult(null, List.copyOf(attempts));
        }

        public boolean hasData() {
            return chosen != null;
        }
    }

    @Value
    public static class ProviderAttempt {
        String provider;
        boolean returnedData;
        String error;

        public static ProviderAttempt success(String provider, boolean returnedData, String error) {
            return new ProviderAttempt(provider, returnedData, error);
        }

        public static ProviderAttempt failure(String provider, String error) {
            return new ProviderAttempt(provider, false, error);
        }

        public boolean isSuccess() {
            return error == null;
        }
    }
}
