# Quick Start Guide

Get the Currency Exchange Rates Provider Service running in 5 minutes!

## Option 1: Docker (Recommended) ⚡

### Step 1: Prerequisites
- Install [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Ensure Docker is running

### Step 2: Start Services

```powershell
# Clone/navigate to project directory
cd C:\Z_LEARNING\copilot\copilot-workshop-01-01

# Copy environment file
Copy-Item .env.example .env

# Start all services (this may take 5-10 minutes first time)
docker-compose up --build
```

### Step 3: Verify

Open your browser and visit:
- **Main App Health**: http://localhost:8080/actuator/health
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Mock Service 1**: http://localhost:8081/rates?base=USD
- **Mock Service 2**: http://localhost:8082/api/rates?from=USD

### Step 4: Test API

Try this in PowerShell:

```powershell
# Get all currencies
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/currencies" -UseBasicParsing | Select-Object -Expand Content

# Get exchange rate
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/currencies/exchange-rates?from=USD&to=EUR&amount=100" -UseBasicParsing | Select-Object -Expand Content
```

Or use curl:

```bash
curl http://localhost:8080/api/v1/currencies
curl "http://localhost:8080/api/v1/currencies/exchange-rates?from=USD&to=EUR&amount=100"
```

### Step 5: Stop Services

```powershell
# Stop all services
docker-compose down

# Or use the helper script
.\docker-manager.ps1 down
```

---

## Option 2: Local Development 💻

### Step 1: Prerequisites
- Java 21 JDK installed
- Maven 3.6+ installed
- PostgreSQL 16+ installed (optional)

### Step 2: Run with H2 Database (No PostgreSQL needed)

```powershell
# Build the project
mvn clean install

# Run with test profile (uses H2 in-memory database)
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

### Step 3: Access Application

Open http://localhost:8080/swagger-ui.html

---

## Common Commands

### Using Docker Manager Script

```powershell
# Start services
.\docker-manager.ps1 up

# View logs
.\docker-manager.ps1 logs

# Check status
.\docker-manager.ps1 status

# Test endpoints
.\docker-manager.ps1 test

# Stop services
.\docker-manager.ps1 down

# Clean everything (removes volumes)
.\docker-manager.ps1 clean
```

### Using Docker Compose Directly

```powershell
# Start in foreground (see logs)
docker-compose up --build

# Start in background
docker-compose up -d --build

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

---

## Troubleshooting

### Port Already in Use

If port 8080, 8081, 8082, or 5432 is already in use:

```powershell
# Check what's using the port
netstat -ano | findstr :8080

# Kill the process or change the port in docker-compose.yml
```

### Docker Build Fails

```powershell
# Clean Docker cache
docker system prune -a

# Rebuild
docker-compose up --build
```

### Services Won't Start

```powershell
# Check Docker Desktop is running
# Check available disk space (need ~2GB)
# Check logs:
docker-compose logs
```

### "Out of Memory" Error

1. Open Docker Desktop
2. Go to Settings → Resources
3. Increase Memory to at least 4GB
4. Restart Docker Desktop

---

## What's Next?

1. **Explore the API** - Use Swagger UI to test all endpoints
2. **Check the Mock Services** - They return random exchange rates
3. **View the Database** - Connect to PostgreSQL on localhost:5432
   - Database: `currency_exchange`
   - Username: `admin`
   - Password: `admin123`
4. **Read the Documentation**:
   - [Docker Setup Guide](DOCKER_SETUP.md)
   - [Implementation Plan](docs/IMPLEMENTATION_PLAN.md)
   - [Testing Guide](docs/TESTING_GUIDE.md)

---

## Test Endpoints

### Get All Currencies
```bash
curl http://localhost:8080/api/v1/currencies
```

### Get Exchange Rate
```bash
curl "http://localhost:8080/api/v1/currencies/exchange-rates?from=USD&to=EUR&amount=100"
```

### Add Currency (requires ADMIN role - not yet implemented)
```bash
curl -X POST "http://localhost:8080/api/v1/currencies?currency=CHF"
```

### Check Application Health
```bash
curl http://localhost:8080/actuator/health
```

### Test Mock Service 1
```bash
curl "http://localhost:8081/rates?base=USD"
```

### Test Mock Service 2
```bash
curl "http://localhost:8082/api/rates?from=EUR"
```

---

## Need Help?

- Check the [DOCKER_SETUP.md](DOCKER_SETUP.md) for detailed troubleshooting
- Review logs: `docker-compose logs -f`
- Test services: `.\docker-manager.ps1 test`
- Check service status: `docker-compose ps`

**Happy Testing! 🚀**

