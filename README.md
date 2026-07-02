# Wallet Platform

This repository serves as a multi-service workspace for the wallet platform.

## Services

- **wallet-api**: Core API managing wallets, transactions, caching (Redis), database storage (PostgreSQL), and publishing transaction events to Kafka.
- **notification-service**: Microservice listening to Kafka `transactions` events and logging them.

## Getting Started

### 1. Start Shared Infrastructure
Run the central Docker Compose setup to spin up PostgreSQL, Redis, Zookeeper, and Kafka:
```bash
docker compose up -d
```

### 2. Run Wallet API
Navigate to the `wallet-api` directory and start the service:
```bash
cd wallet-api
./mvnw spring-boot:run
```
It runs on port `8080` (context path `/api/v1`). Swagger UI is accessible at:
[http://localhost:8080/api/v1/swagger-ui/index.html](http://localhost:8080/api/v1/swagger-ui/index.html)

### 3. Run Notification Service
Navigate to the `notification-service` directory and start the service:
```bash
cd notification-service
./mvnw spring-boot:run
```
It runs on port `8081` and listens for Kafka events on the `transactions` topic.
