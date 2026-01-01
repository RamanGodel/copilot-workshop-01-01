# Environment Variables Configuration

This document lists all environment variables used by the Currency Exchange Rate Service.

## Database Configuration

| Variable | Description | Required | Default (Dev) | Example |
|----------|-------------|----------|---------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL database URL | Yes | `jdbc:postgresql://localhost:5432/currency_exchange` | `jdbc:postgresql://prod-db:5432/currency_exchange` |
| `SPRING_DATASOURCE_USERNAME` | Database username | Yes | `currency_user` | `prod_currency_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | Yes | `currency_pass` | `SecurePassword123!` |

## Mock Service Configuration

| Variable | Description | Required | Default (Dev) | Example |
|----------|-------------|----------|---------------|---------|
| `MOCK_EXCHANGE_SERVICE_1_URL` | Mock Exchange Service 1 URL | No | `http://localhost:8081/api/exchange-rates` | `http://mock-service-1:8081/api/exchange-rates` |
| `MOCK_EXCHANGE_SERVICE_2_URL` | Mock Exchange Service 2 URL | No | `http://localhost:8082/api/exchange-rates` | `http://mock-service-2:8082/api/exchange-rates` |

## External Provider API Keys

| Variable | Description | Required | Default (Dev) | Example |
|----------|-------------|----------|---------------|---------|
| `FIXER_API_KEY` | Fixer.io API key | No | `your_fixer_api_key_here` | `abc123def456...` |
| `EXCHANGERATESAPI_API_KEY` | ExchangeRatesAPI key | No | `your_exchangeratesapi_key_here` | `xyz789uvw012...` |

## SSL/TLS Configuration

| Variable | Description | Required | Default (Dev) | Example |
|----------|-------------|----------|---------------|---------|
| `SERVER_SSL_KEY_STORE` | Path to SSL keystore file | No (HTTPS only) | N/A | `/etc/ssl/certs/keystore.p12` |
| `SERVER_SSL_KEY_STORE_PASSWORD` | SSL keystore password | No (HTTPS only) | N/A | `KeystorePass123!` |
| `SERVER_SSL_KEY_ALIAS` | SSL certificate alias | No (HTTPS only) | N/A | `currency-exchange` |
| `PROVIDER_SSL_VERIFY` | Enable SSL certificate verification | No | `true` | `false` (dev only) |

## Profile Activation

Set the active profile using:
```bash
SPRING_PROFILES_ACTIVE=dev|test|prod
```

## Environment-Specific Configurations

### Development (`application-dev.properties`)
- Uses localhost defaults for all services
- H2 console enabled
- Debug logging enabled
- SSL certificate verification disabled for development

### Test (`application-test.properties`)
- Uses H2 in-memory database
- No external dependencies required
- Minimal logging
- Fast startup and execution

### Production (`application-prod.properties`)
- **All environment variables are mandatory** (no defaults)
- SSL certificate verification enabled
- Error-level logging only
- Security-first configuration

## Setting Environment Variables

### Linux/macOS
```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/currency_exchange"
export SPRING_DATASOURCE_USERNAME="currency_user"
export SPRING_DATASOURCE_PASSWORD="currency_pass"
export SPRING_PROFILES_ACTIVE="prod"
```

### Windows PowerShell
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/currency_exchange"
$env:SPRING_DATASOURCE_USERNAME="currency_user"
$env:SPRING_DATASOURCE_PASSWORD="currency_pass"
$env:SPRING_PROFILES_ACTIVE="prod"
```

### Docker Compose
```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/currency_exchange
  - SPRING_DATASOURCE_USERNAME=currency_user
  - SPRING_DATASOURCE_PASSWORD=currency_pass
  - SPRING_PROFILES_ACTIVE=prod
```

### Kubernetes
```yaml
env:
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
  - name: SPRING_PROFILES_ACTIVE
    value: prod
```

## Security Best Practices

1. **Never commit secrets** to version control
2. **Use different credentials** for each environment
3. **Rotate passwords** regularly
4. **Use Kubernetes Secrets** or similar tools in production
5. **Enable SSL/TLS** in production environments
6. **Restrict database user permissions** to minimum required
7. **Use strong passwords** (minimum 12 characters, mixed case, numbers, symbols)
8. **Monitor and audit** access to secrets

## Validation

To verify your environment variables are set correctly:

```bash
# Check database connectivity
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Check actuator health endpoint
curl http://localhost:8080/actuator/health

# Check with specific profile
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```
