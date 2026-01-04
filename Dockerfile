# Stage 1: Build the application
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy Gradle wrapper files (gradlew.bat is Windows-only, not needed for Linux build)
COPY gradlew .
COPY gradle gradle

# Ensure gradlew is executable
RUN chmod +x gradlew

# Copy build configuration
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Build the application (skip tests for faster build)
RUN ./gradlew bootJar -x test --no-daemon

# List all JARs to see what was created
RUN echo "=== JAR files in build/libs ===" && \
    ls -lh /app/build/libs/ && \
    echo "=== Finding boot JAR ===" && \
    JAR_FILE=$(find /app/build/libs -name "*.jar" ! -name "*-plain.jar" | head -1) && \
    echo "Found JAR: $JAR_FILE" && \
    cp "$JAR_FILE" /app/app.jar && \
    echo "=== Copied JAR ===" && \
    ls -lh /app/app.jar

# Stage 2: Create the final Docker image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create a non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the JAR from the build stage
COPY --from=build /app/app.jar app.jar

# Verify JAR exists and show info (as root)
RUN ls -lh /app/app.jar && \
    echo "JAR file size: $(du -h /app/app.jar | cut -f1)" && \
    test -f /app/app.jar || (echo "ERROR: app.jar not found!" && ls -la /app/ && exit 1)

# Change ownership to spring user
RUN chown spring:spring app.jar

# Verify file is readable
RUN test -r /app/app.jar || (echo "ERROR: app.jar not readable!" && exit 1)

# Switch to non-root user
USER spring:spring

# Verify JAR is accessible as spring user
RUN test -f /app/app.jar && test -r /app/app.jar || (echo "ERROR: app.jar not accessible!" && ls -la /app/ && exit 1)

# Expose the application port
EXPOSE 8080

# Health check using wget (usually available) or netcat
# Alternative: Use Spring Boot Actuator health endpoint if available
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

# Run the application (use absolute path to be sure)
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
