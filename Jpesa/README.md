J-Pesa (Java Pesa) Backend System

J-Pesa is a robust, lightweight, and high-performance backend system for a mobile money application (similar to M-Pesa).

It is built using Pure Java (JDK 22) without heavy frameworks like Spring Boot. Instead, it creates a custom architecture from the ground up using Undertow for the web server, Jackson for JSON processing, and raw JDBC for database interactions.

This project demonstrates core backend engineering concepts: ACID Transactions, Connection Pooling, JWT Authentication, Database Normalization, and Unit Testing.

Architecture

The application follows the classic Controller-Service-Repository layered architecture:

Controller Layer (io.undertow): Handles HTTP requests, JSON parsing, and authentication middleware.

Service Layer (Business Logic): Manages complex logic like calculating transaction fees, verifying OTPs, and enforcing ACID compliance for money transfers.

Repository Layer (JDBC): Direct interaction with PostgreSQL using PreparedStatements to prevent SQL injection.

Model Layer (POJOs): Represents database entities (Users, Wallets, Transactions).

Tech Stack

Language: Java 22

Server: Undertow (Non-blocking, lightweight web server)

Database: PostgreSQL

JSON Processing: Jackson

Connection Pool: HikariCP

Security: BCrypt (Password Hashing) & JJWT (JSON Web Tokens)

Testing: JUnit 5 & Mockito

⚡ Features

1. Authentication & Security

Registration: Automatically creates a User profile and an empty Wallet.

Secure Login:

Verifies Phone & Password (BCrypt hashed).

Generates a 6-digit OTP (Simulated SMS).

Verifies OTP and issues a JWT Token.

Middleware: Protects transaction endpoints (/api/txn/*) by verifying the JWT header.

Password Reset: Complete flow (Initiate -> OTP -> Reset).

2. Financial Transactions (ACID Compliant)

Deposits: Load money into a wallet.

Airtime Purchase: Deduct balance to buy airtime.

P2P Transfers (Send Money):

Atomicity: Uses connection.setAutoCommit(false) to ensure money is deducted from sender and added to recipient in a single atomic operation.

Revenue Model: Automatically deducts a Transaction Fee (e.g., 5 KES) and routes it to a System Revenue Wallet (000000).

Mini Statement: Fetches the last 10 transactions in reverse chronological order.

3. Data Integrity

Foreign Keys: Ensures transactions cannot exist without wallets.

Unique Constraints: Prevents duplicate phone numbers or emails.

Audit Logs: Tracks system actions.

Database Setup

The system requires a PostgreSQL database named jpesa_db.

Schema Overview

Users: Stores identity (Phone, Email, Password Hash).

Wallets: Stores Balance and Currency (One-to-One with User).

Transactions: Stores the ledger of every movement (Deposit, Transfer, Fee).

OTP_Codes: Stores temporary login codes.

Critical Initial Data

For the Transaction Fee logic to work, a System Revenue Account must exist. Run this SQL after creating tables:

-- Create the "Tax Collector" User (Phone MUST be 000000)
INSERT INTO users (full_name, phone_number, email, password_hash, status)
VALUES ('J-Pesa Revenue', '000000', 'revenue@jpesa.com', 'ADMIN_PASS', 'ACTIVE');

-- Create the Wallet for this user
INSERT INTO wallets (user_id, balance)
VALUES (
(SELECT user_id FROM users WHERE phone_number = '000000'),
0.00
);


🚀 How to Run

Prerequisites

Java JDK 17+ (Project targets JDK 22)

Maven

PostgreSQL installed and running

Configuration

Update src/main/resources/application.properties:

db.url=jdbc:postgresql://localhost:5432/jpesa_db
db.username=your_db_user
db.password=your_db_pass
jwt.secret=YourSuperSecretKeyHere...
jwt.expiration=86400000


Build & Start

Build the project:

mvn clean package


Run the application:

# Run via Maven
mvn exec:java -Dexec.mainClass="org.example.JpesaApp"

# OR Run the Jar (after shade plugin build)
java -jar target/Jpesa-1.0-SNAPSHOT.jar


The server will start at http://0.0.0.0:8080.

🧪 API Endpoints

Method

Endpoint

Auth Required

Description

POST

/api/auth/register

No

Register a new user

POST

/api/auth/login

No

Login (Triggers OTP)

POST

/api/auth/verify-otp

No

Verify OTP & Get Token

POST

/api/txn/deposit

Yes

Deposit money

POST

/api/txn/airtime

Yes

Buy Airtime

POST

/api/txn/send

Yes

Send money to another user

GET

/api/txn/ministatement

Yes

Get transaction history

Testing

The project includes Unit Tests using JUnit 5 and Mockito.
Tests focus on the Service layer logic, mocking the database interactions to ensure business rules (fees, negative balances, user existence) work correctly.

Run tests via:

mvn test
