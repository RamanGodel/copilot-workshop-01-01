# Deployment Guide

This guide provides comprehensive instructions for deploying the Currency Exchange Rate Service to production environments.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Configuration](#environment-configuration)
3. [Database Setup](#database-setup)
4. [Docker Deployment](#docker-deployment)
5. [Kubernetes Deployment](#kubernetes-deployment)
6. [Health Checks](#health-checks)
7. [Monitoring](#monitoring)
8. [Security Considerations](#security-considerations)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### System Requirements

- **Operating System**: Linux (Ubuntu 20.04+ recommended), Windows Server 2019+, or macOS
- **Memory**: Minimum 2GB RAM (4GB+ recommended for production)
- **CPU**: 2+ cores recommended
- **Storage**: 10GB+ available disk space
- **Java**: OpenJDK 21 or Oracle JDK 21
- **Database**: PostgreSQL 16+ (external instance recommended)

### Required Tools

- **Docker** 24.0+ and **Docker Compose** 2.0+ (for container deployment)
- **kubectl** 1.28+ (for Kubernetes deployment)
- **PostgreSQL Client** (for database management)
- **curl** or similar HTTP client (for health checks)

### Network Requirements

- Outbound HTTPS access to external exchange rate providers:
  - `https://api.fixer.io`
  - `https://api.exchangeratesapi.io`
- Inbound access on port 8080 (or your configured port)
- Access to PostgreSQL database (port 5432 by default)

---

## Environment Configuration

### Environment Profiles

The application supports three profiles:

| Profile | Purpose | Configuration File |
|---------|---------|-------------------|
| `dev` | Development | `application-dev.properties` |
| `test` | Testing | `application-test.properties` |
| `prod` | Production | `application-prod.properties` |

Activate a profile by setting:
```bash
export SPRING_PROFILES_ACTIVE=prod
```

### Required Environment Variables

See **[ENVIRONMENT_VARIABLES.md](ENVIRONMENT_VARIABLES.md)** for complete reference.

#### Mandatory Variables (Production)

```bash
# Application Profile
export SPRING_PROFILES_ACTIVE=prod

# Database Configuration
export SPRING_DATASOURCE_URL=jdbc:postgresql://db-host:5432/currency_exchange
export SPRING_DATASOURCE_USERNAME=prod_currency_user
export SPRING_DATASOURCE_PASSWORD=SecurePassword123!

# External Provider API Keys (optional but recommended)
export FIXER_API_KEY=your_fixer_api_key_here
export EXCHANGERATESAPI_API_KEY=your_exchangeratesapi_key_here

# Mock Services (if using)
export MOCK_EXCHANGE_SERVICE_1_URL=http://mock-service-1:8081/api/exchange-rates
export MOCK_EXCHANGE_SERVICE_2_URL=http://mock-service-2:8082/api/exchange-rates
```

#### Optional Variables

```bash
# SSL/TLS Configuration
export SERVER_SSL_KEY_STORE=/etc/ssl/certs/keystore.p12
export SERVER_SSL_KEY_STORE_PASSWORD=KeystorePassword123!
export SERVER_SSL_KEY_ALIAS=currency-exchange

# SSL Verification (disable only for development)
export PROVIDER_SSL_VERIFY=true
```

### Configuration Files

#### Production Configuration (`application-prod.properties`)

Located at `src/main/resources/application-prod.properties`:

```properties
# Profile Activation
spring.config.activate.on-profile=prod

# Database - All values MUST be provided via environment variables
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# Logging - Error level only for production
logging.level.root=ERROR
logging.level.com.example.workshop=INFO

# Security
provider.ssl.verify=${PROVIDER_SSL_VERIFY:true}
```

---

## Database Setup

### PostgreSQL Installation

#### Using Docker

```bash
docker run -d \
  --name currency-postgres \
  -e POSTGRES_DB=currency_exchange \
  -e POSTGRES_USER=prod_currency_user \
  -e POSTGRES_PASSWORD=SecurePassword123! \
  -p 5432:5432 \
  -v postgres-data:/var/lib/postgresql/data \
  postgres:16-alpine
```

#### Using Package Manager (Ubuntu)

```bash
# Install PostgreSQL
sudo apt update
sudo apt install postgresql-16 postgresql-contrib

# Start PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### Database Initialization

1. **Connect to PostgreSQL**:
```bash
psql -U postgres
```

2. **Create Database and User**:
```sql
-- Create database
CREATE DATABASE currency_exchange;

-- Create user
CREATE USER prod_currency_user WITH ENCRYPTED PASSWORD 'SecurePassword123!';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE currency_exchange TO prod_currency_user;

-- Connect to database
\c currency_exchange

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO prod_currency_user;
```

3. **Verify Connection**:
```bash
psql -h localhost -U prod_currency_user -d currency_exchange
```

### Database Migrations

Liquibase migrations run automatically on application startup:

- Creates required tables: `currency`, `exchange_rate`, `role`, `user`, `user_roles`
- Inserts default data: roles (USER, PREMIUM_USER, ADMIN), major currencies
- Creates performance indexes
- Configurable via `application.properties`:
  ```properties
  spring.liquibase.enabled=true
  spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
  ```

---

## Docker Deployment

### Build Docker Image

```bash
# Build the application image
docker build -t currency-exchange-service:latest .

# Or using docker-compose
docker-compose build
```

### Run with Docker Compose

1. **Create `.env` file**:
```env
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/currency_exchange
SPRING_DATASOURCE_USERNAME=currency_user
SPRING_DATASOURCE_PASSWORD=SecurePassword123!
FIXER_API_KEY=your_fixer_api_key
EXCHANGERATESAPI_API_KEY=your_api_key
```

2. **Start services**:
```bash
docker-compose up -d
```

3. **Verify deployment**:
```bash
# Check service health
curl http://localhost:8080/actuator/health/liveness

# View logs
docker-compose logs -f currency-exchange
```

### Docker Compose Production Configuration

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: currency_exchange
      POSTGRES_USER: ${SPRING_DATASOURCE_USERNAME}
      POSTGRES_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
    volumes:
      - postgres-prod-data:/var/lib/postgresql/data
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${SPRING_DATASOURCE_USERNAME}"]
      interval: 10s
      timeout: 5s
      retries: 5

  currency-exchange:
    image: currency-exchange-service:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/currency_exchange
      SPRING_DATASOURCE_USERNAME: ${SPRING_DATASOURCE_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
      FIXER_API_KEY: ${FIXER_API_KEY}
      EXCHANGERATESAPI_API_KEY: ${EXCHANGERATESAPI_API_KEY}
    depends_on:
      postgres:
        condition: service_healthy
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health/liveness"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

volumes:
  postgres-prod-data:
```

Run with:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

---

## Kubernetes Deployment

### Prerequisites

- Kubernetes cluster (1.28+)
- kubectl configured
- Container registry access

### Create Kubernetes Secrets

```bash
# Create namespace
kubectl create namespace currency-exchange

# Create database credentials secret
kubectl create secret generic db-credentials \
  --from-literal=username=prod_currency_user \
  --from-literal=password=SecurePassword123! \
  -n currency-exchange

# Create API keys secret
kubectl create secret generic api-keys \
  --from-literal=fixer-api-key=your_fixer_api_key \
  --from-literal=exchangeratesapi-api-key=your_api_key \
  -n currency-exchange
```

### PostgreSQL Deployment

Create `postgres-deployment.yaml`:

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
  namespace: currency-exchange
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: currency-exchange
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:16-alpine
        env:
        - name: POSTGRES_DB
          value: currency_exchange
        - name: POSTGRES_USER
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
        ports:
        - containerPort: 5432
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
        livenessProbe:
          exec:
            command:
            - pg_isready
            - -U
            - prod_currency_user
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          exec:
            command:
            - pg_isready
            - -U
            - prod_currency_user
          initialDelaySeconds: 5
          periodSeconds: 5
      volumes:
      - name: postgres-storage
        persistentVolumeClaim:
          claimName: postgres-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: postgres
  namespace: currency-exchange
spec:
  selector:
    app: postgres
  ports:
  - port: 5432
    targetPort: 5432
```

### Application Deployment

Create `app-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: currency-exchange
  namespace: currency-exchange
spec:
  replicas: 3
  selector:
    matchLabels:
      app: currency-exchange
  template:
    metadata:
      labels:
        app: currency-exchange
    spec:
      containers:
      - name: currency-exchange
        image: currency-exchange-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          value: jdbc:postgresql://postgres:5432/currency_exchange
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
        - name: FIXER_API_KEY
          valueFrom:
            secretKeyRef:
              name: api-keys
              key: fixer-api-key
        - name: EXCHANGERATESAPI_API_KEY
          valueFrom:
            secretKeyRef:
              name: api-keys
              key: exchangeratesapi-api-key
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
---
apiVersion: v1
kind: Service
metadata:
  name: currency-exchange
  namespace: currency-exchange
spec:
  type: LoadBalancer
  selector:
    app: currency-exchange
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
```

### Deploy to Kubernetes

```bash
# Apply PostgreSQL deployment
kubectl apply -f postgres-deployment.yaml

# Wait for PostgreSQL to be ready
kubectl wait --for=condition=ready pod -l app=postgres -n currency-exchange --timeout=300s

# Apply application deployment
kubectl apply -f app-deployment.yaml

# Wait for application to be ready
kubectl wait --for=condition=ready pod -l app=currency-exchange -n currency-exchange --timeout=300s

# Get service URL
kubectl get service currency-exchange -n currency-exchange
```

### Verify Deployment

```bash
# Check pod status
kubectl get pods -n currency-exchange

# View logs
kubectl logs -f deployment/currency-exchange -n currency-exchange

# Check health
kubectl exec -it deployment/currency-exchange -n currency-exchange -- \
  curl http://localhost:8080/actuator/health/liveness
```

---

## Health Checks

### Available Health Endpoints

| Endpoint | Purpose | Access |
|----------|---------|--------|
| `/actuator/health` | Full health status | ADMIN only |
| `/actuator/health/liveness` | Kubernetes liveness probe | Public |
| `/actuator/health/readiness` | Kubernetes readiness probe | Public |

### Liveness Probe

Indicates if the application is running:
```bash
curl http://localhost:8080/actuator/health/liveness
```

**Response** (Healthy):
```json
{
  "status": "UP"
}
```

### Readiness Probe

Indicates if the application is ready to serve traffic:
```bash
curl http://localhost:8080/actuator/health/readiness
```

**Response** (Ready):
```json
{
  "status": "UP"
}
```

### Full Health Check (ADMIN Only)

Provides detailed health information:
```bash
curl -u admin:admin123 http://localhost:8080/actuator/health
```

**Response**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()",
        "responseTime": "5ms"
      }
    },
    "externalProviders": {
      "status": "UP",
      "details": {
        "totalProviders": 4,
        "healthyProviders": 4,
        "providers": [
          {
            "name": "MockExchangeService1",
            "status": "UP",
            "availableCurrencies": 10
          },
          {
            "name": "MockExchangeService2",
            "status": "UP",
            "availableCurrencies": 8
          }
        ]
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Health Check Configuration

Configure in `application.properties`:
```properties
# Show detailed health information (ADMIN only)
management.endpoint.health.show-details=when-authorized

# Enable specific health indicators
management.health.db.enabled=true
management.health.diskspace.enabled=true

# Kubernetes probes
management.endpoint.health.probes.enabled=true
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true
```

---

## Monitoring

### Actuator Endpoints

All management endpoints require ADMIN role:

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health status |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus-format metrics |
| `/actuator/loggers` | Logger configuration |
| `/actuator/env` | Environment properties |

### Metrics

Access metrics (ADMIN only):
```bash
curl -u admin:admin123 http://localhost:8080/actuator/metrics
```

Key metrics:
- `exchange.rate.requests` - Total exchange rate requests
- `jvm.memory.used` - JVM memory usage
- `system.cpu.usage` - CPU usage
- `http.server.requests` - HTTP request statistics

### Prometheus Integration

Expose metrics for Prometheus:
```bash
curl http://localhost:8080/actuator/prometheus
```

### Logging

#### Log Levels

Configured per environment:
- **Development**: DEBUG
- **Test**: WARN
- **Production**: INFO (application), ERROR (root)

#### Log Files

- **Application Log**: `logs/application.log`
- **Error Log**: `logs/error.log`

#### View Logs

```bash
# Docker
docker-compose logs -f currency-exchange

# Kubernetes
kubectl logs -f deployment/currency-exchange -n currency-exchange

# Local file
tail -f logs/application.log
```

---

## Security Considerations

### Authentication

- **Session-based authentication** with Spring Security
- **BCrypt password hashing** for secure credential storage
- **Default accounts** (change immediately in production):
  - `admin` / `admin123` (ADMIN)
  - `premium` / `premium123` (PREMIUM_USER)
  - `user` / `user123` (USER)

### Security Headers

Automatically configured:
- **HSTS**: Enforce HTTPS for 1 year
- **CSP**: Content Security Policy
- **X-Frame-Options**: Prevent clickjacking
- **XSS Protection**: Enable XSS filtering

### HTTPS Configuration

Enable HTTPS by setting environment variables:
```bash
export SERVER_SSL_KEY_STORE=/path/to/keystore.p12
export SERVER_SSL_KEY_STORE_PASSWORD=keystorePassword
export SERVER_SSL_KEY_ALIAS=currency-exchange
```

### Database Security

- Use strong passwords (12+ characters, mixed case, numbers, symbols)
- Restrict database user permissions to minimum required
- Use SSL/TLS for database connections in production
- Rotate credentials regularly

### API Keys

- Store API keys as environment variables or Kubernetes secrets
- Never commit API keys to version control
- Rotate API keys periodically
- Monitor API usage for anomalies

### Network Security

- Use firewall rules to restrict access
- Enable CORS only for trusted origins
- Use HTTPS in production
- Implement rate limiting (if needed)

### Secrets Management

#### Kubernetes Secrets
```bash
kubectl create secret generic app-secrets \
  --from-file=db-password=./db-password.txt \
  --from-file=api-keys=./api-keys.txt \
  -n currency-exchange
```

#### Docker Secrets
```bash
# Create secrets
echo "SecurePassword123!" | docker secret create db_password -
echo "api_key_value" | docker secret create fixer_api_key -

# Reference in docker-compose.yml
services:
  app:
    secrets:
      - db_password
      - fixer_api_key
    environment:
      SPRING_DATASOURCE_PASSWORD_FILE: /run/secrets/db_password
```

---

## Troubleshooting

### Common Issues

#### Application Won't Start

**Symptoms**: Application fails to start, throws exceptions

**Solutions**:
1. Check database connectivity:
   ```bash
   psql -h db-host -U username -d currency_exchange
   ```
2. Verify environment variables are set:
   ```bash
   echo $SPRING_DATASOURCE_URL
   echo $SPRING_DATASOURCE_USERNAME
   ```
3. Check application logs:
   ```bash
   docker-compose logs currency-exchange
   ```
4. Ensure Java 21 is installed:
   ```bash
   java -version
   ```

#### Database Connection Errors

**Symptoms**: `Connection refused`, `Authentication failed`

**Solutions**:
1. Verify PostgreSQL is running:
   ```bash
   docker-compose ps postgres
   ```
2. Check database credentials:
   ```bash
   psql -h localhost -U prod_currency_user -d currency_exchange
   ```
3. Verify network connectivity:
   ```bash
   telnet db-host 5432
   ```
4. Check firewall rules

#### Health Check Failures

**Symptoms**: `/actuator/health` returns `DOWN`

**Solutions**:
1. Check individual health indicators:
   ```bash
   curl -u admin:admin123 http://localhost:8080/actuator/health
   ```
2. Verify database health:
   ```bash
   curl http://localhost:8080/actuator/health/db
   ```
3. Check external provider connectivity
4. Review application logs for errors

#### High Memory Usage

**Symptoms**: OutOfMemoryError, pod restarts

**Solutions**:
1. Increase heap size:
   ```bash
   export JAVA_OPTS="-Xms512m -Xmx2g"
   ```
2. Monitor memory usage:
   ```bash
   curl -u admin:admin123 http://localhost:8080/actuator/metrics/jvm.memory.used
   ```
3. Check for memory leaks in logs
4. Adjust Kubernetes resource limits

#### API Requests Failing

**Symptoms**: 401 Unauthorized, 403 Forbidden, 500 errors

**Solutions**:
1. Verify authentication:
   - Login via Swagger UI or get session cookie
2. Check user roles:
   - Ensure user has required role for endpoint
3. Review error logs:
   ```bash
   tail -f logs/error.log
   ```
4. Test with curl:
   ```bash
   curl -u admin:admin123 http://localhost:8080/api/v1/currencies
   ```

#### External Provider Issues

**Symptoms**: No exchange rates available, provider health DOWN

**Solutions**:
1. Check API keys are set:
   ```bash
   echo $FIXER_API_KEY
   ```
2. Verify network connectivity to providers
3. Check provider health indicator:
   ```bash
   curl -u admin:admin123 http://localhost:8080/actuator/health
   ```
4. Review provider response in logs
5. Use mock services for testing

### Debug Mode

Enable debug logging temporarily:
```bash
# Set environment variable
export LOGGING_LEVEL_COM_EXAMPLE_WORKSHOP=DEBUG

# Or via Actuator (ADMIN only)
curl -X POST -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}' \
  http://localhost:8080/actuator/loggers/com.example.workshop
```

### Support Resources

- **Documentation**: [docs/](.)
- **Environment Variables**: [ENVIRONMENT_VARIABLES.md](ENVIRONMENT_VARIABLES.md)
- **Testing Guide**: [TESTING_GUIDE.md](TESTING_GUIDE.md)
- **Implementation Plan**: [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md)
- **Swagger UI**: http://localhost:8080/swagger-ui.html

---

## Maintenance

### Database Backups

```bash
# Backup
docker exec currency-postgres pg_dump -U prod_currency_user currency_exchange > backup.sql

# Restore
docker exec -i currency-postgres psql -U prod_currency_user currency_exchange < backup.sql
```

### Application Updates

```bash
# Build new image
docker build -t currency-exchange-service:v2.0.0 .

# Update Kubernetes deployment
kubectl set image deployment/currency-exchange \
  currency-exchange=currency-exchange-service:v2.0.0 \
  -n currency-exchange

# Monitor rollout
kubectl rollout status deployment/currency-exchange -n currency-exchange
```

### Scaling

```bash
# Docker Compose (limited)
docker-compose up -d --scale currency-exchange=3

# Kubernetes
kubectl scale deployment currency-exchange --replicas=5 -n currency-exchange
```

---

**For additional support, refer to other documentation files or raise an issue in the project repository.**
