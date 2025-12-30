package com.example.workshop.actuator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DatabaseHealthIndicator.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private DatabaseHealthIndicator healthIndicator;

    @Test
    void shouldReturnUpWhenDatabaseIsHealthy() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("database");
        assertThat(health.getDetails()).containsKey("responseTime");
        assertThat(health.getDetails()).containsKey("status");
        assertThat(health.getDetails().get("status")).isEqualTo("Connected");
    }

    @Test
    void shouldReturnDownWhenDatabaseConnectionFails() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("database");
        assertThat(health.getDetails()).containsKey("error");
        assertThat(health.getDetails()).containsKey("message");
        assertThat(health.getDetails().get("status")).isEqualTo("Connection failed");
    }

    @Test
    void shouldReturnDownWhenQueryReturnsNoResult() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("status");
        assertThat(health.getDetails().get("status")).isEqualTo("Query returned no result");
    }
}
