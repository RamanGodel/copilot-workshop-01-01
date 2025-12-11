package com.example.mockservice2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class ExchangeRateController {

    private final Random random = new Random();

    @GetMapping("/rates")
    public ExchangeRateResponse getRates(@RequestParam(defaultValue = "USD") String from) {
        ExchangeRateResponse response = new ExchangeRateResponse();
        response.setSuccess(true);
        response.setSource(from.toUpperCase());
        response.setLastUpdate(Instant.now().getEpochSecond());
        response.setData(generateRateData(from));
        return response;
    }

    private List<RateData> generateRateData(String source) {
        List<RateData> data = new ArrayList<>();

        // Different format: array of rate objects
        data.add(createRateData("EUR", generateRandomRate(0.85, 0.95)));
        data.add(createRateData("USD", source.equals("USD") ? BigDecimal.ONE : generateRandomRate(0.90, 1.10)));
        data.add(createRateData("GBP", generateRandomRate(0.75, 0.85)));
        data.add(createRateData("JPY", generateRandomRate(110.0, 150.0)));
        data.add(createRateData("CHF", generateRandomRate(0.88, 0.98)));
        data.add(createRateData("CAD", generateRandomRate(1.20, 1.40)));
        data.add(createRateData("AUD", generateRandomRate(1.30, 1.50)));
        data.add(createRateData("CNY", generateRandomRate(6.50, 7.50)));
        data.add(createRateData("INR", generateRandomRate(70.0, 85.0)));
        data.add(createRateData("MXN", generateRandomRate(16.0, 20.0)));
        data.add(createRateData("SGD", generateRandomRate(1.30, 1.40)));

        return data;
    }

    private RateData createRateData(String currency, BigDecimal rate) {
        return new RateData(currency, rate, currency + " - " + getCurrencyName(currency));
    }

    private String getCurrencyName(String code) {
        return switch (code) {
            case "EUR" -> "Euro";
            case "USD" -> "US Dollar";
            case "GBP" -> "British Pound";
            case "JPY" -> "Japanese Yen";
            case "CHF" -> "Swiss Franc";
            case "CAD" -> "Canadian Dollar";
            case "AUD" -> "Australian Dollar";
            case "CNY" -> "Chinese Yuan";
            case "INR" -> "Indian Rupee";
            case "MXN" -> "Mexican Peso";
            case "SGD" -> "Singapore Dollar";
            default -> "Unknown Currency";
        };
    }

    private BigDecimal generateRandomRate(double min, double max) {
        double randomValue = min + (max - min) * random.nextDouble();
        return BigDecimal.valueOf(randomValue).setScale(6, RoundingMode.HALF_UP);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExchangeRateResponse {
        private boolean success;
        private String source;
        private long lastUpdate;
        private List<RateData> data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateData {
        private String currencyCode;
        private BigDecimal exchangeRate;
        private String description;
    }
}

