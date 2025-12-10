# GitHub Issues for Currency Exchange Rates Provider Service

## Labels to Create First
- `phase-1` through `phase-9`
- `backend`
- `testing`
- `documentation`
- `security`
- `infrastructure`
- `quality`

---

## Issue #1: Phase 1 - REST Layer with Stubs

**Title:** Phase 1: REST Layer with Stubs

**Labels:** `phase-1`, `backend`, `testing`, `documentation`

**Milestone:** Phase 1 - Foundation

**Description:**

### Goal
Complete API structure with stubbed responses, validation, error handling, and Swagger documentation.

### Tasks
- [x] Project Setup
  - [x] Spring Boot 3.x with Java 21
  - [x] Configure Maven dependencies
- [x] Domain Models
  - [x] Create `Currency` entity
  - [x] Create `ExchangeRate` entity
  - [x] Create all DTOs
- [x] Controllers with Stub Implementation
  - [x] `CurrencyController` with all endpoints
- [x] Validation
  - [x] Add validation annotations
  - [x] Create custom `@ValidPeriod` annotation
  - [x] Implement `PeriodValidator` class
- [x] Exception Handling
  - [x] Create custom exceptions
  - [x] Implement `GlobalExceptionHandler`
- [x] API Documentation
  - [x] Configure Springdoc OpenAPI
  - [x] Add Swagger annotations
- [x] Testing
  - [x] Controller tests with `@WebMvcTest`
  - [x] Validation tests
  - [x] Exception handler tests

### Acceptance Criteria
- [ ] All endpoints return proper stubbed responses
- [ ] Validation works correctly for all inputs
- [ ] Error handling returns appropriate HTTP status codes
- [ ] Swagger UI accessible at `/swagger-ui.html`
- [ ] Test coverage ≥ 80% for completed components
- [ ] All tests passing

### Technical Stack
- Spring Boot 3.x, Java 21, Maven
- Spring Web, Validation, Lombok
- Springdoc OpenAPI
- JUnit 5, MockMvc

**Estimated Time:** 2-3 days

---

## Issue #2: Phase 2 - Docker and Docker Compose

**Title:** Phase 2: Containerization with Docker and Mock Services

**Labels:** `phase-2`, `infrastructure`, `backend`

**Milestone:** Phase 2 - Infrastructure

**Dependencies:** Blocked by #1

**Description:**

### Goal
Containerization with PostgreSQL database and 2 mock exchange rate services.

### Tasks
- [ ] Docker Configuration
  - [ ] Create `Dockerfile` for main application
  - [ ] Multi-stage build with Maven + JRE
  - [ ] Configure healthcheck
- [ ] Mock Exchange Rate Services
  - [ ] Create Mock Service 1 (port 8081)
  - [ ] Create Mock Service 2 (port 8082)
  - [ ] Create separate Dockerfiles for each
- [ ] Docker Compose
  - [ ] Create `docker-compose.yml`
  - [ ] Configure PostgreSQL service
  - [ ] Configure main application service
  - [ ] Configure mock services
  - [ ] Set up network and volumes
- [ ] Application Configuration
  - [ ] Update application properties with environment variables
  - [ ] Configure profile-specific settings
- [ ] Testing
  - [ ] Test Docker builds
  - [ ] Test docker-compose startup
  - [ ] Verify service connectivity

### Acceptance Criteria
- [ ] All services build successfully
- [ ] `docker-compose up` starts all services
- [ ] PostgreSQL accessible from main app
- [ ] Mock services return test data
- [ ] Health checks working
- [ ] Documentation updated with Docker instructions

### Deliverables
- `Dockerfile` for main app
- `mock-service-1/Dockerfile`
- `mock-service-2/Dockerfile`
- `docker-compose.yml`
- Updated README with Docker instructions

**Estimated Time:** 1-2 days

---

## Issue #3: Phase 3 - Database Integration

**Title:** Phase 3: Database Integration with JPA and Liquibase

**Labels:** `phase-3`, `backend`, `testing`

**Milestone:** Phase 3 - Persistence

**Dependencies:** Blocked by #2

**Description:**

### Goal
Spring Data JPA, Liquibase migrations, and complete persistence layer.

### Tasks
- [ ] Dependencies
  - [ ] Add Spring Data JPA, PostgreSQL Driver, Liquibase
- [ ] Entity Models
  - [ ] Enhance `Currency` entity with JPA annotations
  - [ ] Enhance `ExchangeRate` entity
  - [ ] Create `User` entity
  - [ ] Create `Role` entity
- [ ] Liquibase Migrations
  - [ ] Create `db.changelog-master.xml`
  - [ ] Migration for each table
  - [ ] Insert default roles
  - [ ] Add indexes
- [ ] Repository Layer
  - [ ] `CurrencyRepository`
  - [ ] `ExchangeRateRepository` with custom queries
  - [ ] `UserRepository`
  - [ ] `RoleRepository`
- [ ] Service Layer
  - [ ] `CurrencyService` with Optional and Stream API
  - [ ] `ExchangeRateService`
- [ ] Update Controllers
  - [ ] Replace stubs with actual service calls
- [ ] Testing
  - [ ] Repository tests with `@DataJpaTest`
  - [ ] Service unit tests with Mockito
  - [ ] Integration tests with TestContainers

### Acceptance Criteria
- [ ] All entities properly mapped
- [ ] Liquibase migrations run successfully
- [ ] Repositories work with real database
- [ ] Services implement business logic correctly
- [ ] Controllers use real data
- [ ] All tests passing
- [ ] Test coverage ≥ 80%

### Technical Stack
- Spring Data JPA, Liquibase, PostgreSQL
- TestContainers for integration tests
- Mockito for unit tests

**Estimated Time:** 2-3 days

---

## Issue #4: Phase 4 - Security Implementation

**Title:** Phase 4: Spring Security with Role-Based Access Control

**Labels:** `phase-4`, `security`, `backend`, `testing`

**Milestone:** Phase 4 - Security

**Dependencies:** Blocked by #3

**Description:**

### Goal
Spring Security with authentication, authorization, and role-based access control.

### Tasks
- [ ] Dependencies
  - [ ] Add Spring Boot Starter Security
  - [ ] Add Spring Security Test
- [ ] Security Configuration
  - [ ] Create `SecurityConfig` class
  - [ ] Configure `SecurityFilterChain`
  - [ ] Configure endpoint security with roles
  - [ ] Password encoder (BCrypt)
  - [ ] Session management
- [ ] User Details Service
  - [ ] Create `CustomUserDetailsService`
  - [ ] Load user from database
  - [ ] Map roles to authorities
- [ ] Password Encryption
  - [ ] Create default users (admin, premium, user)
  - [ ] Encrypt passwords with BCrypt
- [ ] Login Page
  - [ ] Create custom login page
  - [ ] Add logout functionality
  - [ ] Error handling
- [ ] Method Security
  - [ ] Enable `@EnableMethodSecurity`
  - [ ] Add `@PreAuthorize` where needed
- [ ] Testing
  - [ ] Security tests with `@WithMockUser`
  - [ ] Test authentication and authorization
  - [ ] Test role-based access
  - [ ] Integration tests

### Acceptance Criteria
- [ ] Login page functional
- [ ] Authentication working
- [ ] Role-based access control working:
  - `/api/v1/currencies` (GET) - public
  - `/api/v1/currencies` (POST) - ADMIN only
  - `/api/v1/currencies/refresh` - ADMIN only
  - `/api/v1/currencies/trends` - ADMIN and PREMIUM_USER
- [ ] Default users created
- [ ] Security tests passing
- [ ] Swagger UI accessible

**Estimated Time:** 2-3 days

---

## Issue #5: Phase 5 - External Provider Integration

**Title:** Phase 5: Integration with External Exchange Rate Providers

**Labels:** `phase-5`, `backend`, `testing`

**Milestone:** Phase 5 - Integration

**Dependencies:** Blocked by #4

**Description:**

### Goal
HTTP clients for real exchange rate providers with error handling and resilience.

### Tasks
- [ ] Dependencies
  - [ ] Add Spring Boot Starter WebClient
  - [ ] Add Resilience4j
- [ ] Provider Interfaces
  - [ ] Create `ExchangeRateProvider` interface
- [ ] Provider Implementations
  - [ ] Implement `FixerIoProvider`
  - [ ] Implement `ExchangeRatesApiProvider`
  - [ ] Implement `MockProvider1Client`
  - [ ] Implement `MockProvider2Client`
- [ ] Provider Aggregator
  - [ ] Create `ExchangeRateProviderAggregator`
  - [ ] Implement aggregation strategy
  - [ ] Handle partial failures with Stream API
- [ ] Configuration
  - [ ] Externalize API keys and URLs
  - [ ] Configure timeouts
- [ ] Error Handling
  - [ ] Create provider-specific exceptions
  - [ ] Implement fallback strategies
  - [ ] Add circuit breaker
  - [ ] Add retry logic
- [ ] Testing
  - [ ] Unit tests with WireMock
  - [ ] Test error scenarios
  - [ ] Test fallback logic
  - [ ] Integration tests with mock services

### Acceptance Criteria
- [ ] All providers successfully fetch rates
- [ ] Aggregator combines results correctly
- [ ] Circuit breaker triggers on failures
- [ ] Retry logic works
- [ ] Fallback strategies functional
- [ ] All tests passing
- [ ] Error handling comprehensive

### Technical Stack
- Spring WebClient, Resilience4j
- WireMock for testing

**Estimated Time:** 2-3 days

---

## Issue #6: Phase 6 - Scheduled Jobs

**Title:** Phase 6: Scheduled Background Jobs for Rate Updates

**Labels:** `phase-6`, `backend`, `testing`

**Milestone:** Phase 6 - Automation

**Dependencies:** Blocked by #5

**Description:**

### Goal
Automatic hourly rate updates and startup initialization.

### Tasks
- [ ] Enable Scheduling
  - [ ] Add `@EnableScheduling`
- [ ] Scheduled Tasks
  - [ ] Create `ExchangeRateScheduler`
  - [ ] Implement hourly rate fetch (cron: `0 0 * * * *`)
  - [ ] Save rates using Stream API
  - [ ] Add logging
  - [ ] Error handling
- [ ] Startup Initialization
  - [ ] Create `@EventListener(ApplicationReadyEvent.class)`
  - [ ] Load initial rates on startup
  - [ ] Initialize default currencies
- [ ] Async Processing
  - [ ] Enable `@EnableAsync`
  - [ ] Configure thread pool
  - [ ] Make rate fetching async if needed
- [ ] Testing
  - [ ] Test scheduled execution
  - [ ] Test startup initialization
  - [ ] Test async processing
  - [ ] Test error handling

### Acceptance Criteria
- [ ] Rates update automatically every hour
- [ ] Initial rates loaded on startup
- [ ] Scheduler handles errors gracefully
- [ ] Logging shows execution details
- [ ] Tests verify scheduling behavior
- [ ] No blocking of main application

**Estimated Time:** 1 day

---

## Issue #7: Phase 7 - Advanced Features

**Title:** Phase 7: Caching, Performance Optimization, and Monitoring

**Labels:** `phase-7`, `backend`, `testing`

**Milestone:** Phase 7 - Optimization

**Dependencies:** Blocked by #6

**Description:**

### Goal
Caching, performance optimizations, and monitoring setup.

### Tasks
- [ ] Caching
  - [ ] Add Spring Boot Starter Cache
  - [ ] Add Redis or Caffeine
  - [ ] Enable `@EnableCaching`
  - [ ] Add `@Cacheable` to frequently accessed methods
  - [ ] Configure cache TTL
  - [ ] Implement cache eviction
- [ ] Performance Optimizations
  - [ ] Configure HikariCP
  - [ ] Optimize JPA queries
  - [ ] Add pagination
  - [ ] Verify database indexes
- [ ] Monitoring & Observability
  - [ ] Add Spring Boot Actuator
  - [ ] Configure health endpoints
  - [ ] Add custom health indicators
  - [ ] Add metrics (requests, response times, cache hits)
  - [ ] Configure Prometheus (optional)
- [ ] Logging
  - [ ] Configure structured logging
  - [ ] Set log levels per package
  - [ ] Add correlation IDs
- [ ] Testing
  - [ ] Test cache behavior
  - [ ] Test health endpoints
  - [ ] Performance tests

### Acceptance Criteria
- [ ] Caching reduces database queries
- [ ] Health endpoints accessible
- [ ] Custom health indicators working
- [ ] Metrics collected
- [ ] Performance improved vs Phase 6
- [ ] All tests passing

**Estimated Time:** 1-2 days

---

## Issue #8: Phase 8 - Code Quality

**Title:** Phase 8: Code Quality Tools and Comprehensive Testing

**Labels:** `phase-8`, `quality`, `testing`

**Milestone:** Phase 8 - Quality Assurance

**Dependencies:** Blocked by #7

**Description:**

### Goal
CheckStyle, PMD, JaCoCo coverage, and comprehensive testing.

### Tasks
- [ ] Static Analysis Configuration
  - [ ] Add CheckStyle plugin
  - [ ] Configure checkstyle.xml
  - [ ] Add PMD plugin
  - [ ] Configure rulesets
  - [ ] Add SpotBugs (optional)
- [ ] Code Coverage
  - [ ] Add JaCoCo plugin
  - [ ] Configure 80% coverage goal
  - [ ] Add PiTest plugin (mutation testing)
  - [ ] Generate reports
- [ ] Comprehensive Testing
  - [ ] Unit tests (target 100% for business logic)
  - [ ] Integration tests
  - [ ] Controller tests
  - [ ] Repository tests
- [ ] Test Organization
  - [ ] Organize by type
  - [ ] Create test base classes
  - [ ] Use test fixtures and builders
  - [ ] Use AssertJ assertions
- [ ] CI/CD Preparation
  - [ ] Create Maven profiles
  - [ ] Configure Failsafe plugin
  - [ ] Create build scripts
  - [ ] Document test execution
- [ ] Code Quality Goals
  - [ ] Achieve 0 CheckStyle violations
  - [ ] Achieve 0 PMD violations (P1-P3)
  - [ ] Achieve 80%+ JaCoCo coverage
  - [ ] Achieve 70%+ PiTest mutation coverage

### Acceptance Criteria
- [ ] All static analysis tools configured
- [ ] No critical violations
- [ ] Code coverage ≥ 80%
- [ ] Mutation coverage ≥ 70%
- [ ] All tests passing
- [ ] Build succeeds with all checks
- [ ] Reports generated

**Estimated Time:** 2-3 days

---

## Issue #9: Phase 9 - Production Readiness

**Title:** Phase 9: Production Readiness and Deployment

**Labels:** `phase-9`, `infrastructure`, `documentation`, `security`

**Milestone:** Phase 9 - Production

**Dependencies:** Blocked by #8

**Description:**

### Goal
Actuator, health checks, security hardening, and deployment documentation.

### Tasks
- [ ] Actuator Configuration
  - [ ] Enable required endpoints
  - [ ] Secure actuator endpoints (ADMIN only)
  - [ ] Configure detailed health info
- [ ] Health Checks
  - [ ] Database health indicator
  - [ ] Custom health indicators
  - [ ] Readiness and liveness probes
- [ ] Security Hardening
  - [ ] HTTPS configuration
  - [ ] Security headers (HSTS, CSP, X-Frame-Options)
  - [ ] Rate limiting (optional)
  - [ ] Input sanitization
  - [ ] CORS configuration
- [ ] Environment Configuration
  - [ ] Create environment-specific properties
  - [ ] Externalize secrets
  - [ ] Configure profiles
- [ ] Documentation
  - [ ] Update README.md
  - [ ] Create DEPLOYMENT.md
  - [ ] Update API documentation
  - [ ] Create Postman collection (optional)
- [ ] Final Testing
  - [ ] End-to-end smoke tests
  - [ ] Security audit
  - [ ] Performance testing
  - [ ] Deployment verification

### Acceptance Criteria
- [ ] All actuator endpoints working
- [ ] Health checks comprehensive
- [ ] Security hardened
- [ ] Documentation complete
- [ ] Application runs in production mode
- [ ] All tests passing
- [ ] Deployment guide verified

### Success Criteria
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
- ✅ Static analysis passing
- ✅ Application production-ready

**Estimated Time:** 1-2 days

---

## How to Use These Issues

1. **Create Labels** in GitHub: `phase-1` through `phase-9`, `backend`, `testing`, `documentation`, `security`, `infrastructure`, `quality`

2. **Create Milestones**: One for each phase

3. **Copy each issue** into GitHub Issues

4. **Set Dependencies**: Use GitHub's "blocked by" feature or mention dependencies in comments

5. **Assign Team Members** as appropriate

6. **Track Progress** using GitHub Projects or Kanban boards

7. **Update Status** by checking off tasks as they're completed

8. **Link Pull Requests** to issues using `Closes #X` in PR descriptions
