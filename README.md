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

### Test Structure

```
src/test/
├── config/
│   ├── IntegrationTestBase.java        # Base class for integration tests (Kafka + PostgreSQL containers)
│   └── PostgresContainerTest.java      # Base class for @DataJpaTest with Testcontainers
├── controller/
│   └── TransactionControllerTest.java  # Full Spring MVC integration tests
├── service/
│   ├── AccountServiceTest.java         # Unit tests (Mockito)
│   ├── AuthServiceTest.java            # Unit tests (Mockito)
│   └── TransactionServiceTest.java     # Unit tests (Mockito)
├── repository/
│   ├── AccountRepositoryTest.java      # @DataJpaTest with real PostgreSQL
│   ├── RefreshTokenRepositoryTest.java # @DataJpaTest with real PostgreSQL
│   ├── TransactionRepositoryTest.java  # @DataJpaTest with real PostgreSQL
│   └── UserRepositoryTest.java        # @DataJpaTest with real PostgreSQL
└── util/
    └── TestDataFactory.java            # Shared test data builders
```

---

### Controller Tests — `TransactionControllerTest`

> Type: **Integration** (`@SpringBootTest` + `MockMvc` + Testcontainers)

| Test | Description |
|------|-------------|
| `shouldWithdrawMoney` | Withdraws 100 from a 1000-balance account — asserts balance becomes 900, 1 transaction saved |
| `shouldReturn401WhenTokenIsMissing` | No `Authorization` header → 401, balance unchanged, no transactions created |
| `shouldReturn400WhenAmountIsInvalid` | Amount with >2 decimal places (e.g. `100.9999`) → 400 Bad Request |
| `shouldReturnExportFile` | Verifies CSV format, `Content-Type: text/csv`, `Content-Disposition: attachment; filename=transactions.csv` |

CSV format:
```
id,fromAccountId,toAccountId,amount,status,type,createdAt
1,10,null,100,COMPLETED,WITHDRAW,2026-06-28T12:00:00Z
```

---

### Service Tests — `AccountServiceTest`

> Type: **Unit** (Mockito)

| Test | Description |
|------|-------------|
| `shouldCreateAccount` | Creates account with correct user, currency, and `createdAt` |
| `shouldThrowWhenUserNotFound` | `NoSuchElementException` when user not found — `accountRepository.save` never called |
| `shouldBlockAccount` | Sets `blockedAt`, saves account, response `isBlocked()` = true |
| `shouldThrowWhenAccountNotFoundOnBlock` | `NoSuchElementException` — `save` never called |
| `shouldUnblockAccount` | Clears `blockedAt`, saves account, response `isBlocked()` = false |
| `shouldThrowWhenAccountNotFoundOnUnblock` | `NoSuchElementException` — `save` never called |

---

### Service Tests — `AuthServiceTest`

> Type: **Unit** (Mockito)

| Test | Description |
|------|-------------|
| `shouldCreateUser` | Registers user with hashed password, `ROLE_USER`, saves to repository |
| `shouldThrowWhenEmailAlreadyExists` | `IllegalArgumentException` — `save` and `encode` never called |
| `shouldLoginUser` | Returns access + refresh tokens, saves `RefreshToken` with hash, expiry, no `revokedAt` |
| `shouldThrowWhenUserNotFound` | `BadCredentialsException` — no downstream calls |
| `shouldThrowWhenPasswordIncorrect` | `BadCredentialsException` — `refreshTokenRepository` and `jwtService` never called |
| `shouldRefreshToken` | Returns new access token for valid, non-revoked, non-expired refresh token |
| `shouldThrowWhenTokenNotFound` | `NoSuchElementException` — `jwtService` never called |
| `shouldThrowWhenRefreshTokenRevoked` | `BadCredentialsException("Refresh token revoked")` |
| `shouldThrowWhenRefreshTokenExpired` | `BadCredentialsException("Refresh token expired")` |

---

### Service Tests — `TransactionServiceTest`

> Type: **Unit** (Mockito)

**Get Transaction:**

| Test | Description |
|------|-------------|
| `shouldReturnTransaction` | Returns transaction if current user is owner of from/to account |
| `shouldThrowWhenTransactionNotBelongToCurrentUser` | `NoSuchElementException` when neither account belongs to user |
| `shouldThrowWhenTransactionNotFound` | `NoSuchElementException` on missing transaction |

**Deposit:**

| Test | Description |
|------|-------------|
| `shouldDepositMoney` | Balance increases by amount, transaction saved, Kafka event sent |
| `shouldCreateDepositTransaction` | `fromAccount` is null, `toAccount` set, status `COMPLETED`, type `DEPOSIT` |
| `shouldSendEventAfterSuccessfulCreationOfDepositTransaction` | Event fields match response (`transactionId`, `toAccountId`, `amount`), `fromAccountId` is null |
| `shouldThrowWhenAccountNotFoundOnDepositMoney` | `NoSuchElementException` — no save calls |
| `shouldThrowWhenAccountBlockedOnDepositMoney` | `AccountBlockedException` — no save calls |

**Withdraw:**

| Test | Description |
|------|-------------|
| `shouldWithdrawMoney` | Balance decreases by amount, transaction saved, Kafka event sent |
| `shouldCreateWithdrawTransaction` | `fromAccount` set, `toAccount` is null, status `COMPLETED`, type `WITHDRAW` |
| `shouldSendEventAfterSuccessfulCreationOfWithdrawTransaction` | Event fields match response, `toAccountId` is null |
| `shouldThrowWhenAccountNotFoundOnWithdrawMoney` | `NoSuchElementException` — no save calls |
| `shouldThrowWhenAccountBlockedOnWithdrawMoney` | `AccountBlockedException` — no save calls |
| `shouldThrowWhenWithdrawalAmountExceedsBalance` | `InsufficientBalanceException` — no save calls |

**Transfer:**

| Test | Description |
|------|-------------|
| `shouldTransferMoney` | fromAccount decreases, toAccount increases, both saved in order, event sent |
| `shouldCreateTransferTransaction` | Both accounts set, amount correct, status `COMPLETED`, type `TRANSFER` |
| `shouldSendEventAfterSuccessfulCreationOfTransferTransaction` | All event fields match response |
| `shouldThrowWhenSourceAccountNotFound` | `NoSuchElementException` — destination not even queried |
| `shouldThrowWhenDestinationAccountNotFound` | `NoSuchElementException` — no save calls |
| `shouldThrowWhenAccountBlockedOnTransferMoney` | `AccountBlockedException` when toAccount is blocked |
| `shouldThrowWhenWithdrawalAmountExceedsBalanceOnTransferMoney` | `InsufficientBalanceException` — no save calls |
| `shouldThrowWhenAccountCurrenciesDiffer` | `CurrencyMismatchException` — no save calls |
| `shouldThrowWhenSameAccountsProvided` | `InvalidAccountsException` — no repository calls at all |

---

### Repository Tests — `AccountRepositoryTest`

> Type: **Integration** (`@DataJpaTest` + Testcontainers)

| Test | Description |
|------|-------------|
| `shouldReturnAccountWhenIdAndUserIdExists` | `findByIdAndUserId` returns account for correct owner |
| `shouldReturnEmptyWhenIdAndUserIdDoesNotExist` | Returns `Optional.empty()` for missing pair |
| `shouldReturnAccountsWhenUserIdExists` | `findByUserId` returns all accounts (2) for user |
| `shouldReturnEmptyWhenUserIdDoesNotExist` | Returns empty list for unknown user |

---

### Repository Tests — `UserRepositoryTest`

> Type: **Integration** (`@DataJpaTest` + Testcontainers)

| Test | Description |
|------|-------------|
| `shouldReturnTrueWhenEmailExists` | `existsByEmail` returns true for saved user |
| `shouldReturnFalseWhenEmailDoesNotExists` | Returns false for unknown email |
| `shouldReturnUserWhenEmailExists` | `findByEmail` returns user with matching email |
| `shouldReturnEmptyWhenEmailDoesNotExist` | Returns `Optional.empty()` for unknown email |

---

### Repository Tests — `RefreshTokenRepositoryTest`

> Type: **Integration** (`@DataJpaTest` + Testcontainers)

| Test | Description |
|------|-------------|
| `shouldReturnRefreshTokenWhenTokenHashExists` | `findByTokenHash` returns token with correct user and hash |
| `shouldReturnEmptyWhenTokenHashDoesNotExist` | Returns `Optional.empty()` for unknown hash |

---

### Repository Tests — `TransactionRepositoryTest`

> Type: **Integration** (`@DataJpaTest` + Testcontainers)

| Test | Description |
|------|-------------|
| `shouldReturnOnlyFirstPageOfTransactionsByAccountId` | Pagination works: 5 total, page size 2 → 2 results, 3 pages |
| `shouldReturnTopUsersByCompletedTransactions` | Top users sorted by total transferred volume, filtered by currency/date range, excludes `FAILED` and out-of-range transactions |
| `shouldReturnTransactionStatisticsOfAccount` | Stats per account: total deposits `1100.00`, withdrawals `500.00`, transfers `300.00`, count `4` — excludes `FAILED` and other accounts |



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
