package com.example.workshop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a currency in the system.
 * Each currency is uniquely identified by its ISO 4217 code (e.g., USD, EUR, GBP).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Currency {

    private Long id;

    private String code;

    private String name;
}

