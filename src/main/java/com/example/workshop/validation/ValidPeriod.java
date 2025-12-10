package com.example.workshop.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for period format.
 * Valid formats: 12H (hours), 10D (days), 3M (months), 1Y (years)
 */
@Documented
@Constraint(validatedBy = PeriodValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPeriod {

    String message() default "Period must be in format: [number][H|D|M|Y] (e.g., 12H, 10D, 3M, 1Y)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

