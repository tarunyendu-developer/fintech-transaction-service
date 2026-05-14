# Financial Transaction Processing System

## Project Overview

The Financial Transaction Processing System is an enterprise-level backend banking application developed using Java and Spring Boot. The project simulates real-world financial operations such as account creation, money transfers, debit, credit, transaction reversal, fraud detection, idempotency handling, transaction history tracking, filtering, pagination, metrics generation, and audit logging.

This project was designed to demonstrate strong backend engineering concepts, clean REST API development, transaction management, exception handling, and enterprise-level architecture.

---

# Features

## Account Management

* Create bank accounts
* Generate unique account numbers
* Fetch account details using account number
* Maintain account balance and status

---

## Money Transfer System

* Transfer money between accounts
* Validate sender and receiver accounts
* Balance validation before transfer
* Atomic transaction processing using `@Transactional`
* Prevent duplicate transactions using Idempotency Key

---

## Debit Operations

* Withdraw money from account
* Balance validation
* Transaction tracking

---

## Credit Operations

* Deposit money into account
* Update balances securely
* Transaction tracking

---

## Fraud Detection

* Detect suspicious high-value transactions
* Block transactions exceeding fraud threshold
* Custom fraud exception handling

---

## Transaction Reversal

* Reverse successful transfer transactions
* Restore balances automatically
* Update transaction status to `REVERSED`

---

## Transaction History

* Fetch complete transaction history
* Fetch transactions by account number
* Fetch transaction by transaction ID
* Filter transactions by type and status
* Pagination and sorting support

---

## Metrics & Analytics

* Total transactions count
* Successful transactions count
* Failed transactions count
* Debit transaction count
* Credit transaction count
* Transfer transaction count

---

## Exception Handling

* Global exception handling using `@RestControllerAdvice`
* Custom exception responses
* Proper HTTP status codes

---

## Enterprise Features

* Layered architecture
* DTO-based request/response handling
* Service abstraction
* Audit logging
* Retry mechanism using Spring Retry
* Clean REST APIs
* Unit testing with JUnit and Mockito

---

# Tech Stack

| Technology      | Description           |
| --------------- | --------------------- |
| Java 21         | Programming Language  |
| Spring Boot     | Backend Framework     |
| Spring Data JPA | ORM Framework         |
| Hibernate       | ORM Provider          |
| MySQL           | Relational Database   |
| Maven           | Build Tool            |
| JUnit 5         | Unit Testing          |
| Mockito         | Mock Testing          |
| Lombok          | Boilerplate Reduction |
| Swagger/OpenAPI | API Documentation     |
| Spring Retry    | Retry Mechanism       |

---

# Project Architecture

The application follows a layered architecture:

```text
Controller Layer
        ↓
Service Layer
        ↓
Repository Layer
        ↓
Database Layer
```

---

# Entity Relationship Overview

## Account Entity

Contains:

* accountId
* accountHolderName
* accountNumber
* balance
* status
* createdAt
* version

---

## Transaction Entity

Contains:

* transactionId
* fromAccount
* toAccount
* amount
* type
* status
* idempotencyKey
* createdAt

---

## Audit Log Entity

Contains:

* auditId
* transaction
* eventType
* message
* createdAt

---

# REST API Endpoints

## Account APIs

### Create Account

```http
POST /accounts
```

### Get Account By Account Number

```http
GET /accounts/{accountNumber}
```

---

## Transaction APIs

### Transfer Money

```http
POST /transactions/transfer
```

### Debit Money

```http
POST /transactions/debit
```

### Credit Money

```http
POST /transactions/credit
```

### Reverse Transaction

```http
POST /transactions/reverse
```

### Get Transaction By ID

```http
GET /transactions/id/{transactionId}
```

### Get Transactions By Account Number

```http
GET /transactions/account/{accountNumber}
```

### Get Transaction History

```http
GET /transactions?page=0&size=5&sortBy=createdAt
```

### Filter Transactions

```http
GET /transactions/filter?type=TRANSFER&status=SUCCESS&page=0&size=5
```

### Get Metrics

```http
GET /transactions/metrics
```

---

# Sample API Requests

## Create Account

```json
{
  "accountHolderName": "Tarun",
  "balance": 10000
}
```

---

## Transfer Money

```json
{
  "fromAccountNumber": "ACC1112260676",
  "toAccountNumber": "ACC1348016654",
  "amount": 1000,
  "idempotencyKey": "TXN-1001"
}
```

---

## Debit Money

```json
{
  "accountNumber": "ACC1112260676",
  "amount": 500,
  "idempotencyKey": "TXN-2001"
}
```

---

## Credit Money

```json
{
  "accountNumber": "ACC1112260676",
  "amount": 2000,
  "idempotencyKey": "TXN-3001"
}
```

---

## Reverse Transaction

```json
{
  "transactionId": "f76bda2c-849d-472a-a91a-34a56af9c500"
}
```

---

# Idempotency Handling

The system prevents duplicate financial transactions using an idempotency key.

If the same request is sent multiple times with the same idempotency key:

* Only the first request is processed
* Subsequent requests return the existing transaction
* Prevents duplicate money transfers

---

# Fraud Detection Logic

The system validates transaction amount before processing.

Transactions above the configured threshold are blocked automatically.

Example:

```text
Amount > 50000 → Fraud Detected
```

---

# Transaction Reversal Logic

The system supports transaction reversal for successful transfer transactions.

During reversal:

* Money is transferred back to sender
* Receiver balance is reduced
* Original transaction status becomes `REVERSED`

---

# Pagination and Filtering

The system supports:

* Pagination
* Sorting
* Transaction filtering

Examples:

```http
GET /transactions?page=0&size=5
```

```http
GET /transactions/filter?type=TRANSFER&status=SUCCESS
```

---

# Swagger Documentation

Swagger UI is integrated for API testing.

Access Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

---

# Unit Testing

The project includes unit testing using:

* JUnit 5
* Mockito

Test coverage includes:

* Successful transfer
* Insufficient balance
* Fraud detection
* Debit operations
* Credit operations
* Idempotency validation
* Exception handling

---

# Database Configuration

## MySQL Configuration

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/financial_transaction_db
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

# How To Run The Project

## Step 1

Clone repository:

```bash
git clone https://github.com/tarunyendu-developer/financial-transaction-processing-system.git
```

---

## Step 2

Open project in IntelliJ IDEA

---

## Step 3

Create MySQL database:

```sql
CREATE DATABASE financial_transaction_db;
```

---

## Step 4

Update database credentials in `application.properties`

---

## Step 5

Run application:

```bash
mvn spring-boot:run
```

---

## Step 6

Access Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

---

# Future Enhancements

* JWT Authentication
* Role-Based Authorization
* Redis Caching
* Kafka Event Streaming
* Docker Containerization
* Microservices Architecture
* Notification System
* Email Alerts
* Real-Time Monitoring Dashboard
* CI/CD Pipeline

---

# Key Concepts Implemented

* REST API Development
* Transaction Management
* Exception Handling
* DTO Pattern
* Layered Architecture
* Idempotency
* Fraud Detection
* Pagination
* Filtering
* Unit Testing
* Retry Mechanism
* Audit Logging
* Enterprise Backend Design

---

# Author

## Tarun Yendu

Full Stack Developer

Skills:

* Java
* Spring Boot
* Hibernate
* SQL
* React
* REST APIs
* Full Stack Development

---

# Conclusion

This project demonstrates enterprise-level backend development using Spring Boot and Java. It covers real-world fintech concepts such as secure transaction handling, idempotency, fraud validation, transaction reversal, and analytics.

The application was built with focus on clean architecture, scalability, maintainability, and professional backend engineering practices.
