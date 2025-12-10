# Currency Exchange Rates Provider Service - Implementation Plan

## Project Overview
A Spring Boot application that provides up-to-date currency exchange rates from multiple sources, with scheduled updates, role-based access control, and comprehensive testing.

---

## Phase 1: REST Layer with Stubs
**Goal:** Complete API structure with stubbed responses, validation, error handling, and Swagger documentation

### 1.1 Project Setup
- [x] Spring Boot 3.x with Java 21
- [x] Configure Maven dependencies:
  - Spring Boot Starter Web
  - Spring Boot Starter Validation
  - Lombok
  - Springdoc OpenAPI (Swagger)
  - Spring Boot Starter Test

### 1.2 Domain Models
- [x] Create `Currency` entity (id, code, name)
- [x] Create `ExchangeRate` entity (id, baseCurrency, targetCurrency, rate, timestamp)
- [x] Create DTOs:
  - `CurrencyDTO`
  - `ExchangeRateRequestDTO` (amount, from, to)
  - `ExchangeRateResponseDTO` (amount, from, to, result, rate, timestamp)
  - `TrendRequestDTO` (from, to, period)
  - `TrendResponseDTO` (from, to, period, changePercentage)
  - `ErrorResponseDTO` (timestamp, status, error, message, path)

### 1.3 Controllers with Stub Implementation
- [x] `CurrencyController`:
  - `GET /api/v1/currencies` - Return list of currencies
  - `POST /api/v1/currencies?currency={code}` - Add to in-memory set
  - `POST /api/v1/currencies/refresh` - Return success message
  - `GET /api/v1/currencies/exchange-rates` - Return stubbed rates
  - `GET /api/v1/currencies/trends` - Return stubbed trend data

### 1.4 Validation
- [x] Add validation annotations:
  - `@NotBlank` for currency codes
  - `@Positive` for amounts
  - `@Pattern` for currency format (3 letters)
  - Custom validator for period format (12H, 10D, 3M, 1Y)
- [x] Create custom `@ValidPeriod` annotation
- [x] Implement `PeriodValidator` class

### 1.5 Exception Handling
- [x] Create custom exceptions:
  - `CurrencyNotFoundException`
  - `InvalidCurrencyCodeException`
  - `InvalidPeriodException`
  - `ExchangeRateNotFoundException`
- [x] Implement `@RestControllerAdvice` with `GlobalExceptionHandler`:
  - Handle validation errors (`MethodArgumentNotValidException`)
  - Handle custom business exceptions
  - Handle generic exceptions
  - Return proper HTTP status codes and error DTOs

### 1.6 API Documentation
- [x] Configure Springdoc OpenAPI
- [x] Add `@Operation`, `@ApiResponse` annotations
- [x] Add `@Schema` descriptions for DTOs
- [x] Configure Swagger UI at `/swagger-ui.html`
- [x] Add API info (title, version, description)

### 1.7 Testing
- [x] Controller tests with `@WebMvcTest`:
  - Test all endpoints with valid data
  - Test validation errors
  - Test exception handling
  - Verify response structure
- [x] Validation tests for custom validators
- [x] Exception handler tests

**Deliverables:** Working REST API with stubbed data, full validation, error handling, and Swagger documentation

---

## Phase 2: Docker and Docker Compose
**Goal:** Containerization with PostgreSQL database and 2 mock exchange rate services

### 2.1 Docker Configuration
- [ ] Create `Dockerfile` for main application:
  - Multi-stage build (Maven + JRE)
  - Use Java 21 base image
  - Expose port 8080
  - Configure healthcheck

### 2.2 Mock Exchange Rate Services
- [ ] Create Mock Service 1 (`mock-exchange-service-1`):
  - Simple Spring Boot app on port 8081
  - Endpoint: `GET /rates?base={currency}`
  - Return random rates for EUR, USD, GBP, JPY, etc.
  - Create separate `Dockerfile`

- [ ] Create Mock Service 2 (`mock-exchange-service-2`):
  - Simple Spring Boot app on port 8082
  - Endpoint: `GET /api/rates?from={currency}`
  - Different response format
  - Return random rates
  - Create separate `Dockerfile`

### 2.3 Docker Compose
- [ ] Create `docker-compose.yml`:
  - PostgreSQL service (port 5432)
    - Environment variables (POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD)
    - Volume for data persistence
  - Main application service
    - Depends on PostgreSQL
    - Environment variables for DB connection
    - Port mapping 8080:8080
  - Mock service 1 (port 8081)
  - Mock service 2 (port 8082)
  - Network configuration

### 2.4 Application Configuration
- [ ] Update `application.properties` / `application.yml`:
  - Database connection properties (with environment variables)
  - Logging configuration
  - Mock services URLs
  - Profile-specific configurations (dev, test, prod)

### 2.5 Testing
- [ ] Test Docker build for each service
- [ ] Test docker-compose startup
- [ ] Verify service connectivity
- [ ] Test database connection from main app

**Deliverables:** Fully containerized application with PostgreSQL and mock services

---

## Phase 3: Database Integration
**Goal:** Spring Data JPA, Liquibase migrations, and persistence layer

### 3.1 Dependencies
- [ ] Add Maven dependencies:
  - Spring Boot Starter Data JPA
  - PostgreSQL Driver
  - Liquibase Core

### 3.2 Entity Models
- [ ] Enhance `Currency` entity:
  - `@Entity`, `@Table` annotations
  - `@Id`, `@GeneratedValue` for id
  - Unique constraint on code
  - Timestamps (createdAt, updatedAt)

- [ ] Enhance `ExchangeRate` entity:
  - JPA annotations
  - `@ManyToOne` relationships to Currency
  - Composite index on (baseCurrency, targetCurrency, timestamp)
  - Validation annotations

- [ ] Create `User` entity:
  - id, username, password, email
  - `@ManyToMany` relationship with Role

- [ ] Create `Role` entity:
  - id, name (USER, PREMIUM_USER, ADMIN)
  - `@ManyToMany` relationship with User

### 3.3 Liquibase Migrations
- [ ] Configure Liquibase in application properties
- [ ] Create `db.changelog-master.xml` (or YAML)
- [ ] Migration 1: Create `currency` table
- [ ] Migration 2: Create `exchange_rate` table
- [ ] Migration 3: Create `user` table
- [ ] Migration 4: Create `role` table
- [ ] Migration 5: Create `user_roles` join table
- [ ] Migration 6: Insert default roles (USER, PREMIUM_USER, ADMIN)
- [ ] Add indexes for performance

### 3.4 Repository Layer
- [ ] Create `CurrencyRepository extends JpaRepository`:
  - `Optional<Currency> findByCode(String code)`
  - `boolean existsByCode(String code)`

- [ ] Create `ExchangeRateRepository extends JpaRepository`:
  - `List<ExchangeRate> findByBaseCurrencyAndTargetCurrency(Currency base, Currency target)`
  - `Optional<ExchangeRate> findTopByBaseCurrencyAndTargetCurrencyOrderByTimestampDesc()`
  - `List<ExchangeRate> findByBaseCurrencyAndTargetCurrencyAndTimestampAfter()`
  - Custom query methods with `@Query`

- [ ] Create `UserRepository extends JpaRepository`:
  - `Optional<User> findByUsername(String username)`

- [ ] Create `RoleRepository extends JpaRepository`:
  - `Optional<Role> findByName(String name)`

### 3.5 Service Layer
- [ ] Create `CurrencyService`:
  - `List<Currency> getAllCurrencies()` (use Optional and Stream API)
  - `Currency addCurrency(String code)` (check duplicates)
  - `Optional<Currency> findByCode(String code)`

- [ ] Create `ExchangeRateService`:
  - `ExchangeRateResponseDTO getExchangeRate(ExchangeRateRequestDTO request)`
  - `void saveExchangeRate(ExchangeRate rate)`
  - `List<ExchangeRate> getHistoricalRates(String from, String to, LocalDateTime since)`
  - Use Stream API for filtering and transformations

### 3.6 Update Controllers
- [ ] Replace stub implementations with actual service calls
- [ ] Use Optional for null-safe operations
- [ ] Implement proper error handling for database operations

### 3.7 Testing
- [ ] Repository tests with `@DataJpaTest`:
  - Test CRUD operations
  - Test custom query methods
  - Test relationships

- [ ] Service unit tests with Mockito:
  - Mock repositories
  - Test business logic
  - Test Optional usage
  - Test Stream API operations

- [ ] Integration tests with TestContainers:
  - Test with real PostgreSQL container
  - Test Liquibase migrations
  - Test end-to-end flows

**Deliverables:** Fully functional persistence layer with JPA, Liquibase, and comprehensive tests

---

## Phase 4: Security Implementation
**Goal:** Spring Security with authentication, authorization, and role-based access control

### 4.1 Dependencies
- [ ] Add Spring Boot Starter Security
- [ ] Add Spring Security Test

### 4.2 Security Configuration
- [ ] Create `SecurityConfig` class:
  - Configure `SecurityFilterChain`
  - Password encoder (BCrypt)
  - Authentication provider
  - Configure endpoint security:
    - `/api/v1/currencies` (GET) - permitAll()
    - `/api/v1/currencies` (POST) - hasRole('ADMIN')
    - `/api/v1/currencies/exchange-rates` - permitAll()
    - `/api/v1/currencies/refresh` - hasRole('ADMIN')
    - `/api/v1/currencies/trends` - hasAnyRole('ADMIN', 'PREMIUM_USER')
  - Login page configuration
  - CSRF configuration
  - Session management

### 4.3 User Details Service
- [ ] Create `CustomUserDetailsService implements UserDetailsService`:
  - Load user by username from database
  - Map User entity to UserDetails
  - Include roles/authorities

### 4.4 Password Encryption
- [ ] Create data initialization component
- [ ] Create default users with encrypted passwords:
  - admin / admin123 (ADMIN role)
  - premium / premium123 (PREMIUM_USER role)
  - user / user123 (USER role)

### 4.5 Login Page
- [ ] Create custom login page (Thymeleaf or static HTML)
- [ ] Add logout functionality
- [ ] Add error handling for failed login

### 4.6 Method Security
- [ ] Enable `@EnableMethodSecurity`
- [ ] Add `@PreAuthorize` annotations to service methods if needed

### 4.7 Testing
- [ ] Security tests with `@WithMockUser`:
  - Test authenticated access
  - Test authorization (different roles)
  - Test unauthenticated access
  - Test forbidden access

- [ ] Integration tests with Spring Security Test:
  - Test login flow
  - Test role-based access
  - Test session management

**Deliverables:** Secure application with authentication, authorization, and role-based access control

---

## Phase 5: External Provider Integration
**Goal:** HTTP clients for real exchange rate providers with error handling

### 5.1 Dependencies
- [ ] Add Spring Boot Starter WebClient (or RestTemplate)
- [ ] Add Resilience4j (for circuit breaker, retry)

### 5.2 Provider Interfaces
- [ ] Create `ExchangeRateProvider` interface:
  - `Map<String, BigDecimal> getExchangeRates(String baseCurrency)`
  - `String getProviderName()`

### 5.3 Provider Implementations
- [ ] Implement `FixerIoProvider`:
  - HTTP client to fixer.io API
  - Parse JSON response
  - Handle API errors
  - Use Optional for nullable values

- [ ] Implement `ExchangeRatesApiProvider`:
  - HTTP client to exchangeratesapi.io
  - Parse response
  - Error handling

- [ ] Implement `MockProvider1Client`:
  - Call local mock service 1
  - Parse response

- [ ] Implement `MockProvider2Client`:
  - Call local mock service 2
  - Parse response

### 5.4 Provider Aggregator
- [ ] Create `ExchangeRateProviderAggregator`:
  - Use Stream API to call all providers
  - Aggregate results (average, median, or fallback strategy)
  - Handle partial failures
  - Log provider responses

### 5.5 Configuration
- [ ] Externalize API keys and URLs in application properties
- [ ] Create provider-specific configuration classes
- [ ] Configure timeouts and connection pools

### 5.6 Error Handling
- [ ] Create provider-specific exceptions
- [ ] Implement fallback strategies
- [ ] Add circuit breaker with Resilience4j
- [ ] Add retry logic

### 5.7 Testing
- [ ] Unit tests with WireMock:
  - Mock external API responses
  - Test success scenarios
  - Test error scenarios (timeout, 4xx, 5xx)
  - Test fallback logic

- [ ] Integration tests:
  - Test with real mock services
  - Test provider aggregation
  - Test circuit breaker behavior

**Deliverables:** Working integration with multiple exchange rate providers and robust error handling

---

## Phase 6: Scheduled Jobs
**Goal:** Automatic hourly rate updates and startup initialization

### 6.1 Enable Scheduling
- [ ] Add `@EnableScheduling` to main application class

### 6.2 Scheduled Tasks
- [ ] Create `ExchangeRateScheduler`:
  - `@Scheduled(cron = "0 0 * * * *")` - Every hour
  - Fetch rates for all currencies in the system
  - Save to database using Stream API
  - Log execution time and results
  - Handle errors gracefully

### 6.3 Startup Initialization
- [ ] Create `@EventListener(ApplicationReadyEvent.class)`:
  - Load initial exchange rates on startup
  - Initialize default currencies if needed
  - Verify database connectivity

### 6.4 Async Processing
- [ ] Enable `@EnableAsync`
- [ ] Make rate fetching async if needed
- [ ] Configure thread pool

### 6.5 Testing
- [ ] Test scheduled execution
- [ ] Test startup initialization
- [ ] Test async processing
- [ ] Test error handling in scheduled tasks

**Deliverables:** Automatic background updates of exchange rates

---

## Phase 7: Advanced Features
**Goal:** Caching, performance optimizations, and monitoring

### 7.1 Caching
- [ ] Add Spring Boot Starter Cache
- [ ] Add Redis or Caffeine dependency
- [ ] Enable `@EnableCaching`
- [ ] Add `@Cacheable` to frequently accessed methods:
  - Currency list
  - Latest exchange rates
- [ ] Configure cache TTL
- [ ] Implement cache eviction on refresh

### 7.2 Performance Optimizations
- [ ] Add database connection pooling (HikariCP)
- [ ] Optimize JPA queries (fetch strategies, projections)
- [ ] Add pagination for large result sets
- [ ] Use database indexes

### 7.3 Monitoring & Observability
- [ ] Add Spring Boot Actuator
- [ ] Configure health endpoints
- [ ] Add custom health indicators:
  - Database health
  - External provider health
- [ ] Add metrics:
  - Request counts
  - Response times
  - Cache hit rates
  - Exchange rate fetch success/failure
- [ ] Configure Prometheus metrics (optional)

### 7.4 Logging
- [ ] Configure Logback/Log4j2
- [ ] Add structured logging (JSON format)
- [ ] Log levels per package
- [ ] Add correlation IDs for request tracking

### 7.5 Testing
- [ ] Test cache behavior
- [ ] Test health endpoints
- [ ] Performance tests
- [ ] Load tests (optional)

**Deliverables:** Optimized, production-ready application with monitoring

---

## Phase 8: Code Quality
**Goal:** CheckStyle, PMD, JaCoCo coverage, comprehensive testing

### 8.1 Static Analysis Configuration
- [ ] Add CheckStyle plugin to pom.xml:
  - Configure rules (Google or Sun style)
  - Customize checkstyle.xml
  - Run on compile phase

- [ ] Add PMD plugin:
  - Configure rulesets
  - Custom rules for project
  - Run on verify phase

- [ ] Add SpotBugs (optional):
  - Configure bug patterns
  - Exclude false positives

### 8.2 Code Coverage
- [ ] Add JaCoCo plugin:
  - Configure coverage goals (e.g., 80%)
  - Generate reports
  - Fail build on low coverage

- [ ] Add PiTest plugin (mutation testing):
  - Configure mutation coverage
  - Target critical classes
  - Generate mutation reports

### 8.3 Comprehensive Testing
- [ ] Unit tests (target 100% for business logic):
  - All service methods
  - Validators
  - Utility classes
  - Use Mockito for mocking

- [ ] Integration tests:
  - TestContainers for PostgreSQL
  - WireMock for external APIs
  - End-to-end API tests
  - Security integration tests

- [ ] Controller tests:
  - `@WebMvcTest` for validation
  - Test all endpoints
  - Test error scenarios

- [ ] Repository tests:
  - `@DataJpaTest`
  - Test custom queries
  - Test relationships

### 8.4 Test Organization
- [ ] Organize tests by type (unit, integration)
- [ ] Use test base classes for common setup
- [ ] Use test fixtures and builders
- [ ] Use AssertJ for fluent assertions

### 8.5 CI/CD Preparation
- [ ] Create Maven profiles (dev, test, prod)
- [ ] Configure Maven Failsafe for integration tests
- [ ] Create shell scripts for build and test
- [ ] Document test execution

### 8.6 Code Quality Goals
- [ ] 0 CheckStyle violations
- [ ] 0 PMD violations (P1-P3)
- [ ] 80%+ JaCoCo coverage
- [ ] 70%+ PiTest mutation coverage
- [ ] All tests passing

**Deliverables:** High-quality, well-tested codebase with automated quality checks

---

## Phase 9: Production Readiness
**Goal:** Actuator, health checks, security hardening, and deployment

### 9.1 Actuator Configuration
- [ ] Enable required actuator endpoints:
  - `/actuator/health`
  - `/actuator/info`
  - `/actuator/metrics`
  - `/actuator/prometheus` (optional)
- [ ] Secure actuator endpoints (ADMIN only)
- [ ] Configure detailed health information

### 9.2 Health Checks
- [ ] Database health indicator
- [ ] Custom health indicators:
  - External provider availability
  - Recent exchange rate fetch status
- [ ] Readiness and liveness probes for Kubernetes

### 9.3 Security Hardening
- [ ] HTTPS configuration
- [ ] Security headers (HSTS, CSP, X-Frame-Options)
- [ ] Rate limiting (optional)
- [ ] Input sanitization
- [ ] SQL injection prevention (parameterized queries)
- [ ] CORS configuration

### 9.4 Environment Configuration
- [ ] Environment-specific properties:
  - application-dev.yml
  - application-test.yml
  - application-prod.yml
- [ ] Externalize secrets (use environment variables)
- [ ] Configure profiles

### 9.5 Documentation
- [ ] Update README.md:
  - Project description
  - Setup instructions
  - Running with Docker
  - API documentation link
  - Testing instructions
  - Architecture overview

- [ ] Create DEPLOYMENT.md:
  - Deployment steps
  - Environment variables
  - Database setup
  - Health check endpoints

- [ ] API documentation:
  - Swagger UI
  - Postman collection (optional)

### 9.6 Final Testing
- [ ] End-to-end smoke tests
- [ ] Security audit
- [ ] Performance testing
- [ ] Deployment verification

**Deliverables:** Production-ready, secure, documented application

---

## Technical Stack Summary

### Core Technologies
- Java 21
- Spring Boot 3.x
- Maven
- PostgreSQL
- Liquibase
- Spring Data JPA
- Lombok

### Security
- Spring Security
- BCrypt password encryption
- Role-based access control

### API & Documentation
- Spring Web
- Swagger/OpenAPI (Springdoc)
- Bean Validation

### Testing
- JUnit 5
- Mockito
- Spring Test Framework
- TestContainers
- WireMock
- AssertJ

### Code Quality
- JaCoCo
- CheckStyle
- PMD
- SpotBugs (optional)
- PiTest

### Monitoring & Operations
- Spring Boot Actuator
- Micrometer
- Logging (Logback/SLF4J)

### Containerization
- Docker
- Docker Compose

### HTTP Clients
- Spring WebClient / RestTemplate
- Resilience4j

### Caching (Phase 7)
- Spring Cache
- Redis or Caffeine

---

## Best Practices to Follow

1. **Use Lombok** for reducing boilerplate (`@Data`, `@Builder`, `@Slf4j`)
2. **Use Optional** for null-safe operations
3. **Use Stream API** for collection processing
4. **Implement proper exception hierarchy**
5. **Use DTOs for API layer** (don't expose entities)
6. **Follow REST conventions** (proper HTTP methods and status codes)
7. **Write tests first** (TDD approach when possible)
8. **Keep controllers thin** (business logic in services)
9. **Use constructor injection** (not field injection)
10. **Follow SOLID principles**
11. **Document all public APIs** (Swagger annotations)
12. **Use meaningful commit messages**
13. **Keep methods small and focused**
14. **Use constants for magic values**
15. **Handle exceptions appropriately at each layer**

---

## Estimated Timeline

- **Phase 1:** 2-3 days
- **Phase 2:** 1-2 days
- **Phase 3:** 2-3 days
- **Phase 4:** 2-3 days
- **Phase 5:** 2-3 days
- **Phase 6:** 1 day
- **Phase 7:** 1-2 days
- **Phase 8:** 2-3 days
- **Phase 9:** 1-2 days

**Total:** 14-22 days (depending on experience level)

---

## Success Criteria

- ✅ All API endpoints working as specified
- ✅ All tests passing (unit, integration, functional)
- ✅ Code coverage ≥ 80%
- ✅ Zero critical security vulnerabilities
- ✅ All services running in Docker
- ✅ Swagger documentation accessible
- ✅ Scheduled jobs working correctly
- ✅ Security properly configured with roles
- ✅ Database migrations working
- ✅ External provider integration functional
- ✅ Static analysis passing (CheckStyle, PMD)
- ✅ Application runs in production mode

---

## Next Steps

1. Start with Phase 1: Create the REST API structure with stubbed responses
2. Set up the project in your IDE
3. Create a Git repository and commit regularly
4. Follow the checklist for each phase
5. Test thoroughly after each phase before moving to the next
6. Document any deviations or additional features

Good luck with your implementation! 🚀

