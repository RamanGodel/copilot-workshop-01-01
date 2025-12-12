# Docker Setup Guide

This guide explains how to run the Currency Exchange Rates Provider Service using Docker and Docker Compose.

## Prerequisites

- Docker Desktop installed (Windows/Mac/Linux)
- Docker Compose (included with Docker Desktop)
- At least 4GB of available RAM
- Ports 5432, 8080, 8081, 8082 available

## Architecture

The application consists of 4 services:

1. **PostgreSQL Database** (port 5432) - Main database
2. **Main Application** (port 8080) - Currency Exchange API
3. **Mock Service 1** (port 8081) - Mock exchange rate provider with format: `GET /rates?base={currency}`
4. **Mock Service 2** (port 8082) - Mock exchange rate provider with format: `GET /api/rates?from={currency}`

## Quick Start

### 1. Environment Setup

Copy the example environment file:

```powershell
Copy-Item .env.example .env
```

Edit `.env` if you want to change default values:

```env
POSTGRES_DB=currency_exchange
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin123
SPRING_PROFILES_ACTIVE=dev
```

### 2. Build and Run All Services

```powershell
docker-compose up --build
```

This will:
- Build all Docker images
- Start PostgreSQL database
- Start both mock services
- Start the main application
- Create a network for service communication

### 3. Verify Services

Once all services are running, verify:

- **Main Application**: http://localhost:8080/actuator/health
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Mock Service 1**: http://localhost:8081/rates?base=USD
- **Mock Service 2**: http://localhost:8082/api/rates?from=USD
- **PostgreSQL**: localhost:5432 (use database client)

## Docker Commands

### Start Services (detached mode)

```powershell
docker-compose up -d
```

### Stop Services

```powershell
docker-compose down
```

### Stop and Remove Volumes (clean database)

```powershell
docker-compose down -v
```

### View Logs

```powershell
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f app
docker-compose logs -f postgres
docker-compose logs -f mock-service-1
docker-compose logs -f mock-service-2
```

### Rebuild Services

```powershell
# Rebuild all
docker-compose up --build

# Rebuild specific service
docker-compose up --build app
```

### Check Service Status

```powershell
docker-compose ps
```

### Execute Commands in Running Container

```powershell
# Access PostgreSQL
docker exec -it currency-exchange-db psql -U admin -d currency_exchange

# Access main application container
docker exec -it currency-exchange-app sh
```

## Testing Mock Services

### Mock Service 1 (Simple Format)

```powershell
curl http://localhost:8081/rates?base=USD
```

Response format:
```json
{
  "base": "USD",
  "timestamp": "2025-12-10T17:30:00",
  "rates": {
    "EUR": 0.8923,
    "GBP": 0.7845,
    "JPY": 135.2300
  }
}
```

### Mock Service 2 (Complex Format)

```powershell
curl http://localhost:8082/api/rates?from=USD
```

Response format:
```json
{
  "success": true,
  "source": "USD",
  "lastUpdate": 1702228200,
  "data": [
    {
      "currencyCode": "EUR",
      "exchangeRate": 0.892345,
      "description": "EUR - Euro"
    }
  ]
}
```

## Troubleshooting

### Port Already in Use

If you see "port already in use" errors:

```powershell
# Check what's using the port (example for port 8080)
netstat -ano | findstr :8080

# Stop the process or change port in docker-compose.yml
```

### Database Connection Issues

If the app can't connect to PostgreSQL:

1. Check database is healthy:
   ```powershell
   docker-compose ps
   ```

2. Check database logs:
   ```powershell
   docker-compose logs postgres
   ```

3. Wait for database to be ready (health check takes ~30 seconds)

### Container Build Fails

If Maven build fails:

1. Check your internet connection
2. Clear Maven cache:
   ```powershell
   docker-compose down
   docker system prune -a
   docker-compose up --build
   ```

### Out of Memory

If you see OOM errors:

1. Increase Docker Desktop memory allocation (Settings > Resources)
2. Recommended: 4GB minimum

### Services Not Starting

Check individual service health:

```powershell
# Check all containers
docker ps -a

# Check specific service logs
docker-compose logs app
```

## Database Management

### Access PostgreSQL CLI

```powershell
docker exec -it currency-exchange-db psql -U admin -d currency_exchange
```

### Run SQL Queries

```sql
-- List all tables
\dt

-- View schema
\d+ table_name

-- Query data
SELECT * FROM currency;
```

### Backup Database

```powershell
docker exec currency-exchange-db pg_dump -U admin currency_exchange > backup.sql
```

### Restore Database

```powershell
cat backup.sql | docker exec -i currency-exchange-db psql -U admin -d currency_exchange
```

## Development Workflow

### 1. Make Code Changes

Edit your Java code in `src/` directory

### 2. Rebuild and Restart

```powershell
# Rebuild only the main app
docker-compose up --build app

# Or rebuild everything
docker-compose down
docker-compose up --build
```

### 3. View Logs

```powershell
docker-compose logs -f app
```

## Environment Variables

The main application supports these environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| SPRING_PROFILES_ACTIVE | dev | Active Spring profile (dev/test/prod) |
| SPRING_DATASOURCE_URL | jdbc:postgresql://postgres:5432/currency_exchange | Database URL |
| SPRING_DATASOURCE_USERNAME | admin | Database username |
| SPRING_DATASOURCE_PASSWORD | admin123 | Database password |
| MOCK_SERVICE_1_URL | http://mock-service-1:8081 | Mock service 1 URL |
| MOCK_SERVICE_2_URL | http://mock-service-2:8082 | Mock service 2 URL |

## Network Configuration

All services run on a custom bridge network called `currency-network`. Services can communicate using their service names:

- `postgres` - Database host
- `mock-service-1` - Mock service 1 host
- `mock-service-2` - Mock service 2 host
- `app` - Main application host

## Health Checks

All services have health checks configured:

- **PostgreSQL**: Checked every 10s using `pg_isready`
- **Mock Services**: Checked every 30s using HTTP endpoint
- **Main App**: Checked every 30s using `/actuator/health`

The main application will wait for all dependencies to be healthy before starting.

## Production Deployment

For production deployment:

1. Update `.env` with secure credentials
2. Set `SPRING_PROFILES_ACTIVE=prod`
3. Use external PostgreSQL instead of container (recommended)
4. Configure proper secrets management
5. Set up SSL/TLS certificates
6. Configure reverse proxy (nginx/traefik)
7. Set up monitoring and logging

## Next Steps

After successfully running with Docker:

1. Test all API endpoints via Swagger UI
2. Verify mock services are being called
3. Check PostgreSQL for data persistence
4. Move to Phase 3: Database Integration (JPA, Liquibase)

