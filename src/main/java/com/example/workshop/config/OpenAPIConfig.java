package com.example.workshop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for Swagger documentation.
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Currency Exchange Rates Provider API")
                        .version("1.0.0")
                        .description("""
                                REST API for managing currency exchange rates from multiple providers.
                                
                                This production-ready service provides:
                                - Up-to-date currency exchange rates from multiple sources
                                - Scheduled hourly rate updates
                                - Historical trends analysis
                                - Role-based access control (USER, PREMIUM_USER, ADMIN)
                                - Comprehensive health checks and monitoring
                                - Resilient provider fallback strategies
                                
                                ## Authentication
                                Use one of the default accounts to access protected endpoints:
                                - admin/admin123 (ADMIN) - Full access
                                - premium/premium123 (PREMIUM_USER) - Public + trends
                                - user/user123 (USER) - Public endpoints only
                                
                                ## Rate Limiting
                                For production use, please contact support for API keys and rate limits.
                                """)
                        .contact(new Contact()
                                .name("Currency Exchange API Support")
                                .email("support@example.com")
                                .url("https://github.com/example/currency-exchange"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server"),
                        new Server()
                                .url("https://api.example.com")
                                .description("Production server (configure in deployment)")
                ));
    }
}

