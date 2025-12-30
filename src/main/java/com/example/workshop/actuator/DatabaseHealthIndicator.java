package com.example.workshop.actuator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Custom health indicator for database connectivity and status.
 * Checks if the database is reachable and can execute queries.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Test database connectivity with a simple query
            long startTime = System.currentTimeMillis();
            ResultSet resultSet = statement.executeQuery("SELECT 1");
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (resultSet.next()) {
                log.debug("Database health check passed in {}ms", responseTime);
                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("responseTime", responseTime + "ms")
                        .withDetail("status", "Connected")
                        .build();
            } else {
                log.warn("Database health check failed - no result returned");
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("status", "Query returned no result")
                        .build();
            }
            
        } catch (SQLException e) {
            log.error("Database health check failed", e);
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", e.getClass().getName())
                    .withDetail("message", e.getMessage())
                    .withDetail("status", "Connection failed")
                    .build();
        }
    }
}
