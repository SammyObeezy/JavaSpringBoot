# STAGE 1: Build the JAR
# Reverted to JDK 21 (LTS) to ensure compatibility with Spring Boot 3.2.3 Maven Plugin
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Clean and package, skipping tests to speed up build
RUN mvn clean package -DskipTests

# STAGE 2: Run the App (Minimal Image)
# Reverted to JRE 21 (LTS)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Create a non-root user for security
RUN addgroup -S escrow && adduser -S escrow -G escrow
USER escrow

# Expose the HTTPS port
EXPOSE 8443

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]