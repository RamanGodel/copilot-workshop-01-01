# Multi-stage build for Currency Exchange Rates Provider Service
# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install CA certificates so HTTPS works (Fixer/ExchangeRatesApi, etc.)
USER root
RUN apk add --no-cache ca-certificates openssl su-exec && update-ca-certificates

# Ensure JVM uses the system CA bundle (Alpine)
ENV JAVA_TOOL_OPTIONS="-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts -Djavax.net.ssl.trustStorePassword=changeit"

# Optional corporate CA hook (mounted via docker-compose)
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Run entrypoint as root (needed to update CA trust stores), then it will drop to 'spring'
USER root

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["/entrypoint.sh"]
