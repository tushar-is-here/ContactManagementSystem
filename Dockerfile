# ================= Build Stage =================
FROM maven:3.9.2-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy project files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Build the application (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# ================= Runtime Stage =================
FROM openjdk:21-jre-slim

WORKDIR /app

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create non-root user
RUN adduser --disabled-password --gecos '' appuser && chown appuser:appuser app.jar
USER appuser

# Expose port
EXPOSE 8080

# Environment variables
ENV SPRING_PROFILES_ACTIVE=production
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check (optional)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]