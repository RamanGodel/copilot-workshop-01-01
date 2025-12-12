package com.example.mockservice1;

import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
public class RatesController {

    private final Random random = new Random();

    @GetMapping("/rates")
    public RatesResponse getRates(@RequestParam(defaultValue = "USD") String base) {
        RatesResponse response = new RatesResponse();
        response.setBase(base.toUpperCase());
        response.setTimestamp(LocalDateTime.now().toString());
        response.setRates(generateRandomRates(base));
        return response;
    }

    private Map<String, BigDecimal> generateRandomRates(String base) {
        Map<String, BigDecimal> rates = new HashMap<>();

        // Common currencies with random rates
        rates.put("EUR", generateRandomRate(0.85, 0.95));
        rates.put("USD", base.equals("USD") ? BigDecimal.ONE : generateRandomRate(0.90, 1.10));
        rates.put("GBP", generateRandomRate(0.75, 0.85));
        rates.put("JPY", generateRandomRate(110.0, 150.0));
        rates.put("CHF", generateRandomRate(0.88, 0.98));
        rates.put("CAD", generateRandomRate(1.20, 1.40));
        rates.put("AUD", generateRandomRate(1.30, 1.50));
        rates.put("CNY", generateRandomRate(6.50, 7.50));
        rates.put("INR", generateRandomRate(70.0, 85.0));
        rates.put("BRL", generateRandomRate(4.80, 5.50));

        return rates;
    }

    private BigDecimal generateRandomRate(double min, double max) {
        double randomValue = min + (max - min) * random.nextDouble();
        return BigDecimal.valueOf(randomValue).setScale(4, RoundingMode.HALF_UP);
    }

    @Data
    public static class RatesResponse {
        private String base;
        private String timestamp;
        private Map<String, BigDecimal> rates;
    }
}

