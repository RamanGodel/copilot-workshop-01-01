# Mock Exchange Rate Services

This directory contains two mock services that simulate external exchange rate API providers for testing purposes.

## Mock Service 1

**Port:** 8081  
**Endpoint:** `GET /rates?base={currency}`

### Example Request
```bash
curl "http://localhost:8081/rates?base=USD"
```

### Example Response
```json
{
  "base": "USD",
  "timestamp": "2025-12-10T17:30:00",
  "rates": {
    "EUR": 0.8923,
    "USD": 1.0000,
    "GBP": 0.7845,
    "JPY": 135.2300,
    "CHF": 0.9234,
    "CAD": 1.3456,
    "AUD": 1.4123,
    "CNY": 7.1234,
    "INR": 82.5678,
    "BRL": 5.1234
  }
}
```

## Mock Service 2

**Port:** 8082  
**Endpoint:** `GET /api/rates?from={currency}`

### Example Request
```bash
curl "http://localhost:8082/api/rates?from=USD"
```

### Example Response
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
    },
    {
      "currencyCode": "USD",
      "exchangeRate": 1.000000,
      "description": "USD - US Dollar"
    },
    {
      "currencyCode": "GBP",
      "exchangeRate": 0.784567,
      "description": "GBP - British Pound"
    }
  ]
}
```

## Building Services

### Build Mock Service 1
```bash
cd mock-services/mock-exchange-service-1
mvn clean package
```

### Build Mock Service 2
```bash
cd mock-services/mock-exchange-service-2
mvn clean package
```

## Running Locally (without Docker)

### Mock Service 1
```bash
cd mock-services/mock-exchange-service-1
mvn spring-boot:run
```

### Mock Service 2
```bash
cd mock-services/mock-exchange-service-2
mvn spring-boot:run
```

## Features

- **Random Rates:** Both services generate random exchange rates within realistic ranges
- **Multiple Currencies:** Support for 10+ major currencies
- **Different Formats:** Each service returns data in a different format to simulate real-world API diversity
- **Lightweight:** Minimal dependencies, fast startup
- **Dockerized:** Each service has its own Dockerfile

## Notes

- Rates are randomly generated on each request
- No authentication required
- No rate limiting
- Perfect for testing and development
- Data is NOT persisted

