# Contact Management System REST API

A comprehensive REST API for managing contacts with user authentication and authorization, built with Java 21, Spring Boot 3.2, and JWT authentication.

## 🚀 Features

- **User Authentication**: Register, login, and JWT-based security
- **Contact Management**: Full CRUD operations for contacts
- **Advanced Search**: Search by first name, last name, or email with partial matching
- **Pagination & Sorting**: Efficient data retrieval with configurable page sizes
- **Input Validation**: Comprehensive validation with detailed error messages
- **API Documentation**: Interactive Swagger/OpenAPI documentation
- **Security**: JWT tokens, password encryption, and authorization
- **Database Support**: H2 (development) and PostgreSQL (production)
- **Containerization**: Docker support with multi-stage builds
- **Deployment Ready**: Configured for Heroku deployment

## 🛠️ Technology Stack

- **Java 21** - Latest LTS with modern features (Records, Pattern Matching, Text Blocks)
- **Spring Boot 3.2** - Latest framework with native support and performance improvements
- **Spring Security 6** - JWT authentication and authorization
- **Spring Data JPA** - Data persistence with Hibernate
- **PostgreSQL** - Production database
- **Maven** - Dependency management and build tool
- **Docker** - Containerization
- **Swagger/OpenAPI 3** - API documentation
- **JUnit 5 & Mockito** - Testing framework

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL (for production)
- Docker & Docker Compose (optional)

## 🏃‍♂️ Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd contact-management-system
```

### 2. Run with Maven (Development)
```bash
# Using H2 in-memory database
./mvnw spring-boot:run

# Or with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=development
```

### 3. Run with Docker Compose
```bash
# Start PostgreSQL and API together
docker-compose up -d

# View logs
docker-compose logs -f contact-api
```

### 4. Access the Application
- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

## 📖 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "tushar_is_here",
  "password": "securePassword123",
  "email": "tpanchal484@gmail.com"
}
```

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "tushar_is_here",
  "password": "securePassword123"
}
```

#### Get Current User Info
```http
GET /api/v1/auth/me
Authorization: Bearer <jwt-token>
```

### Contact Management Endpoints

#### Create Contact
```http
POST /api/v1/contacts
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "firstName": "Tushar",
  "lastName": "Panchal",
  "email": "tpanchal484@gmail.com",
  "phoneNumber": "+1234567890"
}
```

#### Get All Contacts (Paginated)
```http
GET /api/v1/contacts?page=0&size=20&sortBy=firstName&sortDirection=asc
Authorization: Bearer <jwt-token>
```

#### Get Contact by ID
```http
GET /api/v1/contacts/{id}
Authorization: Bearer <jwt-token>
```

#### Update Contact
```http
PUT /api/v1/contacts/{id}
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@example.com",
  "phoneNumber": "+1234567890"
}
```

#### Delete Contact
```http
DELETE /api/v1/contacts/{id}
Authorization: Bearer <jwt-token>
```

#### Search Contacts
```http
GET /api/v1/contacts/search?firstName=Tushar&lastName=Panchal&email=gmail.com&page=0&size=10
Authorization: Bearer <jwt-token>
```

#### Get Contact Statistics
```http
GET /api/v1/contacts/stats
Authorization: Bearer <jwt-token>
```

## 🔧 Configuration

### Application Properties

The application supports multiple profiles:

- **default**: Uses Postgres local DB
- **production**: Uses PostgreSQL database
- **test**: Uses H2 for testing

### Environment Variables

For production deployment, set these environment variables:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/contactdb
DB_USERNAME=your-username
DB_PASSWORD=your-password
JWT_SECRET=your-very-secure-secret-key-here
JWT_EXPIRATION=86400000
SPRING_PROFILES_ACTIVE=production
```

### Database Configuration

#### Postgres (Development)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/metalbook
    driver-class-name: org.postgressql.Driver
    username: sa
    password: password
```

#### PostgreSQL (Production)
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

## 🧪 Testing

### Run Tests
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ContactServiceTest

# Run tests with coverage
./mvnw test jacoco:report
```

### Test Categories
- **Unit Tests**: Service and repository layer tests
- **Integration Tests**: Full application context tests
- **Web Layer Tests**: Controller and API tests

## 📦 Build & Deployment

### Build JAR
```bash
./mvnw clean package
```

### Build Docker Image
```bash
docker build -t contact-management-api .
```

### Deploy to Heroku

1. **Create Railway App**
   ```bash
   Create new app on Railway and connect GitHub repository with it.
   ```

2. **Add PostgreSQL Addon**
   ```bash
   jdbc:postgresql://switchyard.proxy.rlwy.net:31370/railway
   ```

3. **Set Environment Variables**
   ```bash
   ENV: prod
   ```

4. **Deploy**
   ```bash
   Set up automatic deploys from the main branch or deploy manually with CI/CD.
   ```

### Deploy with Docker

```bash
# Build and run
docker build -t contact-management-system .
docker run -d -p 8080:8080 --name cms-container contact-management-system

# Or use docker-compose
docker-compose up -d

# Check if the container is running
docker ps

# Access your application
http://localhost:8080/swagger-ui.html

# View logs
docker logs -f cms-container

# Stop and remove container
docker stop cms-container
docker rm cms-container
docker rmi contact-management-system
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/example/contactmanagement/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects (Records)
│   │   ├── entity/         # JPA entities
│   │   ├── exception/      # Custom exceptions
│   │   ├── repository/     # Data access layer
│   │   ├── security/       # Security configuration
│   │   ├── service/        # Business logic
│   │   └── ContactManagementApplication.java
│   └── resources/
│       ├── application.yml
│       └── application-production.yml
├── test/
│   └── java/              # Test classes
├── docker-compose.yml
├── Dockerfile
├── Procfile              # Heroku deployment
└── pom.xml
```

## 🔒 Security Features

- **JWT Authentication**: Stateless authentication with configurable expiration
- **Password Encryption**: BCrypt hashing with configurable strength
- **CORS Support**: Configurable cross-origin resource sharing
- **Input Validation**: Comprehensive validation with custom error messages
- **SQL Injection Protection**: JPA/Hibernate parameter binding
- **Authorization**: User-specific data access control
- **Security Headers**: Standard security headers configuration

## 🚨 Error Handling

The API provides consistent error responses with appropriate HTTP status codes:

### Error Response Format
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "path": "/api/v1/contacts",
  "timestamp": "2024-01-20T10:30:00",
  "fieldErrors": {
    "email": "Email should be valid",
    "firstName": "First name is required"
  }
}
```

### HTTP Status Codes
- **200 OK**: Successful GET, PUT requests
- **201 Created**: Successful POST requests
- **400 Bad Request**: Validation errors, malformed requests
- **401 Unauthorized**: Authentication required or failed
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **409 Conflict**: Resource already exists (duplicate email)
- **500 Internal Server Error**: Unexpected server errors

## 📊 Java 21 Features Used

This project leverages modern Java 21 features:

### Records (DTOs)
```java
public record ContactRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Email String email,
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{10,15}$") String phoneNumber
) {}
```

### Switch Expressions (Pattern Matching)
```java
private SearchType determineSearchType(ContactSearchParams params) {
    return switch ((hasFirstName ? 1 : 0) + (hasLastName ? 2 : 0) + (hasEmail ? 4 : 0)) {
        case 1 -> SearchType.FIRST_NAME_ONLY;
        case 2 -> SearchType.LAST_NAME_ONLY;
        case 4 -> SearchType.EMAIL_ONLY;
        case 0 -> SearchType.ALL_CONTACTS;
        default -> SearchType.MULTIPLE_CRITERIA;
    };
}
```

### Text Blocks (Documentation)
```java
System.out.println("""
    🚀 Contact Management System Started! 🚀
    📍 API Documentation: http://localhost:8080/swagger-ui.html
    🔐 Authentication Endpoints: /api/v1/auth/*
    📧 Contact Endpoints: /api/v1/contacts/*
    """);
```

## 🧪 API Testing Examples

### Using cURL

#### 1. Register a new user
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com"
  }'
```

#### 2. Login and get JWT token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

#### 3. Create a contact (use token from login)
```bash
curl -X POST http://localhost:8080/api/v1/contacts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "firstName": "Tushar",
    "lastName": "Panchal",
    "email": "tpanchal484@gmail.com",
    "phoneNumber": "+1234567890"
  }'
```

#### 4. Search contacts
```bash
curl -X GET "http://localhost:8080/api/v1/contacts/search?firstName=Tushar&page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Using Postman

1. **Import Collection**: Create a Postman collection with the provided API endpoints
2. **Environment Variables**: Set up variables for base URL and JWT token
3. **Pre-request Scripts**: Add scripts to automatically set authorization headers

## 🔍 Monitoring & Health Checks

### Health Check Endpoint
```bash
curl http://localhost:8080/actuator/health
```

### Response
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

## 🐳 Docker Configuration

### Multi-stage Dockerfile Benefits
- **Smaller Image Size**: Separates build and runtime environments
- **Security**: Uses non-root user and minimal JRE image
- **Performance**: Optimized layer caching for faster builds

### Docker Compose Services
- **PostgreSQL**: Database with health checks and persistent storage
- **Contact API**: Main application with dependency management
- **Nginx**: Optional reverse proxy with SSL support

## 📈 Performance Considerations

### Database Optimization
- **Indexes**: Automatic indexing on email and user_id fields
- **Pagination**: Efficient data retrieval with configurable page sizes
- **Connection Pooling**: HikariCP for optimal database connections

### Caching Strategies
- **JPA Second-Level Cache**: Hibernate caching for frequently accessed data
- **Query Optimization**: Custom JPQL queries for complex searches
- **Stateless Design**: JWT tokens eliminate server-side session storage

### Memory Management
- **Java 21 Optimizations**: G1GC garbage collector with improved performance
- **Resource Limits**: Docker container memory limits and JVM heap sizing
- **Connection Management**: Proper resource cleanup and connection management

## 🔧 Troubleshooting

### Common Issues

#### 1. Database Connection Issues
```bash
# Check if PostgreSQL is running when deployed with Docker
docker-compose ps postgres

# View database logs
docker-compose logs postgres
```

#### 2. JWT Token Issues
- **Invalid Token**: Check token format and expiration
- **Missing Header**: Ensure Authorization header is properly set
- **Secret Key**: Verify JWT_SECRET environment variable

#### 3. Validation Errors
- **Email Format**: Ensure valid email format
- **Phone Number**: Use international format with optional + prefix
- **Required Fields**: All fields are mandatory except where noted

#### 4. CORS Issues
```yaml
# Update CORS configuration in SecurityConfig if needed
configuration.setAllowedOriginPatterns(List.of("*"));
```

## 📚 Additional Resources

- **Spring Boot Documentation**: https://docs.spring.io/spring-boot/docs/current/reference/html/
- **Spring Security**: https://docs.spring.io/spring-security/reference/
- **JWT.io**: https://jwt.io/ (for token debugging)
- **PostgreSQL Documentation**: https://www.postgresql.org/docs/
- **Docker Documentation**: https://docs.docker.com/

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Contact Management Team**
- Email: tpanchal484@gmail.com
- GitHub: [@tushar-is-here](https://github.com/tushar-is-here)

---

## 🎯 Assignment Completion Checklist

✅ **REST API Implementation**: Complete CRUD operations for contacts  
✅ **Contact Properties**: First Name, Last Name, Email, Phone Number  
✅ **Search Functionality**: Search by first name, last name, or email  
✅ **Security**: JWT-based authentication and authorization  
✅ **API Documentation**: Comprehensive OpenAPI/Swagger documentation  
✅ **Spring Boot**: Built with Spring Boot 3.2 and Java 21  
✅ **Code Quality**: Well-organized, maintainable code following best practices  
✅ **Deployment**: Docker configuration and Heroku deployment setup  
✅ **Testing**: Unit tests, integration tests, and API tests  
✅ **Error Handling**: Comprehensive error handling and validation

**Deployment URL**: Replace with your actual Heroku deployment URL after deployment.

**Total Development Time**: Estimated 8-12 hours for a complete implementation with testing and documentation.