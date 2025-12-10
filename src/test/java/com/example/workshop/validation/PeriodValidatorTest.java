package com.example.workshop.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for PeriodValidator.
 */
class PeriodValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid period formats should pass validation")
    void testValidPeriodFormats() {
        // Test Hours
        TestDTO dto1 = new TestDTO("12H");
        assertThat(validator.validate(dto1)).isEmpty();

        TestDTO dto2 = new TestDTO("24H");
        assertThat(validator.validate(dto2)).isEmpty();

        // Test Days
        TestDTO dto3 = new TestDTO("1D");
        assertThat(validator.validate(dto3)).isEmpty();

        TestDTO dto4 = new TestDTO("30D");
        assertThat(validator.validate(dto4)).isEmpty();

        // Test Months
        TestDTO dto5 = new TestDTO("1M");
        assertThat(validator.validate(dto5)).isEmpty();

        TestDTO dto6 = new TestDTO("12M");
        assertThat(validator.validate(dto6)).isEmpty();

        // Test Years
        TestDTO dto7 = new TestDTO("1Y");
        assertThat(validator.validate(dto7)).isEmpty();

        TestDTO dto8 = new TestDTO("5Y");
        assertThat(validator.validate(dto8)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"10X", "ABC", "12", "H12", "1.5D", "-10D", "10d", "10 D"})
    @DisplayName("Invalid period formats should fail validation")
    void testInvalidPeriodFormats(String invalidPeriod) {
        TestDTO dto = new TestDTO(invalidPeriod);
        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Period must be in format");
    }

    @Test
    @DisplayName("Null period should fail validation when marked as NotBlank")
    void testNullPeriod() {
        TestDTO dto = new TestDTO(null);
        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Empty period should fail validation")
    void testEmptyPeriod() {
        TestDTO dto = new TestDTO("");
        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Blank period should fail validation")
    void testBlankPeriod() {
        TestDTO dto = new TestDTO("   ");
        Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Period with large numbers should pass validation")
    void testLargeNumbers() {
        TestDTO dto1 = new TestDTO("999H");
        assertThat(validator.validate(dto1)).isEmpty();

        TestDTO dto2 = new TestDTO("365D");
        assertThat(validator.validate(dto2)).isEmpty();

        TestDTO dto3 = new TestDTO("100Y");
        assertThat(validator.validate(dto3)).isEmpty();
    }

    /**
     * Test DTO class for validation testing.
     */
    private static class TestDTO {
        @ValidPeriod
        private String period;

        public TestDTO(String period) {
            this.period = period;
        }

        public String getPeriod() {
            return period;
        }
    }
}

