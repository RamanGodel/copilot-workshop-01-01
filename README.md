# Currency Exchange Rates Provider Service

A comprehensive Spring Boot application that provides up-to-date currency exchange rates from multiple sources, with scheduled updates, role-based access control, and comprehensive testing.

## 🚀 Features

- **RESTful API** for currency exchange rates with validation and error handling
- **Multiple Exchange Rate Providers** with fallback strategies
- **Database Persistence** with PostgreSQL and Liquibase migrations
- **🔐 Role-Based Security** with Spring Security (NEW in Phase 4!)
- **Scheduled Updates** - Automatic hourly rate refresh
- **Docker Support** - Full containerization with Docker Compose
- **API Documentation** - Interactive Swagger UI
- **Comprehensive Testing** - Unit, integration, and functional tests
- **Code Quality** - CheckStyle, PMD, JaCoCo coverage

## 🔐 Security (Phase 4)

The application implements Spring Security with role-based access control:

### Default User Accounts

| Username | Password | Role | Access Level |
|----------|----------|------|--------------|
| `admin` | `admin123` | ADMIN | Full access to all endpoints |
| `premium` | `premium123` | PREMIUM_USER | Public endpoints + trends |
| `user` | `user123` | USER | Public endpoints only |

### Endpoint Security

- **Public Access** (no authentication):
  - `GET /api/v1/currencies` - List currencies
  - `GET /api/v1/currencies/exchange-rates` - Get exchange rates
  - `/swagger-ui/**` - API documentation

- **ADMIN Only**:
  - `POST /api/v1/currencies` - Add currency
  - `POST /api/v1/currencies/refresh` - Refresh rates
  - `/actuator/**` - Monitoring endpoints

- **ADMIN & PREMIUM_USER**:
  - `GET /api/v1/currencies/trends` - Currency trends

### Login

1. Navigate to http://localhost:8080
2. You'll be redirected to the login page
3. Use one of the default accounts above
4. After login, access Swagger UI at http://localhost:8080/swagger-ui.html

📚 **Full Security Documentation**: See [docs/PHASE4_QUICKSTART.md](docs/PHASE4_QUICKSTART.md)

## 📋 Prerequisites

### For Local Development
- Java 21
- Maven 3.6+
- PostgreSQL 16+ (optional if using Docker)

### For Docker Deployment
- Docker Desktop
- Docker Compose
- 4GB+ available RAM

## 🏗️ Architecture

The application consists of:
- **Main Application** (port 8080) - Currency Exchange API
- **PostgreSQL Database** (port 5432) - Data persistence
- **Mock Service 1** (port 8081) - Test exchange rate provider
- **Mock Service 2** (port 8082) - Test exchange rate provider (different format)

## 🐳 Quick Start with Docker (Recommended)

### 1. Setup Environment

```powershell
# Copy environment template
Copy-Item .env.example .env

# Edit .env if needed (optional)
```

### 2. Start All Services

```powershell
# Using the helper script
.\docker-manager.ps1 up

# Or using docker-compose directly
docker-compose up --build -d
```

### 3. Verify Services

- **Main Application**: http://localhost:8080/actuator/health
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Mock Service 1**: http://localhost:8081/rates?base=USD
- **Mock Service 2**: http://localhost:8082/api/rates?from=USD

### 4. Stop Services

```powershell
.\docker-manager.ps1 down
# or
docker-compose down
```

## 💻 Local Development (Without Docker)

### 1. Setup Database

```bash
# Install PostgreSQL 16+ and create database
createdb currency_exchange
```

### 2. Configure Application

Edit `src/main/resources/application-dev.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/currency_exchange
spring.datasource.username=admin
spring.datasource.password=admin123
```

### 3. Build and Run

```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 🔌 API Endpoints

### Public Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/currencies` | Get all available currencies |
| GET | `/api/v1/currencies/exchange-rates` | Get exchange rate between currencies |
| GET | `/swagger-ui.html` | Interactive API documentation |
| GET | `/actuator/health` | Health check endpoint |

### Protected Endpoints

| Method | Endpoint | Required Role | Description |
|--------|----------|---------------|-------------|
| POST | `/api/v1/currencies` | ADMIN | Add a new currency |
| POST | `/api/v1/currencies/refresh` | ADMIN | Trigger manual rate refresh |
| GET | `/api/v1/currencies/trends` | PREMIUM_USER, ADMIN | Get rate trends |

### Example Request

```bash
curl "http://localhost:8080/api/v1/currencies/exchange-rates?from=USD&to=EUR&amount=100"
```

### Example Response

```json
{
  "from": "USD",
  "to": "EUR",
  "amount": 100.00,
  "result": 89.23,
  "rate": 0.8923,
  "timestamp": "2025-12-10T17:30:00"
}
```

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

### Run with Coverage

```bash
mvn clean test jacoco:report
# View report at: target/site/jacoco/index.html
```

### Code Quality Checks

```bash
# CheckStyle
mvn checkstyle:check

# PMD
mvn pmd:check

# All quality checks
mvn clean verify
```

## 📚 Documentation

- **[Implementation Plan](docs/IMPLEMENTATION_PLAN.md)** - Detailed development phases
- **[Docker Setup Guide](DOCKER_SETUP.md)** - Complete Docker documentation
- **[Testing Guide](docs/TESTING_GUIDE.md)** - Testing strategy and examples
- **[API Documentation](http://localhost:8080/swagger-ui.html)** - Interactive API docs (when running)

## 🛠️ Development Tools

### Docker Manager Script

Simplify Docker operations with the PowerShell script:

```powershell
.\docker-manager.ps1 [command]

Commands:
  build     - Build all Docker images
  up        - Start all services
  down      - Stop all services
  restart   - Restart all services
  logs      - View service logs
  status    - Check service status
  clean     - Remove all containers and volumes
  test      - Test all endpoints
  help      - Show help
```

### Default Users

For testing protected endpoints:

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| premium | premium123 | PREMIUM_USER |
| user | user123 | USER |

## 📁 Project Structure

```
copilot-workshop-01-01/
├── src/
│   ├── main/
│   │   ├── java/com/example/workshop/
│   │   │   ├── Application.java
│   │   │   ├── config/         # Configuration classes
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── dto/           # Data Transfer Objects
│   │   │   ├── exception/     # Custom exceptions
│   │   │   ├── model/         # JPA entities
│   │   │   ├── repository/    # Spring Data repositories
│   │   │   ├── service/       # Business logic
│   │   │   └── validation/    # Custom validators
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-test.properties
│   │       └── application-prod.properties
│   └── test/
│       └── java/com/example/workshop/
│           ├── controller/     # Controller tests
│           ├── service/       # Service tests
│           └── integration/   # Integration tests
├── mock-services/
│   ├── mock-exchange-service-1/
│   └── mock-exchange-service-2/
├── docs/                      # Additional documentation
├── docker-compose.yml         # Docker Compose configuration
├── Dockerfile                 # Main app Dockerfile
├── docker-manager.ps1         # Docker management script
└── pom.xml                    # Maven configuration
```

## 🔒 Security

- Spring Security with role-based access control (RBAC)
- BCrypt password encryption
- CSRF protection enabled
- Secure headers configured
- Input validation on all endpoints

## 🚀 Deployment

### Production Checklist

- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Configure secure database credentials
- [ ] Set up HTTPS/SSL certificates
- [ ] Configure external PostgreSQL
- [ ] Set up monitoring and logging
- [ ] Configure secrets management
- [ ] Review security settings

See [DOCKER_SETUP.md](DOCKER_SETUP.md) for detailed deployment instructions.

## 🤝 Contributing

1. Follow the [Implementation Plan](docs/IMPLEMENTATION_PLAN.md)
2. Write tests for new features
3. Ensure all quality checks pass
4. Update documentation
5. Submit pull request

## 📝 Development Status

### ✅ Phase 1: REST Layer (Complete)
- REST API with validation
- Error handling
- Swagger documentation
- Comprehensive tests

### 🚧 Phase 2: Docker & Docker Compose (In Progress)
- Dockerfile for main app ✅
- Mock services ✅
- Docker Compose configuration ✅
- Documentation ✅

### 📋 Upcoming Phases
- Phase 3: Database Integration (JPA, Liquibase)
- Phase 4: Security Implementation
- Phase 5: External Provider Integration
- Phase 6: Scheduled Jobs
- Phase 7: Advanced Features (Caching, Monitoring)
- Phase 8: Code Quality (CheckStyle, PMD, JaCoCo)
- Phase 9: Production Readiness

## 📄 License

This project is for educational purposes.

## 🆘 Support

- Check [DOCKER_SETUP.md](DOCKER_SETUP.md) for troubleshooting
- Review [docs/IMPLEMENTATION_PLAN.md](docs/IMPLEMENTATION_PLAN.md) for architecture details
- Test endpoints using Swagger UI

---

**Happy Coding! 🎉**

