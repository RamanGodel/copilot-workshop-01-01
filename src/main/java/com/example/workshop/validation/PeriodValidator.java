package com.example.workshop.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for the @ValidPeriod annotation.
 * Validates that period strings follow the format: [number][H|D|M|Y]
 * Examples: 12H (12 hours), 10D (10 days), 3M (3 months), 1Y (1 year)
 */
public class PeriodValidator implements ConstraintValidator<ValidPeriod, String> {

    private static final Pattern PERIOD_PATTERN = Pattern.compile("^\\d+[HDMY]$");

    @Override
    public void initialize(ValidPeriod constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        return PERIOD_PATTERN.matcher(value).matches();
    }
}

