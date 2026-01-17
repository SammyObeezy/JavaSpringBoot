# STAGE 1: Build the JAR
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Clean and package, skipping tests to speed up build
RUN mvn clean package -DskipTests

# STAGE 2: Run the App (Minimal Image)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/escrow-service-0.0.1-SNAPSHOT.jar app.jar

# Copy the SSL Keystore (Required for HTTPS/8443)
COPY src/main/resources/escrow-keystore.p12 ./escrow-keystore.p12

# Create a non-root user for security
RUN addgroup -S escrow && adduser -S escrow -G escrow
USER escrow

# Expose the HTTPS port
EXPOSE 8443

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]