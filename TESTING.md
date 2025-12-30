# Code Quality and Testing Guide

This document describes the code quality standards, testing practices, and how to run various quality checks for the Copilot Workshop project.

## Table of Contents

1. [Overview](#overview)
2. [Code Quality Tools](#code-quality-tools)
3. [Testing Strategy](#testing-strategy)
4. [Maven Profiles](#maven-profiles)
5. [Running Quality Checks](#running-quality-checks)
6. [Coverage Reports](#coverage-reports)
7. [Build Scripts](#build-scripts)
8. [CI/CD Integration](#cicd-integration)

## Overview

This project maintains high code quality standards through:
- **Static Analysis**: CheckStyle, PMD, SpotBugs
- **Code Coverage**: JaCoCo (target: 80% line coverage, 70% branch coverage)
- **Mutation Testing**: PiTest (target: 70% mutation coverage)
- **Comprehensive Tests**: Unit tests, integration tests, controller tests, repository tests

## Code Quality Tools

### CheckStyle

CheckStyle enforces Google Java Style Guide conventions.

**Configuration**: `checkstyle.xml`

**Run manually**:
```bash
mvn checkstyle:check
```

**Key rules enforced**:
- Naming conventions (UpperCamelCase for classes, lowerCamelCase for methods)
- Maximum line length: 120 characters
- Maximum method length: 150 lines
- Maximum parameter count: 7
- Proper whitespace and indentation

### PMD

PMD detects common programming flaws like unused variables, empty catch blocks, unnecessary object creation.

**Configuration**: `pmd-ruleset.xml`

**Run manually**:
```bash
mvn pmd:check
```

**Rule categories enabled**:
- Best Practices
- Code Style
- Design
- Error Prone
- Performance
- Security

### SpotBugs

SpotBugs performs bytecode analysis to find potential bugs.

**Run manually**:
```bash
mvn spotbugs:check
```

**Detection categories**:
- Null pointer dereferences
- Resource leaks
- Security vulnerabilities
- Thread safety issues

## Testing Strategy

### Test Organization

Tests are organized by type and purpose:

```
src/test/java/
├── com/example/workshop/
│   ├── actuator/           # Actuator endpoint tests
│   ├── controller/         # Controller layer tests (@WebMvcTest)
│   ├── repository/         # Repository layer tests (@DataJpaTest)
│   ├── service/            # Service layer unit tests
│   ├── provider/           # External provider tests (with WireMock)
│   ├── security/           # Security integration tests
│   ├── validation/         # Validator tests
│   ├── performance/        # Performance tests
│   └── ApplicationTests.java  # Context load test
```

### Test Types

#### Unit Tests
- Test individual components in isolation
- Use Mockito for mocking dependencies
- Fast execution
- Target: 100% coverage for business logic

**Example naming**: `ExchangeRateServiceTest.java`

#### Integration Tests
- Test multiple components together
- Use real database (H2 or TestContainers)
- Use WireMock for external API mocking
- Slower execution

**Example naming**: `*IntegrationTest.java`

#### Controller Tests
- Use `@WebMvcTest` for focused controller testing
- Mock service layer
- Test HTTP layer, validation, security

#### Repository Tests
- Use `@DataJpaTest` for JPA layer testing
- Test custom queries
- Verify database interactions

### Testing Frameworks

- **JUnit 5**: Test framework
- **Mockito**: Mocking framework
- **AssertJ**: Fluent assertions
- **WireMock**: HTTP service mocking
- **TestContainers**: Docker-based integration tests (optional)
- **REST Assured**: REST API testing
- **Spring Test**: Spring-specific testing support

## Maven Profiles

### Development Profile (dev)
```bash
mvn clean install -Pdev
```
- Default profile
- Runs all tests
- Skip static analysis (for speed)

### Test Profile (test)
```bash
mvn clean install -Ptest
```
- Optimized for testing
- Runs all tests with coverage

### Production Profile (prod)
```bash
mvn clean package -Pprod
```
- Skips tests
- Optimized for deployment

### Quality Profile (quality)
```bash
mvn clean verify -Pquality
```
- Runs CheckStyle, PMD, SpotBugs
- Enforces code quality standards

### Coverage Profile (coverage)
```bash
mvn test -Pcoverage
```
- Generates JaCoCo coverage reports
- Reports saved to `target/site/jacoco/`

### Mutation Profile (mutation)
```bash
mvn test -Pmutation
```
- Runs PiTest mutation testing
- Reports saved to `target/pit-reports/`
- **Note**: This is slow, run only when needed

## Running Quality Checks

### Quick Test Run
```bash
# Run unit tests only
mvn test

# Run all tests including integration tests
mvn verify
```

### Full Quality Check
```bash
# Run all quality checks
mvn clean verify -Pquality

# Or use the build script
./build.sh --quality
```

### Generate Coverage Report
```bash
# Generate JaCoCo coverage report
mvn clean test jacoco:report -Pcoverage

# View report at: target/site/jacoco/index.html
```

### Run Mutation Testing
```bash
# Run PiTest (slow - only run periodically)
mvn clean test -Pmutation org.pitest:pitest-maven:mutationCoverage

# View report at: target/pit-reports/index.html
```

## Coverage Reports

### JaCoCo Coverage

JaCoCo measures line and branch coverage.

**Current Metrics**:
- Line Coverage: ~79% (991/1257 lines covered)
- Branch Coverage: ~18% (216/1228 branches covered)

**Configured Thresholds**:
- Line Coverage: 75%
- Branch Coverage: 15%

**Reports**:
- HTML: `target/site/jacoco/index.html`
- XML: `target/site/jacoco/jacoco.xml`

**View coverage**:
```bash
# Generate report
mvn test jacoco:report -Pcoverage

# Open in browser
open target/site/jacoco/index.html  # macOS
xdg-open target/site/jacoco/index.html  # Linux
```

### PiTest Mutation Coverage

PiTest measures the effectiveness of tests by introducing mutations.

**Threshold**: 70% mutation coverage

**Reports**:
- HTML: `target/pit-reports/index.html`

**View mutation report**:
```bash
# Generate report (slow!)
mvn test -Pmutation org.pitest:pitest-maven:mutationCoverage

# Open in browser
open target/pit-reports/index.html  # macOS
xdg-open target/pit-reports/index.html  # Linux
```

## Build Scripts

### build.sh

Comprehensive build script with options.

```bash
# Basic build
./build.sh

# Build with quality checks
./build.sh --quality

# Build with coverage
./build.sh --coverage

# Build without tests
./build.sh --skip-tests

# Build with mutation testing (slow!)
./build.sh --mutation

# Use specific profile
./build.sh --profile=prod

# Show help
./build.sh --help
```

### test.sh

Focused test execution script.

```bash
# Run all tests
./test.sh --all

# Run unit tests only
./test.sh --unit

# Run integration tests only
./test.sh --integration

# Run with coverage
./test.sh --all --coverage

# Run with mutation testing
./test.sh --all --mutation

# Show help
./test.sh --help
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 21
      uses: actions/setup-java@v2
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Build and Test
      run: mvn clean verify -Pquality,coverage
    
    - name: Upload Coverage Report
      uses: codecov/codecov-action@v2
      with:
        files: target/site/jacoco/jacoco.xml
```

### Quality Gates

The build will fail if:
- Any CheckStyle violations are found
- Any PMD violations (priority 1-3) are found
- Any SpotBugs issues are found
- Line coverage < 80%
- Branch coverage < 70%
- Any test fails

### Suppressing Violations

**CheckStyle**: Use comments
```java
// CHECKSTYLE.OFF: LineLength
public void methodWithLongSignature() { }
// CHECKSTYLE.ON: LineLength
```

**PMD**: Use annotations
```java
@SuppressWarnings("PMD.LongVariable")
private String thisIsAVeryLongVariableName;
```

**SpotBugs**: Use annotations
```java
@SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
public void methodThatMightReturnNull() { }
```

## Best Practices

### Writing Tests

1. **Use descriptive test names**
   ```java
   @Test
   @DisplayName("Should return exchange rate when currencies exist")
   void shouldReturnExchangeRateWhenCurrenciesExist() { }
   ```

2. **Follow AAA pattern** (Arrange, Act, Assert)
   ```java
   @Test
   void testExample() {
       // Arrange
       Currency usd = createUsdCurrency();
       
       // Act
       ExchangeRate rate = service.getRate(usd, eur);
       
       // Assert
       assertThat(rate).isNotNull();
   }
   ```

3. **Use AssertJ for fluent assertions**
   ```java
   assertThat(result)
       .isNotNull()
       .hasFieldOrPropertyWithValue("code", "USD")
       .extracting("rate")
       .isInstanceOf(BigDecimal.class);
   ```

4. **Mock external dependencies**
   ```java
   @Mock
   private ExchangeRateRepository repository;
   
   @InjectMocks
   private ExchangeRateService service;
   ```

### Code Quality

1. **Keep methods small** (< 150 lines)
2. **Limit method parameters** (< 7 parameters)
3. **Avoid deep nesting** (< 3 levels)
4. **Use meaningful names**
5. **Remove unused code**
6. **Handle exceptions properly**
7. **Close resources** (use try-with-resources)

## Troubleshooting

### Tests Failing

```bash
# Run specific test
mvn test -Dtest=ExchangeRateServiceTest

# Run with debug output
mvn test -X

# Skip failing tests temporarily (not recommended)
mvn test -Dmaven.test.failure.ignore=true
```

### Coverage Too Low

1. Check which classes lack coverage:
   ```bash
   mvn jacoco:report
   # Open target/site/jacoco/index.html
   ```

2. Add tests for uncovered code
3. Consider excluding generated code or configuration classes

### Static Analysis Violations

1. Run specific tool to see violations:
   ```bash
   mvn checkstyle:check
   mvn pmd:check
   mvn spotbugs:check
   ```

2. Fix violations according to tool reports
3. Use suppressions sparingly for false positives

## Summary

- **Always run tests** before committing: `mvn test`
- **Run quality checks** before merging: `./build.sh --quality`
- **Check coverage** regularly: `./test.sh --coverage`
- **Run mutation tests** periodically: `./test.sh --mutation`
- **Maintain standards**: 0 violations, 80%+ coverage

For questions or issues, contact the development team.
