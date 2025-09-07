package com.metalbook.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AssignmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssignmentApplication.class, args);
		System.out.println("------------------------------------------------------------------------------------------");
		System.out.println("""
            
            ========================================
            🚀 Contact Management System Started! 🚀
            ========================================
            
            📍 API Documentation: http://localhost:8080/swagger-ui.html
            📍 API Docs JSON: http://localhost:8080/v3/api-docs
            📍 Health Check: http://localhost:8080/actuator/health
            
            🔐 Authentication Endpoints:
            POST /api/v1/auth/register - Register new user
            POST /api/v1/auth/login - Login user
            GET  /api/v1/auth/me - Get current user info
            
            📧 Contact Endpoints:
            GET    /api/v1/contacts - Get all contacts (paginated)
            POST   /api/v1/contacts - Create new contact
            GET    /api/v1/contacts/{id} - Get contact by ID
            PUT    /api/v1/contacts/{id} - Update contact
            DELETE /api/v1/contacts/{id} - Delete contact
            GET    /api/v1/contacts/search - Search contacts
            
            💡 Quick Start:
            1. Register: POST /api/v1/auth/register
            2. Login: POST /api/v1/auth/login
            3. Use JWT token in Authorization header: Bearer <token>
            4. Manage contacts with authenticated requests
            
            🛠️  Built with Java 21 + Spring Boot 3.2 + JWT
            ========================================
            """);
	}

}
