# Testing Guide - Phase 1

## Overview
This guide provides instructions for running tests for Phase 1 of the Currency Exchange Rates Provider Service.

## Prerequisites
- Java 21
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)

## Test Structure

### Test Packages
```
src/test/java/com/example/workshop/
├── ApplicationTests.java (Context loading)
├── controller/
│   ├── CurrencyControllerTest.java (Integration tests)
│   └── CurrencyControllerWebTest.java (Web layer tests)
├── validation/
│   └── PeriodValidatorTest.java (Validator tests)
└── exception/
    └── GlobalExceptionHandlerTest.java (Exception handler tests)
```

## Running Tests

### Option 1: Maven Command Line

#### Run All Tests
```bash
mvn clean test
```

#### Run Specific Test Class
```bash
# Web layer tests (22 tests)
mvn test -Dtest=CurrencyControllerWebTest

# Validation tests (11 tests)
mvn test -Dtest=PeriodValidatorTest

# Exception handler tests (8 tests)
mvn test -Dtest=GlobalExceptionHandlerTest

# Integration tests
mvn test -Dtest=CurrencyControllerTest
```

#### Run with Coverage Report
```bash
mvn clean test jacoco:report
```
Report will be generated at: `target/site/jacoco/index.html`

### Option 2: IDE

#### IntelliJ IDEA
1. Right-click on `src/test/java` folder
2. Select "Run 'All Tests'"

Or run individual test classes:
1. Open the test file
2. Click the green arrow next to the class name or individual test method
3. Select "Run"

#### Eclipse
1. Right-click on `src/test/java` folder
2. Select "Run As" → "JUnit Test"

#### VS Code
1. Install "Test Runner for Java" extension
2. Click on the test beaker icon in the sidebar
3. Run all tests or individual test classes

### Option 3: Gradle (if using Gradle wrapper)
```bash
./gradlew test
```

## Test Categories

### 1. CurrencyControllerWebTest (Web Layer Tests)
**22 Test Cases** covering:
- ✅ GET /api/v1/currencies - List all currencies
- ✅ POST /api/v1/currencies - Add currency (success and error scenarios)
- ✅ POST /api/v1/currencies/refresh - Refresh rates
- ✅ GET /api/v1/currencies/exchange-rates - Get exchange rate (multiple scenarios)
- ✅ GET /api/v1/currencies/trends - Get currency trends (multiple period formats)

**Key Features:**
- Uses `@WebMvcTest` for focused testing
- MockMvc for HTTP request simulation
- JSONPath assertions for response validation
- Tests both success and error scenarios

**Example Test:**
```java
@Test
@DisplayName("GET /api/v1/currencies - Should return list of currencies")
void testGetAllCurrencies_Success() throws Exception {
    mockMvc.perform(get("/api/v1/currencies")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(7))))
            .andExpect(jsonPath("$[*].code", hasItem("USD")));
}
```

### 2. PeriodValidatorTest (Validation Tests)
**11 Test Cases** covering:
- ✅ Valid period formats (12H, 10D, 3M, 1Y)
- ✅ Invalid period formats (10X, ABC, 12, etc.)
- ✅ Null/empty/blank periods
- ✅ Large number validation

**Key Features:**
- Jakarta Bean Validation API
- Parameterized tests for multiple invalid inputs
- AssertJ fluent assertions

**Example Test:**
```java
@ParameterizedTest
@ValueSource(strings = {"10X", "ABC", "12", "H12", "1.5D", "-10D"})
@DisplayName("Invalid period formats should fail validation")
void testInvalidPeriodFormats(String invalidPeriod) {
    TestDTO dto = new TestDTO(invalidPeriod);
    Set<ConstraintViolation<TestDTO>> violations = validator.validate(dto);
    assertThat(violations).isNotEmpty();
}
```

### 3. GlobalExceptionHandlerTest (Exception Handler Tests)
**8 Test Cases** covering:
- ✅ MethodArgumentNotValidException
- ✅ CurrencyNotFoundException
- ✅ InvalidCurrencyCodeException
- ✅ InvalidPeriodException
- ✅ ExchangeRateNotFoundException
- ✅ IllegalArgumentException
- ✅ Generic Exception
- ✅ Null message handling

**Key Features:**
- Mockito for mocking HttpServletRequest
- Verifies HTTP status codes
- Validates ErrorResponseDTO structure

**Example Test:**
```java
@Test
@DisplayName("Should handle CurrencyNotFoundException")
void testHandleCurrencyNotFoundException() {
    CurrencyNotFoundException exception = 
        new CurrencyNotFoundException("Currency USD not found");
    
    ResponseEntity<ErrorResponseDTO> response = 
        exceptionHandler.handleCurrencyNotFoundException(exception, request);
    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().getStatus()).isEqualTo(404);
}
```

### 4. CurrencyControllerTest (Integration Tests)
**8 Test Cases** covering:
- Full Spring context loading
- End-to-end API testing
- Real bean wiring

### 5. ApplicationTests (Context Tests)
**1 Test Case**:
- Spring Boot context loads successfully

## Expected Test Results

### Summary
- **Total Test Classes**: 5
- **Total Test Cases**: 50+
- **Expected Result**: All tests should pass ✅

### Test Execution Time
- Web layer tests: ~2-3 seconds
- Validation tests: ~1 second
- Exception handler tests: ~1 second
- Integration tests: ~3-4 seconds
- **Total**: ~7-10 seconds

## Viewing Test Results

### Maven Surefire Reports
After running tests, reports are generated at:
```
target/surefire-reports/
├── TEST-com.example.workshop.ApplicationTests.xml
├── TEST-com.example.workshop.controller.CurrencyControllerTest.xml
├── TEST-com.example.workshop.controller.CurrencyControllerWebTest.xml
├── TEST-com.example.workshop.validation.PeriodValidatorTest.xml
└── TEST-com.example.workshop.exception.GlobalExceptionHandlerTest.xml
```

### Console Output
Tests will show output like:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.workshop.controller.CurrencyControllerWebTest
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Running com.example.workshop.validation.PeriodValidatorTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 50, Failures: 0, Errors: 0, Skipped: 0
```

## Troubleshooting

### Issue: Tests not found
**Solution**: Ensure you're in the project root directory
```bash
cd C:\Z_LEARNING\copilot\copilot-workshop-01-01
mvn clean test
```

### Issue: Compilation errors
**Solution**: Clean and rebuild
```bash
mvn clean compile
mvn test
```

### Issue: IDE shows errors but Maven compiles fine
**Solution**: This is an IDE indexing issue
- IntelliJ: File → Invalidate Caches → Restart
- Eclipse: Project → Clean → Clean all projects
- VS Code: Reload window (Ctrl+Shift+P → "Reload Window")

### Issue: Port 8080 already in use (for integration tests)
**Solution**: Stop any running application on port 8080
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

## Test Coverage Goals

### Phase 1 Coverage Targets
- **Overall Coverage**: > 80%
- **Service Layer**: > 90%
- **Controller Layer**: > 85%
- **Validation**: 100%
- **Exception Handling**: 100%

### Generate Coverage Report
```bash
mvn clean test jacoco:report
```

Open: `target/site/jacoco/index.html` in a browser

## Next Steps

After all tests pass:
1. ✅ Review test coverage report
2. ✅ Check for any warnings in console output
3. ✅ Verify Swagger documentation at http://localhost:8080/swagger-ui.html
4. ✅ Proceed to Phase 2: Docker and Docker Compose

## Additional Testing

### Manual API Testing

#### Start the Application
```bash
mvn spring-boot:run
```

#### Test with cURL

**Get all currencies:**
```bash
curl http://localhost:8080/api/v1/currencies
```

**Add currency:**
```bash
curl -X POST "http://localhost:8080/api/v1/currencies?currency=PLN"
```

**Get exchange rate:**
```bash
curl "http://localhost:8080/api/v1/currencies/exchange-rates?amount=100&from=USD&to=EUR"
```

**Get trend:**
```bash
curl "http://localhost:8080/api/v1/currencies/trends?from=USD&to=EUR&period=10D"
```

#### Test with Swagger UI
1. Navigate to http://localhost:8080/swagger-ui.html
2. Click "Try it out" on any endpoint
3. Fill in parameters
4. Click "Execute"
5. View response

## Support

For issues or questions:
1. Check the PHASE_1_COMPLETION_SUMMARY.md
2. Review the IMPLEMENTATION_PLAN.md
3. Check test output for detailed error messages

---

**Happy Testing! 🧪**

