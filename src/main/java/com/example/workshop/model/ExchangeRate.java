package com.example.workshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing an exchange rate between two currencies.
 * Contains the rate, base currency, target currency, and timestamp.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exchange_rate", indexes = {
    @Index(name = "idx_exchange_rate_composite",
           columnList = "base_currency_id, target_currency_id, timestamp")
})
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Base currency is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_currency_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_exchange_rate_base_currency"))
    private Currency baseCurrency;

    @NotNull(message = "Target currency is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_currency_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_exchange_rate_target_currency"))
    private Currency targetCurrency;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rate must be greater than 0")
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;

    @NotNull(message = "Timestamp is required")
    @Column(nullable = false)
    private LocalDateTime timestamp;
}

