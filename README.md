# Wallet Platform

A multi-service backend platform for managing digital wallets, built with **Spring Boot 3**, **PostgreSQL**, **Redis**, and **Kafka**.

---

## Architecture

```
wallet-platform/
├── wallet-api/            # Core REST API (port 8080)
└── notification-service/  # Kafka consumer service (port 8081)
```

### Tech Stack

| Layer         | Technology                          |
|---------------|-------------------------------------|
| Framework     | Spring Boot 3                       |
| Database      | PostgreSQL 17 + Flyway migrations   |
| Cache         | Redis 8 (TTL: 24h)                  |
| Messaging     | Apache Kafka 7.5 (Confluent)        |
| Security      | JWT (Access + Refresh tokens)       |
| Build         | Maven                               |
| Documentation | SpringDoc OpenAPI (Swagger UI)      |

---

## Getting Started

### 1. Start Infrastructure

```bash
docker compose up -d
```

Starts: **PostgreSQL** (5432), **Redis** (6379), **Zookeeper** (2181), **Kafka** (9092).

### 2. Run Wallet API

```bash
cd wallet-api
./mvnw spring-boot:run
```

Base URL: `http://localhost:8080/api/v1`
Swagger UI: [http://localhost:8080/api/v1/swagger-ui/index.html](http://localhost:8080/api/v1/swagger-ui/index.html)

### 3. Run Notification Service

```bash
cd notification-service
./mvnw spring-boot:run
```

Listens on port `8081`, consumes Kafka topic `transactions`.

---

## API Endpoints

### Auth — `/auth`

| Method | Path             | Auth | Description                           |
|--------|------------------|------|---------------------------------------|
| POST   | `/auth/register` | ❌   | Register a new user                   |
| POST   | `/auth/login`    | ❌   | Login and receive access/refresh JWT  |
| POST   | `/auth/refresh`  | ❌   | Refresh access token                  |

### Users — `/users`

| Method | Path        | Auth | Description               |
|--------|-------------|------|---------------------------|
| GET    | `/users/me` | ✅   | Get current user profile  |
| PUT    | `/users/me` | ✅   | Update current user info  |

### Accounts — `/accounts`

| Method | Path                               | Auth | Description                                          |
|--------|------------------------------------|------|------------------------------------------------------|
| POST   | `/accounts`                        | ✅   | Create a new account (specify currency)              |
| GET    | `/accounts`                        | ✅   | List all accounts of current user                    |
| GET    | `/accounts/{accountId}`            | ✅   | Get account details (result cached in Redis)         |
| GET    | `/accounts/{accountId}/statistics` | ✅   | Get account stats (total deposits/withdrawals/transfers) |

### Transactions

| Method | Path                                        | Auth | Description                                    |
|--------|---------------------------------------------|------|------------------------------------------------|
| POST   | `/accounts/{accountId}/deposit`             | ✅   | Deposit funds to account                       |
| POST   | `/accounts/{accountId}/withdraw`            | ✅   | Withdraw funds from account                    |
| POST   | `/transfer`                                 | ✅   | Transfer funds between accounts                |
| GET    | `/transactions/{transactionId}`             | ✅   | Get single transaction                         |
| GET    | `/accounts/{accountId}/transactions`        | ✅   | List transactions (paginated, default 20/page) |
| GET    | `/accounts/{accountId}/transactions/export` | ✅   | Export all transactions as `.csv` file         |

### Admin — `/admin` (ROLE_ADMIN only)

| Method | Path                                  | Description                                      |
|--------|---------------------------------------|--------------------------------------------------|
| GET    | `/admin/users`                        | List all users (paginated)                       |
| GET    | `/admin/users/{userId}`               | Get user by ID                                   |
| PUT    | `/admin/users/{userId}`               | Update any user                                  |
| GET    | `/admin/accounts`                     | List all accounts (paginated)                    |
| PATCH  | `/admin/accounts/{accountId}/block`   | Block an account                                 |
| PATCH  | `/admin/accounts/{accountId}/unblock` | Unblock an account                               |
| GET    | `/admin/transactions`                 | List all transactions (paginated)                |
| GET    | `/admin/statistics/top-users`         | Top users by volume (currency, date range, limit)|

> Default admin: **Email** `admin@example.com` | **Password** `admin` (seeded via Flyway V5)

---

## Security

- **JWT-based** stateless authentication — no sessions.
- All protected routes require `Authorization: Bearer <token>` header.
- Public routes: `/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`.
- Admin-only routes: `/admin/**` — requires `ROLE_ADMIN`.
- Passwords hashed with **BCrypt**.

---

## Caching

`GET /accounts/{accountId}` responses are cached in **Redis** with a TTL of **24 hours**.
Cache key pattern: `accounts::<accountId>`.

---

## Kafka Integration

After each transaction, a `TransactionEvent` is published to the `transactions` topic:

```json
{
  "transactionId": 1,
  "fromAccountId": 10,
  "toAccountId": 20,
  "amount": 500.00
}
```

The `notification-service` consumes these events and logs them.

---

## Running Tests

```bash
./mvnw test
```

Integration tests use **Testcontainers** — PostgreSQL and Kafka are spun up automatically, no local setup required.

### Test Coverage

| Test Class                  | Scenarios                                                                              |
|-----------------------------|----------------------------------------------------------------------------------------|
| `TransactionControllerTest` | Withdraw success, 401 on missing token, 400 on invalid amount, CSV export format       |

**Scenarios in detail:**

- **`shouldWithdrawMoney`** — withdraws 100 from a 1000-balance account, asserts balance becomes 900 and exactly 1 transaction is saved.
- **`shouldReturn401WhenTokenIsMissing`** — no auth header → 401, balance unchanged, no transactions created.
- **`shouldReturn400WhenAmountIsInvalid`** — amount with more than 2 decimal places (e.g. `100.9999`) → 400 Bad Request.
- **`shouldReturnExportFile`** — verifies CSV content and headers (`Content-Disposition: attachment; filename=transactions.csv`, `Content-Type: text/csv`).

CSV format:
```
id,fromAccountId,toAccountId,amount,status,type,createdAt
1,10,null,100,COMPLETED,WITHDRAW,2026-06-28T12:00:00Z
```

---

## Configuration

Key settings in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wallet_api
    username: wallet
    password: wallet

  data.redis:
    host: localhost
    port: 6379

  cache.redis:
    time-to-live: 24h

  kafka:
    bootstrap-servers: localhost:9092

server:
  servlet:
    context-path: /api/v1
```
