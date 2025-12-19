# Spring Boot Microservices Payment System with M-Pesa Integration
## Table of Contents

- [Introduction](#introduction)
- [Architecture Overview](#architecture-overview)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Environment Configuration](#environment-configuration)
- [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
- [Testing the Flow](#testing-the-flow)
- [API Endpoints](#api-endpoints)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Introduction

This project is a **production-ready, event-driven microservices architecture** built with Spring Boot 3 that handles booking and payment processing through Safaricom's M-Pesa Daraja API. The system demonstrates best practices in microservices communication, asynchronous processing, API Gateway patterns, and real-time payment integration.

### What it Does:
- Routes all client requests through a centralized API Gateway
- Accepts booking requests from users via the API Gateway
- Processes payments asynchronously via M-Pesa STK Push
- Handles real-time payment callbacks from Safaricom
- Maintains transaction state with complete audit trails
- Provides robust error handling and secure credential management

## Architecture Overview

<img width="1024" height="559" alt="image" src="https://github.com/user-attachments/assets/9994d2a7-e4cc-4a21-81eb-1b041fadbfaa" />

### Flow Description:

1. **Request Entry**: User submits a request through the API Gateway (single entry point)
2. **Routing**: API Gateway routes the request to the appropriate service (Booking Service)
3. **Booking Creation**: Booking Service processes and saves the booking
4. **Event Publishing**: Booking Service publishes `PaymentInitiatedEvent` to RabbitMQ
5. **Event Consumption**: Payment Service consumes the event from the queue
6. **Payment Initiation**: Payment Service initiates M-Pesa STK Push and saves initial transaction
7. **User Payment**: Customer completes payment on their phone
8. **Callback Processing**: M-Pesa sends callback to Ngrok tunnel (port 8083), which forwards to Payment Service endpoint
9. **Status Update**: Payment Service updates transaction status in MySQL database

## Key Features

## Key Features

- **API Gateway Pattern**: Centralized entry point using Spring Cloud Gateway for routing and service discovery
- **Event-Driven Architecture**: Asynchronous microservices communication using RabbitMQ
- **Service Discovery**: Netflix Eureka for dynamic service registration and discovery
- **M-Pesa Integration**: Complete STK Push implementation with OAuth2 token management
- **Webhook Handling**: Robust asynchronous callback processing for payment confirmations
- **Database Transactions**: Reliable persistence with Spring Data JPA and MySQL
- **Security Best Practices**: Masked sensitive data logging and secure credential management
- **Error Handling**: Comprehensive exception handling and logging with SLF4J
- **DTO Mapping**: Clean separation of external API responses and internal models
- **Production Ready**: Configurable for different environments (dev, staging, production)

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 17+ |
| **Framework** | Spring Boot | 3.x |
| **API Gateway** | Spring Cloud Gateway | 4.x |
| **Web** | Spring Web | 3.x |
| **Data Access** | Spring Data JPA | 3.x |
| **Messaging** | Spring AMQP (RabbitMQ) | 3.x |
| **Database** | MySQL | 8.x |
| **Service Discovery** | Netflix Eureka | Latest |
| **External API** | Safaricom Daraja API | Sandbox |
| **Build Tool** | Maven/Gradle | Latest |
| **Utilities** | Lombok | Latest |
| **Testing Tools** | Ngrok (for callbacks) | Latest |
| **Containerization** | Docker (Optional) | Latest |

## Prerequisites

Before running this application, ensure you have the following installed:

### Required Software:
- **Java Development Kit (JDK)**: Version 17 or higher
  ```bash
  java -version  # Should show 17+
  ```

- **MySQL Server**: Version 8.x
  ```bash
  mysql --version
  ```

- **RabbitMQ**: Latest version (or use Docker)
  ```bash
  rabbitmq-server --version
  ```

- **Maven or Gradle**: For building the project
  ```bash
  mvn -version  # or gradle -version
  ```

### Optional but Recommended:
- **Docker & Docker Compose**: For containerized setup
- **Postman or cURL**: For API testing
- **Ngrok**: For exposing local callback endpoint during development
  ```bash
  ngrok http 8082
  ```

### M-Pesa Developer Account:
- Sign up at [Safaricom Daraja Portal](https://developer.safaricom.co.ke/)
- Create a Sandbox App to get your credentials:
  - Consumer Key
  - Consumer Secret
  - Passkey (for STK Push)

## Project Structure

```
spring-boot-mpesa-payment-system/
├── service-registry/                # Eureka Server
│   ├── src/main/java/
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── api-gateway/                     # Spring Cloud Gateway
│   ├── src/main/java/
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── booking-service/                 # Producer Service
│   ├── src/main/java/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── model/
│   │   ├── event/
│   │   └── config/
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── payment-service/                 # Consumer Service
│   ├── src/main/java/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── dto/
│   │   ├── listener/
│   │   └── config/
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── docker-compose.yml               # Optional Docker setup
└── README.md
```

## Environment Configuration

### 1. Service Registry (Eureka Server)

**File**: `service-registry/src/main/resources/application.properties`

```properties
# Server Configuration
spring.application.name=service-registry
server.port=8761

# Eureka Configuration
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

### 2. API Gateway Configuration

**File**: `api-gateway/src/main/resources/application.properties`

```properties
# Application Name
spring.application.name=api-gateway
server.port=8080

# Eureka Client Configuration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true

# Gateway Route Configuration
# Route for Booking Service
spring.cloud.gateway.routes[0].id=booking-service
spring.cloud.gateway.routes[0].uri=lb://booking-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/bookings/**

# Route for Payment Service
spring.cloud.gateway.routes[1].id=payment-service
spring.cloud.gateway.routes[1].uri=lb://payment-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/payments/**

# Discovery Locator (Optional - enables automatic route creation)
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lower-case-service-id=true
```

### 3. Booking Service Configuration

**File**: `booking-service/src/main/resources/application.properties`

```properties
# Application Name
spring.application.name=booking-service
server.port=8081

# Eureka Client Configuration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true

# RabbitMQ Configuration
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# RabbitMQ Queue and Exchange
rabbitmq.exchange.name=payment.exchange
rabbitmq.queue.name=payment.queue
rabbitmq.routing.key=payment.routing.key
```

### 4. Payment Service Configuration

**File**: `payment-service/src/main/resources/application.properties`

```properties
# Application Name
spring.application.name=payment-service
server.port=8082

# Eureka Client Configuration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true

# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3307/payment_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true

# RabbitMQ Configuration
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# RabbitMQ Queue and Exchange
rabbitmq.exchange.name=payment.exchange
rabbitmq.queue.name=payment.queue
rabbitmq.routing.key=payment.routing.key

# M-Pesa Daraja API Configuration (SANDBOX)
mpesa.daraja.consumer-key=YOUR_CONSUMER_KEY_HERE
mpesa.daraja.consumer-secret=YOUR_CONSUMER_SECRET_HERE
mpesa.daraja.oauth-url=https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials
mpesa.daraja.stk-push-url=https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest

# M-Pesa Business Details
mpesa.business.shortcode=174379
mpesa.business.passkey=YOUR_PASSKEY_HERE
mpesa.transaction.type=CustomerPayBillOnline

# Callback URL (Use Ngrok URL for local testing - Ngrok listens on port 8083)
mpesa.callback.url=https://your-ngrok-url.ngrok-free.app/api/payments/callback/mpesa
```

### 5. Creating MySQL Database

```sql
CREATE DATABASE IF NOT EXISTS payment_db;
USE payment_db;

-- Tables will be auto-created by Hibernate
-- But you can verify with:
SHOW TABLES;
```

## Installation & Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/yourusername/spring-boot-mpesa-payment-system.git
cd spring-boot-mpesa-payment-system
```

### Step 2: Start Required Infrastructure

#### Option A: Using Docker Compose (Recommended)

```bash
docker-compose up -d
```

This will start:
- MySQL (port 3307)
- RabbitMQ (port 5672, Management UI: 15672)

#### Option B: Manual Setup

**Start MySQL:**
```bash
# Using Docker
docker run -d \
  --name mysql-payment \
  -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=yourpassword \
  -e MYSQL_DATABASE=payment_db \
  mysql:8

# Or use your local MySQL installation
```

**Start RabbitMQ:**
```bash
# Using Docker
docker run -d \
  --name rabbitmq-payment \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management

# Or use your local RabbitMQ installation
```

### Step 3: Configure M-Pesa Credentials

1. Sign up at [Safaricom Daraja Portal](https://developer.safaricom.co.ke/)
2. Create a Sandbox App
3. Copy your credentials into `payment-service/src/main/resources/application.properties`

### Step 4: Setup Ngrok for Callback Testing

```bash
# Install Ngrok and start tunnel on port 8083
ngrok http 8083

# Copy the HTTPS URL (e.g., https://abc123.ngrok-free.app)
# Update mpesa.callback.url in payment-service application.properties
```

### Step 5: Build All Services

```bash
# Build Service Registry
cd service-registry
mvn clean install
cd ..

# Build API Gateway
cd api-gateway
mvn clean install
cd ..

# Build Booking Service
cd booking-service
mvn clean install
cd ..

# Build Payment Service
cd payment-service
mvn clean install
cd ..
```

## Running the Application

**IMPORTANT**: Services must be started in this specific order:

### 1. Start Service Registry (Eureka Server)

```bash
cd service-registry
mvn spring-boot:run
```

Wait until you see: `Started Eureka Server on port 8761`

Verify at: http://localhost:8761

### 2. Start API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

Wait for: `Started API Gateway on port 8080`

### 3. Start Booking Service

```bash
cd booking-service
mvn spring-boot:run
```

Wait for: `Started BookingServiceApplication on port 8081`

### 4. Start Payment Service

```bash
cd payment-service
mvn spring-boot:run
```

Wait for: `Started PaymentServiceApplication on port 8082`

### Verification Checklist:
- All services registered in Eureka Dashboard (including api-gateway)
- RabbitMQ queue created (check at http://localhost:15672)
- MySQL database tables created
- Ngrok tunnel active on port 8083 and callback URL configured
- API Gateway routes configured and accessible

## Testing the Flow

### Step 1: Create a Booking (Initiates Payment)

**Note**: All requests now go through the API Gateway on port 8080.

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "John Doe",
    "customerPhone": "254712345678",
    "amount": 100.00,
    "description": "Event Booking Payment"
  }'
```

**Expected Response:**
```json
{
  "bookingId": "12345",
  "status": "PENDING",
  "message": "Booking created successfully. Payment initiated."
}
```

### Step 2: Check Logs

**API Gateway Logs:**
```
INFO: Routing request to booking-service
INFO: Request matched route: booking-service
```

**Booking Service Logs:**
```
INFO: Booking created with ID: 12345
INFO: Published PaymentInitiatedEvent to RabbitMQ
```

**Payment Service Logs:**
```
INFO: Received payment event for booking: 12345
INFO: Initiating M-Pesa STK Push...
INFO: STK Push successful. CheckoutRequestID: ws_CO_123456789
INFO: Transaction saved with status: PENDING
```

### Step 3: Complete Payment on Phone

The customer will receive an STK Push prompt on their phone (sandbox uses test credentials).

### Step 4: Verify Callback Processing

**Payment Service Logs:**
```
INFO: Received M-Pesa callback
INFO: Transaction status updated to: COMPLETED
```

### Step 5: Verify Database

```sql
USE payment_db;
SELECT * FROM transactions ORDER BY created_at DESC LIMIT 1;
```

**Expected Result:**
```
| id | booking_id | amount | phone_number   | status    | mpesa_receipt | checkout_request_id |
|----|------------|--------|----------------|-----------|---------------|---------------------|
| 1  | 12345      | 100.00 | 254712345678   | COMPLETED | QAX12BC34    | ws_CO_123456789     |
```

## API Endpoints

### API Gateway (Port 8080)

All external requests should go through the API Gateway:

| Method | Endpoint | Routed To | Description |
|--------|----------|-----------|-------------|
| POST | `/api/bookings` | Booking Service | Create new booking and initiate payment |
| GET | `/api/bookings/{id}` | Booking Service | Get booking details |
| POST | `/api/payments/callback/mpesa` | Payment Service | M-Pesa callback endpoint (called by Safaricom) |
| GET | `/api/payments/status/{checkoutRequestId}` | Payment Service | Check payment status |
| GET | `/api/payments/transaction/{transactionId}` | Payment Service | Get transaction details |

### Internal Service Ports (Not directly accessible from outside)

- **Booking Service**: Port 8081
- **Payment Service**: Port 8082

## Troubleshooting

### Common Issues and Solutions:

#### 1. **API Gateway Not Routing Requests**
```
Solution: Ensure API Gateway started after Eureka and all services are registered
Check: Eureka Dashboard at http://localhost:8761 - verify api-gateway is listed
Verify: Gateway routes in application.properties are correctly configured
Test: Direct service access (port 8081/8082) vs Gateway access (port 8080)
```

#### 2. **Services Not Registering with Eureka**
#### 2. **Services Not Registering with Eureka**
```
Solution: Ensure Eureka Server started first and is running on port 8761
Check: eureka.client.service-url.defaultZone in application.properties
```

#### 3. **RabbitMQ Connection Refused**
```
Solution: Verify RabbitMQ is running
Check: docker ps | grep rabbitmq
Start: docker start rabbitmq-payment
```

#### 4. **MySQL Connection Error**
```
Solution: Check MySQL is running on port 3307
Verify credentials in application.properties
Test connection: mysql -h localhost -P 3307 -u root -p
```

#### 5. **M-Pesa STK Push Fails**
#### 5. **M-Pesa STK Push Fails**
```
Common causes:
- Invalid credentials (check Consumer Key/Secret)
- Wrong Passkey
- Callback URL not accessible (Ngrok tunnel down on port 8083)
- Using production URL instead of sandbox

Solution: 
- Verify all M-Pesa credentials in application.properties
- Ensure Ngrok is running: ngrok http 8083
- Check M-Pesa error response in logs
```

#### 6. **Callback Not Received**
```
Solution:
- Verify Ngrok tunnel is active on port 8083
- Check callback URL matches Ngrok HTTPS URL
- Look for "ERR_NGROK_6024" errors (tunnels expire after 2 hours on free plan)
- Restart Ngrok and update callback URL if needed
```

#### 7. **Payment Status Stuck on PENDING**
```
Possible causes:
- Callback endpoint not reachable
- Database update failed
- Transaction ID mismatch

Solution:
- Check payment-service logs for errors
- Verify callback was received
- Check database transaction status
```

### Enable Debug Logging

Add to `application.properties`:
```properties
logging.level.root=INFO
logging.level.com.yourpackage=DEBUG
logging.level.org.springframework.amqp=DEBUG
```

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Safaricom Daraja API Documentation
- Spring Boot Community
- Netflix OSS (Eureka)
- RabbitMQ Team

## Support

If you encounter any issues or have questions:
- Open an issue on GitHub
- Check the [Troubleshooting](#troubleshooting) section
- Review Safaricom Daraja API [documentation](https://developer.safaricom.co.ke/docs)

---

**If you find this project helpful, please consider giving it a star!**

**Built with Spring Boot and M-Pesa Daraja API**
