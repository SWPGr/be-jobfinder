# Build stage
FROM openjdk:21-jdk-slim AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies (cache layer)
RUN ./mvnw dependency:resolve

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests

# Final stage
FROM openjdk:21-jre-slim

WORKDIR /app

# Copy JAR file from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user
RUN groupadd -r appuser && useradd --no-log-init -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# Expose port for Spring Boot app
EXPOSE 8080

# Add healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
