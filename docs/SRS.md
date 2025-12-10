# Software Requirements Specification (SRS)

## Currency Exchange Rates Provider Service

**Version:** 1.0  
**Date:** December 10, 2025  
**Project:** Currency Exchange Rates Provider Service

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Overall Description](#2-overall-description)
3. [System Features and Requirements](#3-system-features-and-requirements)
4. [External Interface Requirements](#4-external-interface-requirements)
5. [Non-Functional Requirements](#5-non-functional-requirements)
6. [Technical Requirements](#6-technical-requirements)
7. [Appendices](#7-appendices)

---

## 1. Introduction

### 1.1 Purpose
This Software Requirements Specification (SRS) document provides a comprehensive description of the Currency Exchange Rates Provider Service. The service is designed to deliver up-to-date exchange rates for a configurable list of supported currencies.

### 1.2 Scope
The Currency Exchange Rates Provider Service will:
- Provide RESTful API endpoints for currency exchange rate management
- Integrate with multiple external currency exchange rate providers
- Support role-based access control with three user roles: USER, PREMIUM_USER, and ADMIN
- Store and manage exchange rate data with historical tracking
- Automatically refresh exchange rates on a scheduled basis

### 1.3 Definitions, Acronyms, and Abbreviations
- **API**: Application Programming Interface
- **REST**: Representational State Transfer
- **CRUD**: Create, Read, Update, Delete
- **SRS**: Software Requirements Specification
- **JPA**: Java Persistence API
- **RBAC**: Role-Based Access Control

### 1.4 References
- Spring Boot Documentation
- PostgreSQL Documentation
- Liquibase Documentation
- OpenAPI/Swagger Specification
- External Exchange Rate Providers: fixer.io, exchangeratesapi.io, openexchangerates.org, currencylayer.com

---

## 2. Overall Description

### 2.1 Product Perspective
The Currency Exchange Rates Provider Service is a standalone microservice that aggregates currency exchange rates from multiple external providers and exposes them through a unified REST API with role-based access control.

### 2.2 Product Functions
- Currency management (add, list currencies)
- Exchange rate retrieval from multiple sources
- Real-time exchange rate calculations
- Historical exchange rate trend analysis
- Automated scheduled rate updates
- User authentication and authorization
- Role-based access control

### 2.3 User Classes and Characteristics

#### 2.3.1 USER
- Basic access level
- Can view currency lists
- Can perform exchange rate conversions

#### 2.3.2 PREMIUM_USER
- Enhanced access level
- All USER permissions
- Can view exchange rate trends and historical data

#### 2.3.3 ADMIN
- Full administrative access
- All PREMIUM_USER permissions
- Can add new currencies
- Can manually trigger exchange rate refresh

### 2.4 Operating Environment
- **Runtime Environment**: Docker containers
- **Database**: PostgreSQL
- **Platform**: Cross-platform (via Docker)
- **Network**: HTTP/HTTPS REST API

### 2.5 Design and Implementation Constraints
- Must use Java 21
- Must use Spring Boot framework
- Must use Maven for build management
- Must use PostgreSQL as database
- Must use Liquibase for database schema management
- Must use Docker and docker-compose for deployment
- Must implement at least 2 external exchange rate provider integrations

---

## 3. System Features and Requirements

### 3.1 Currency Management

#### 3.1.1 FR-001: List Currencies
**Description**: Retrieve a list of all currencies configured in the system.

**Priority**: High

**Functional Requirements**:
- The system shall provide an endpoint to retrieve all supported currencies
- Initially, the currency list shall be empty
- The endpoint shall be accessible to all authenticated users

**API Specification**:
- **Method**: GET
- **Endpoint**: `/api/v1/currencies`
- **Access**: All users (USER, PREMIUM_USER, ADMIN)
- **Response**: JSON array of currency codes

#### 3.1.2 FR-002: Add Currency
**Description**: Add a new currency to the supported currency list.

**Priority**: High

**Functional Requirements**:
- The system shall provide an endpoint to add new currencies
- Only ADMIN users shall be authorized to add currencies
- The system shall validate currency code format
- The system shall prevent duplicate currency entries

**API Specification**:
- **Method**: POST
- **Endpoint**: `/api/v1/currencies?currency={CURRENCY_CODE}`
- **Access**: ADMIN only
- **Parameters**: currency (required, 3-letter ISO code)
- **Response**: Success/failure status

### 3.2 Exchange Rate Operations

#### 3.2.1 FR-003: Get Exchange Rates
**Description**: Calculate and return exchange rate conversion for specified amount and currency pair.

**Priority**: High

**Functional Requirements**:
- The system shall calculate exchange rates between two currencies
- The system shall support amount-based conversions
- The system shall use the most recent exchange rate data
- The endpoint shall be accessible to all authenticated users

**API Specification**:
- **Method**: GET
- **Endpoint**: `/api/v1/currencies/exchange-rates?amount={AMOUNT}&from={FROM_CURRENCY}&to={TO_CURRENCY}`
- **Access**: All users (USER, PREMIUM_USER, ADMIN)
- **Parameters**: 
  - amount (required, numeric)
  - from (required, 3-letter ISO code)
  - to (required, 3-letter ISO code)
- **Response**: JSON object with conversion result

#### 3.2.2 FR-004: Refresh Exchange Rates
**Description**: Manually trigger an update of exchange rates from external sources.

**Priority**: Medium

**Functional Requirements**:
- The system shall provide an endpoint to manually trigger rate updates
- Only ADMIN users shall be authorized to trigger manual updates
- The system shall fetch rates from all configured external sources
- The system shall store updated rates in the database

**API Specification**:
- **Method**: POST
- **Endpoint**: `/api/v1/currencies/refresh`
- **Access**: ADMIN only
- **Response**: Success/failure status with update timestamp

#### 3.2.3 FR-005: Get Exchange Rate Trends
**Description**: Retrieve historical exchange rate trends showing percentage change over a specified period.

**Priority**: Medium

**Functional Requirements**:
- The system shall calculate percentage change in exchange rates over time
- The system shall support multiple time period formats (hours, days, months, years)
- Minimum supported period shall be 12 hours (12H)
- The endpoint shall be accessible to ADMIN and PREMIUM_USER only

**API Specification**:
- **Method**: GET
- **Endpoint**: `/api/v1/currencies/trends?from={FROM_CURRENCY}&to={TO_CURRENCY}&period={PERIOD}`
- **Access**: ADMIN and PREMIUM_USER only
- **Parameters**:
  - from (required, 3-letter ISO code)
  - to (required, 3-letter ISO code)
  - period (required, format: NNU where NN=number, U=unit [H/D/M/Y], min: 12H)
- **Response**: JSON object with trend data and percentage change

### 3.3 External Integration

#### 3.3.1 FR-006: External Provider Integration
**Description**: Integrate with multiple external currency exchange rate providers.

**Priority**: High

**Functional Requirements**:
- The system shall integrate with at least 2 external exchange rate APIs
- Supported providers may include: fixer.io, exchangeratesapi.io, openexchangerates.org, currencylayer.com
- The system shall handle provider failures gracefully
- The system shall aggregate data from multiple sources

#### 3.3.2 FR-007: Mock Exchange Rate Providers
**Description**: Implement mock endpoints simulating external exchange rate providers.

**Priority**: Medium

**Functional Requirements**:
- The system shall provide 2 mock endpoints simulating exchange rate providers
- Mock endpoints shall return random exchange rates on each request
- Mock endpoints shall support the same currency pairs as real providers
- Exchange rates may be hardcoded or generated randomly

### 3.4 Scheduled Operations

#### 3.4.1 FR-008: Automated Exchange Rate Updates
**Description**: Automatically fetch and update exchange rates on a scheduled basis.

**Priority**: High

**Functional Requirements**:
- The system shall automatically fetch exchange rates when the service starts
- The system shall schedule automatic updates every hour
- The system shall fetch rates from all configured providers (external and mock)
- The system shall store all fetched rates in the database with timestamps

### 3.5 Data Persistence

#### 3.5.1 FR-009: Exchange Rate Storage
**Description**: Store exchange rates in a relational database with historical tracking.

**Priority**: High

**Functional Requirements**:
- The system shall store exchange rates in PostgreSQL database
- Exchange rate table shall include columns: ID, base_currency, target_currency, rate, timestamp
- The system shall maintain historical exchange rate data
- The system shall use Spring Data JPA for data access

#### 3.5.2 FR-010: Database Schema Management
**Description**: Manage database schema using Liquibase migrations.

**Priority**: High

**Functional Requirements**:
- The system shall use Liquibase for database schema management
- All schema changes shall be implemented via migrations
- Migrations may be in XML, SQL, YAML, or JSON format
- Migrations shall be version-controlled and applied automatically

### 3.6 Security

#### 3.6.1 FR-011: User Authentication
**Description**: Implement user authentication using Spring Security.

**Priority**: High

**Functional Requirements**:
- The system shall provide a login page for user authentication
- The system shall support username/password authentication
- Passwords shall be encrypted in the database
- The system shall maintain user sessions

#### 3.6.2 FR-012: Role-Based Access Control
**Description**: Implement role-based authorization for API endpoints.

**Priority**: High

**Functional Requirements**:
- The system shall support three roles: USER, PREMIUM_USER, ADMIN
- Users shall be stored in the database
- Roles shall be stored in the database
- One user may have multiple roles
- The system shall enforce role-based access control on all endpoints

**Role-Endpoint Matrix**:

| Endpoint | USER | PREMIUM_USER | ADMIN |
|----------|------|--------------|-------|
| GET /api/v1/currencies | ✓ | ✓ | ✓ |
| POST /api/v1/currencies | ✗ | ✗ | ✓ |
| GET /api/v1/currencies/exchange-rates | ✓ | ✓ | ✓ |
| POST /api/v1/currencies/refresh | ✗ | ✗ | ✓ |
| GET /api/v1/currencies/trends | ✗ | ✓ | ✓ |

---

## 4. External Interface Requirements

### 4.1 User Interfaces
- **Login Page**: Web-based login form for user authentication
- **API Documentation UI**: Interactive API documentation (Swagger UI)

### 4.2 Hardware Interfaces
Not applicable - service is platform-independent via Docker containerization.

### 4.3 Software Interfaces

#### 4.3.1 External APIs
- **Exchange Rate Providers**: Integration with at least 2 external REST APIs
  - fixer.io (optional)
  - exchangeratesapi.io (optional)
  - openexchangerates.org (optional)
  - currencylayer.com (optional)

#### 4.3.2 Database
- **PostgreSQL**: Relational database for persistent storage
- **Connection**: JDBC connection via Spring Data JPA

#### 4.3.3 Containerization
- **Docker**: Application containerization
- **Docker Compose**: Multi-container orchestration

### 4.4 Communication Interfaces
- **Protocol**: HTTP/HTTPS
- **Data Format**: JSON
- **API Style**: REST

---

## 5. Non-Functional Requirements

### 5.1 Performance Requirements
- **NFR-001**: API response time shall be less than 2 seconds for single currency conversions
- **NFR-002**: System shall handle concurrent requests from multiple users
- **NFR-003**: Scheduled updates shall complete within 5 minutes

### 5.2 Security Requirements
- **NFR-004**: All passwords shall be encrypted using industry-standard algorithms
- **NFR-005**: API endpoints shall enforce role-based access control
- **NFR-006**: System shall protect against common security vulnerabilities (SQL injection, XSS, CSRF)

### 5.3 Reliability Requirements
- **NFR-007**: System shall handle external provider failures gracefully
- **NFR-008**: System shall maintain service availability even if one provider is unavailable
- **NFR-009**: Database transactions shall be ACID-compliant

### 5.4 Maintainability Requirements
- **NFR-010**: Code shall follow Java coding standards and best practices
- **NFR-011**: Code shall be modular and follow SOLID principles
- **NFR-012**: System shall use dependency injection for loose coupling

### 5.5 Portability Requirements
- **NFR-013**: Application shall run in Docker containers
- **NFR-014**: System shall be platform-independent
- **NFR-015**: All dependencies shall be managed via Maven

---

## 6. Technical Requirements

### 6.1 Development Environment

#### 6.1.1 TR-001: Programming Language
**Requirement**: Java 21 shall be used for application development.

#### 6.1.2 TR-002: Framework
**Requirement**: Spring Boot framework shall be used for application structure.

#### 6.1.3 TR-003: Build Tool
**Requirement**: Maven shall be used for dependency management and build automation.

### 6.2 Testing Requirements

#### 6.2.1 TR-004: Unit Testing
**Requirements**:
- Models, controllers, and services shall have unit test coverage
- JUnit 5 shall be used as the testing framework
- Test coverage shall be measured and reported

#### 6.2.2 TR-005: Integration Testing
**Requirements**:
- Functional/integration tests shall be implemented
- Spring Test Framework shall be used for integration testing
- TestContainers shall be used for database integration tests
- WireMock shall be used to validate external API requests

#### 6.2.3 TR-006: Validation Testing
**Requirements**:
- Controller validation shall be tested using @WebMvcTest
- Exception handling scenarios shall be covered by tests
- All validation annotations (e.g., @NotEmpty) shall be tested

### 6.3 Code Quality Requirements

#### 6.3.1 TR-007: Static Code Analysis
**Requirements**:
- Jacoco shall be configured for code coverage analysis
- CheckStyle shall be configured for code style enforcement
- PMD shall be configured for code quality checks
- PiTest may be configured for mutation testing (optional)

#### 6.3.2 TR-008: Code Standards
**Requirements**:
- Optional class shall be used where appropriate
- Stream API shall be used for collection operations
- Lombok annotations shall be used to reduce boilerplate code

### 6.4 API Documentation

#### 6.4.1 TR-009: API Documentation
**Requirements**:
- API shall be documented using OpenAPI/Swagger Specification
- Documentation may be dynamic (generated) or static
- All endpoints shall be documented with parameters and responses

### 6.5 Exception Handling

#### 6.5.1 TR-010: Exception Management
**Requirements**:
- @RestControllerAdvice shall be used for global exception handling
- Exceptions shall return appropriate JSON error responses
- HTTP status codes shall reflect the error type
- Expected exception scenarios shall be covered by tests

### 6.6 Validation

#### 6.6.1 TR-011: Input Validation
**Requirements**:
- Validation annotations shall be used on controller parameters
- Bean validation (JSR-303) shall be implemented
- Custom validation may be implemented where needed

### 6.7 Deployment Requirements

#### 6.7.1 TR-012: Containerization
**Requirements**:
- Main application shall run in a Docker container
- Mock exchange services shall run in Docker containers
- PostgreSQL database shall run in a Docker container
- docker-compose.yml shall orchestrate all containers
- All services shall be networked and accessible

---

## 7. Appendices

### 7.1 Appendix A: Database Schema

#### Exchange Rates Table
```
Table: exchange_rates
Columns:
  - id: Primary Key (Auto-increment)
  - base_currency: VARCHAR(3), NOT NULL
  - target_currency: VARCHAR(3), NOT NULL
  - rate: DECIMAL, NOT NULL
  - timestamp: TIMESTAMP, NOT NULL
```

#### Users Table
```
Table: users
Columns:
  - id: Primary Key
  - username: VARCHAR, UNIQUE, NOT NULL
  - password: VARCHAR (encrypted), NOT NULL
  - enabled: BOOLEAN
```

#### Roles Table
```
Table: roles
Columns:
  - id: Primary Key
  - name: VARCHAR, UNIQUE, NOT NULL (USER, PREMIUM_USER, ADMIN)
```

#### User-Roles Association Table
```
Table: user_roles
Columns:
  - user_id: Foreign Key -> users(id)
  - role_id: Foreign Key -> roles(id)
  - Primary Key: (user_id, role_id)
```

### 7.2 Appendix B: API Endpoint Summary

| Method | Endpoint | Access Level | Description |
|--------|----------|--------------|-------------|
| GET | /api/v1/currencies | All | List all currencies |
| POST | /api/v1/currencies | ADMIN | Add new currency |
| GET | /api/v1/currencies/exchange-rates | All | Get exchange rate conversion |
| POST | /api/v1/currencies/refresh | ADMIN | Manually refresh rates |
| GET | /api/v1/currencies/trends | ADMIN, PREMIUM_USER | Get rate trends |

### 7.3 Appendix C: Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA
- **Migration**: Liquibase
- **Security**: Spring Security
- **Testing**: JUnit 5, Spring Test, TestContainers, WireMock
- **Code Quality**: Jacoco, CheckStyle, PMD, PiTest (optional)
- **Documentation**: Swagger/OpenAPI
- **Utilities**: Lombok, Java Optional, Stream API
- **Containerization**: Docker, Docker Compose

### 7.4 Appendix D: Period Format Examples

For the trends endpoint, the period parameter supports the following formats:
- **Hours**: 12H, 24H, 48H (minimum: 12H)
- **Days**: 1D, 7D, 10D, 30D
- **Months**: 1M, 3M, 6M, 12M
- **Years**: 1Y, 2Y, 5Y

### 7.5 Appendix E: Success Criteria

The project shall be considered complete when:
1. All functional requirements (FR-001 through FR-012) are implemented
2. All technical requirements (TR-001 through TR-012) are met
3. Unit test coverage exceeds 80%
4. Integration tests cover all major user workflows
5. All endpoints are documented in Swagger/OpenAPI
6. Application successfully runs in Docker containers
7. Security authentication and authorization are fully functional
8. Code quality checks pass (CheckStyle, PMD, Jacoco)

---

**Document Version History**

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | December 10, 2025 | System | Initial SRS document created from requirements.txt |

---

**End of Document**

