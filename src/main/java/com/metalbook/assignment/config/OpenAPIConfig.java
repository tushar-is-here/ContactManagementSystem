package com.metalbook.assignment.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "JWT Authentication token. Format: Bearer {token}"
)
public class OpenAPIConfig {

    @Value("${app.api.version:1.0.0}")
    private String apiVersion;

    @Value("${app.api.title:Contact Management API}")
    private String apiTitle;

    @Value("${app.api.description:REST API for managing contacts with authentication}")
    private String apiDescription;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(apiTitle)
                        .version(apiVersion)
                        .description("""
                    # Contact Management System REST API
                    
                    A comprehensive REST API for managing contacts with user authentication and authorization.
                    
                    ## Features
                    - User registration and authentication
                    - JWT-based security
                    - CRUD operations for contacts
                    - Advanced search functionality
                    - Pagination and sorting
                    - Input validation
                    - Comprehensive error handling
                    
                    ## Authentication
                    This API uses JWT (JSON Web Tokens) for authentication. To access protected endpoints:
                    1. Register a new user account or login with existing credentials
                    2. Include the JWT token in the Authorization header: `Bearer {token}`
                    
                    ## API Usage
                    1. **Register**: POST `/api/v1/auth/register` - Create a new user account
                    2. **Login**: POST `/api/v1/auth/login` - Authenticate and get JWT token
                    3. **Manage Contacts**: Use `/api/v1/contacts` endpoints with JWT token
                    
                    ## Search Functionality
                    The API supports searching contacts by:
                    - First name (partial matching, case-insensitive)
                    - Last name (partial matching, case-insensitive)
                    - Email (partial matching, case-insensitive)
                    - Multiple criteria combined
                    
                    ## Error Handling
                    The API provides consistent error responses with appropriate HTTP status codes:
                    - 400: Bad Request (validation errors)
                    - 401: Unauthorized (authentication required)
                    - 403: Forbidden (insufficient permissions)
                    - 404: Not Found (resource doesn't exist)
                    - 409: Conflict (duplicate resource)
                    - 500: Internal Server Error
                    """)
                        .contact(new Contact()
                                .name("Contact Management Team")
                                .email("tpanchal484@gmail.com")
                                .url("git@github.com:tushar-is-here/ContactManagementSystem.git"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server"),
                        new Server()
                                .url("https://contactManagementSystem.com")
                                .description("Production Server")
                ));
    }
}
