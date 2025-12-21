# Phase 4: Security Implementation - Quick Start Guide

## What's New in Phase 4

✅ **Spring Security Integration** - Form-based authentication with role-based access control
✅ **Three User Roles** - USER, PREMIUM_USER, and ADMIN with different permissions
✅ **Secure Endpoints** - Role-based access to API endpoints
✅ **Custom Login Page** - Modern, responsive login interface
✅ **Default Test Users** - Pre-configured users for immediate testing

## Quick Start

### 1. Start the Application

```bash
mvn spring-boot:run
```

Or with Docker Compose:
```bash
docker-compose up
```

### 2. Access the Application

Open your browser and navigate to:
- **Application**: http://localhost:8080
- **Auto-redirects to**: http://localhost:8080/login

### 3. Login with Demo Credentials

Choose one of the following accounts based on what you want to test:

#### 👑 Admin Account (Full Access)
```
Username: admin
Password: admin123
```
**Can access:**
- All public endpoints
- Add new currencies
- Refresh exchange rates
- View currency trends
- Access actuator endpoints

#### ⭐ Premium User (Trends Access)
```
Username: premium
Password: premium123
```
**Can access:**
- All public endpoints
- View currency trends
- Cannot add currencies or refresh rates

#### 👤 Regular User (Basic Access)
```
Username: user
Password: user123
```
**Can access:**
- All public endpoints only
- Cannot access trends, add currencies, or refresh rates

### 4. After Login

You'll be automatically redirected to:
**Swagger UI**: http://localhost:8080/swagger-ui.html

From there, you can:
- View all API endpoints
- Test endpoints directly from the browser
- See which endpoints require authentication

## Testing API Endpoints

### Public Endpoints (No Login Required)

```bash
# Get all currencies
curl http://localhost:8080/api/v1/currencies

# Get exchange rate
curl "http://localhost:8080/api/v1/currencies/exchange-rates?from=USD&to=EUR&amount=100"
```

### Protected Endpoints (Login Required)

For browser-based testing, just login first and use Swagger UI.

For command-line testing with curl:

#### Method 1: Using Session Cookie (after login via browser)
1. Login at http://localhost:8080/login
2. Copy the JSESSIONID cookie from browser dev tools
3. Use it in curl:

```bash
curl -X POST http://localhost:8080/api/v1/currencies?currency=PLN \
  -H "Cookie: JSESSIONID=your-session-id-here"
```

#### Method 2: Using Basic Auth (if enabled)
```bash
# Add currency (requires ADMIN)
curl -X POST "http://localhost:8080/api/v1/currencies?currency=PLN" \
  -u admin:admin123

# Refresh rates (requires ADMIN)
curl -X POST http://localhost:8080/api/v1/currencies/refresh \
  -u admin:admin123

# Get trends (requires PREMIUM_USER or ADMIN)
curl "http://localhost:8080/api/v1/currencies/trends?from=USD&to=EUR&period=7D" \
  -u premium:premium123
```

## Endpoint Security Reference

| Endpoint | Method | Access Level | Roles |
|----------|--------|--------------|-------|
| `/api/v1/currencies` | GET | Public | All |
| `/api/v1/currencies/exchange-rates` | GET | Public | All |
| `/api/v1/currencies` | POST | Protected | ADMIN |
| `/api/v1/currencies/refresh` | POST | Protected | ADMIN |
| `/api/v1/currencies/trends` | GET | Protected | ADMIN, PREMIUM_USER |
| `/swagger-ui/**` | GET | Public | All |
| `/actuator/**` | GET | Protected | ADMIN |

## Logout

To logout, navigate to:
```
http://localhost:8080/logout
```

Or click the logout button if available in the UI.

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Security Tests Only
```bash
mvn test -Dtest=SecurityConfigTest,CustomUserDetailsServiceTest,LoginIntegrationTest
```

### Run Controller Tests (with Security)
```bash
mvn test -Dtest=CurrencyControllerWebTest
```

## Troubleshooting

### "Access Denied" Error

**Problem**: Getting 403 Forbidden on endpoints
**Solution**: 
- Check if you're logged in
- Verify your user has the required role
- Try logging out and back in

### Cannot Login

**Problem**: Login fails with correct credentials
**Solution**:
- Check application logs for errors
- Verify database is running (if using Docker)
- Ensure users were created on startup (check logs for "Created admin user...")

### Session Expired

**Problem**: "Session has expired" message
**Solution**:
- Simply login again
- Session timeout is configured in application.properties
- Your session cookie may have been cleared

### Tests Failing

**Problem**: Security tests failing
**Solution**:
- Ensure all dependencies are installed: `mvn clean install`
- Check if roles exist in test database
- Verify test annotations are correct

## Next Steps

1. ✅ **Try Different User Roles** - Login with each account to see access differences
2. ✅ **Test API Endpoints** - Use Swagger UI to test endpoints with different roles
3. ✅ **Check Logs** - Watch application logs for authentication events
4. ✅ **Run Tests** - Verify all security tests pass

## Additional Resources

- **Full Documentation**: See [PHASE4_SECURITY_SUMMARY.md](PHASE4_SECURITY_SUMMARY.md)
- **Implementation Plan**: See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md)
- **Spring Security Docs**: https://docs.spring.io/spring-security/reference/

## What's Next?

**Phase 5: External Provider Integration**
- Connect to real exchange rate APIs (fixer.io, exchangeratesapi.io)
- Implement circuit breaker and retry logic
- Add fallback strategies
- Handle provider failures gracefully

---

**Need Help?** Check the logs or review the comprehensive documentation in PHASE4_SECURITY_SUMMARY.md

