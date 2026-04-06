<div align="center">

# Finance Dashboard Backend

### A production-ready REST API for financial data management
### Built with Spring Boot 3 · PostgreSQL · JWT · Role-Based Access Control

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-yellow)
![Maven](https://img.shields.io/badge/Build-Maven-red)

</div>

### Live API Documentation
- **Swagger UI**: http://65.0.74.15:8080/swagger-ui/index.html#/

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Getting Started](#getting-started)
- [Default Credentials](#default-credentials)
- [Authentication & JWT](#authentication--jwt)
- [Role-Based Access Control](#role-based-access-control)
- [API Reference](#api-reference)
    - [Auth API](#auth-api)
    - [Transaction API](#transaction-api)
    - [Dashboard API](#dashboard-api)
    - [User API](#user-api)
    - [Category API](#category-api)
- [Request & Response Format](#request--response-format)
- [Validation & Error Handling](#validation--error-handling)
- [Unit Testing](#unit-testing)
- [AWS Production Deployment](#aws-production-deployment)
- [Swagger UI](#swagger-ui)
- [Assumptions & Design Decisions](#assumptions--design-decisions)

---

## Overview

Finance Dashboard Backend is a fully structured, secure, and maintainable REST API backend built for managing personal and organizational financial data. It supports multi-role user access, comprehensive financial record management with filtering, and real-time dashboard analytics including income, expense, and investment summaries.

The system is built following clean architecture principles with strict separation between controllers, services, repositories, and domain models. Every API response follows a consistent envelope format, every endpoint is protected by JWT, and all business rules are enforced at the service layer.

---

## Key Features

### Financial Records Management
- Create, read, update, and soft-delete financial transactions
- Three transaction types: **INCOME**, **EXPENSE**, and **INVESTMENT**
- Optional category tagging per transaction
- Soft delete — records are never permanently removed, preserving audit history
- Full-text notes support up to 500 characters per transaction

### Advanced Filtering
- Filter transactions by **type** (INCOME / EXPENSE / INVESTMENT)
- Filter by **category**
- Filter by **date range** (from and to independently or combined)
- Combine multiple filters simultaneously using AND logic
- All results **sorted by date descending** (newest first)
- **Paginated responses** with configurable page size

### Dashboard Analytics
- Total income, total expenses, total investments
- Net balance (income − expenses − investments)
- Category-wise totals broken down by type
- Monthly trends for the last 6 months (all three types)
- 10 most recent transactions
- Custom date range analytics for Analyst and Admin roles

### User & Role Management
- Create, update, and deactivate users
- Three roles: VIEWER, ANALYST, ADMIN
- Role assignment and status management by Admin
- Active / Inactive user status with enforcement at every request

### Security
- JWT-based stateless authentication
- BCrypt password hashing
- Token validated on every request — inactive users are blocked even with a valid token
- Users deleted from database are rejected even if their token is still valid
- Method-level security using `@PreAuthorize`

### Data Quality
- Input validation on all request bodies using Jakarta Bean Validation
- Consistent error responses with exact field-level error messages
- Flyway-managed versioned database migrations
- JPA auditing for automatic `createdAt` and `updatedAt` timestamps

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.0 |
| Security | Spring Security 6 + JWT (JJWT 0.11.5) |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| Validation | Jakarta Bean Validation |
| Documentation | SpringDoc OpenAPI 3 (Swagger UI) |
| Boilerplate Reduction | Lombok |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito, MockMvc |
| Deployment | AWS EC2, RDS

---

## Project Structure

```
src/main/java/com/finance/
├── FinanceApplication.java
├── Configuration/
│   ├── SecurityConfig.java         # JWT filter chain, role rules, CORS
│   ├── AuditConfig.java            # Enables @CreatedDate / @LastModifiedDate
│   └── OpenApiConfig.java          # Swagger UI + Bearer token support
├── Entity/
│   ├── user/
│   │   ├── User.java               # Users table entity
│   │   ├── Role.java               # VIEWER | ANALYST | ADMIN
│   │   └── UserStatus.java         # ACTIVE | INACTIVE
│   └── transaction/
│       ├── Transaction.java         # Transactions table entity
│       ├── TransactionType.java     # INCOME | EXPENSE | INVESTMENT
│       └── Category.java           # Categories table entity
├── repository/
│   ├── UserRepository.java
│   ├── TransactionRepository.java   # Custom @Query methods for filtering & analytics
│   └── CategoryRepository.java
├── service/
│   ├── AuthService.java            # Login + JWT issuance
│   ├── UserService.java            # User lifecycle management
│   ├── TransactionService.java     # CRUD + soft delete
│   ├── DashboardService.java       # Aggregations and analytics
│   └── CategoryService.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── TransactionController.java
│   ├── DashboardController.java
│   └── CategoryController.java
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── CreateUserRequest.java
│   │   ├── UpdateUserRequest.java
│   │   ├── TransactionRequest.java
│   │   └── CategoryRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── UserResponse.java
│       ├── TransactionResponse.java
│       ├── DashboardSummary.java
│       ├── CategoryResponse.java
│       └── PagedResponse.java
├── security/
│   ├── JwtTokenProvider.java       # Token generation and validation
│   ├── JwtAuthFilter.java          # Per-request token check
│   ├── CustomUserDetails.java      # Spring Security user wrapper
│   └── CustomUserDetailsService.java
├── exception/
│   ├── GlobalExceptionHandler.java # Centralized error mapping
│   ├── ResourceNotFoundException.java
│   ├── AccessDeniedException.java
│   └── ValidationException.java
└── util/
    ├── ApiResponse.java            # Consistent response envelope
    ├── RoleGuard.java              # Programmatic role checks
    └── DateRangeUtil.java          # Date helper methods

src/main/resources/
├── application.yml
└── db/migration/
    ├── V1__create_users.sql
    ├── V2__create_categories.sql
    ├── V3__create_transactions.sql
    └── V4__seed_data.sql

src/test/java/com/finance/
├── service/
│   ├── TransactionServiceTest.java
│   ├── UserServiceTest.java
│   └── DashboardServiceTest.java
└── controller/
    └── AuthControllerTest.java
```

---

## Database Schema

```
┌──────────────────┐          ┌────────────────────────┐          ┌──────────────────┐
│      users       │          │      transactions      │          │    categories    │
├──────────────────┤          ├────────────────────────┤          ├──────────────────┤
│ id          PK   │◄─────────┤ id              PK     │─────────►│ id          PK   │
│ full_name        │          │ amount                 │          │ name        UQ   │
│ email       UQ   │          │ type                   │          │ description      │
│ password         │          │ category_id     FK     │          └──────────────────┘
│ role             │          │ date                   │
│ status           │          │ notes                  │
│ created_at       │          │ created_by      FK     │
│ updated_at       │          │ deleted                │
└──────────────────┘          │ created_at             │
                              │ updated_at             │
                              └────────────────────────┘
```

### Relationships
- **Users → Transactions**: One-to-many. A user can create many transactions. Each transaction stores who created it.
- **Categories → Transactions**: One-to-many. Category is optional on a transaction.
- **Soft Delete**: Transactions set `deleted = true` instead of being removed. All queries filter `WHERE deleted = false`.

---

## Getting Started

### Prerequisites

```bash
java -version     # Must be Java 17 or higher
mvn -version      # Must be Maven 3.8 or higher
psql --version    # PostgreSQL 13 or higher
```

### Step 1 — Create the Database

```sql
psql -U postgres
CREATE DATABASE finance_db;
\q
```

### Step 2 — Configure application.yml

Open `src/main/resources/application.yml` and verify:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/finance_db
    username: postgres
    password: postgres
```

Change `username` and `password` to match your PostgreSQL setup.

### Step 3 — Build and Run

```bash
# Download dependencies and compile
mvn clean install -DskipTests

# Start the application
mvn spring-boot:run
```

Flyway runs automatically on startup and creates all tables and seed data.

### Step 4 — Verify

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@finance.com","password":"password"}'
```

You should receive a JWT token in the response.

**Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

---

## Default Credentials

All three seed users are created by Flyway on first startup.

| Role | Email | Password |
|---|---|---|
| Admin | admin@finance.com | password |
| Analyst | analyst@finance.com | password |
| Viewer | viewer@finance.com | password |

> Change these passwords before deploying to any shared environment.

### Default Categories (10 seed categories)

`Salary`, `Freelance`, `Investment`, `Rent`, `Food`, `Transport`, `Utilities`, `Healthcare`, `Entertainment`, `Education`

---

## Authentication & JWT

### How It Works

```
1. Client sends POST /api/auth/login with email + password
2. Spring Security authenticates against the database
3. JwtTokenProvider generates a signed HS256 token
4. Token contains: email, role, userId, issuedAt, expiry
5. Client includes token in every request:
   Authorization: Bearer <token>
6. JwtAuthFilter intercepts every request:
   a. Extracts token from Authorization header
   b. Validates signature using JWT_SECRET
   c. Checks token not expired
   d. Loads user from database by email
   e. Checks user is still ACTIVE
   f. Sets authentication in SecurityContext
```

### Token Details

| Property | Value |
|---|---|
| Algorithm | HS256 |
| Expiry | 24 hours (86400000 ms) |
| Claims | email (sub), role, userId, iat, exp |

### Security Guards

The filter also protects against these edge cases:

| Scenario | Behavior |
|---|---|
| Valid token, user exists, ACTIVE | ✅ Authorized |
| Valid token, user deactivated after login | ❌ 401 — account inactive |
| Valid token, user deleted from database | ❌ 401 — user no longer exists |
| Expired token | ❌ 401 — unauthorized |
| Tampered token (wrong signature) | ❌ 401 — unauthorized |
| No token sent | ❌ 401 — unauthorized |

### Using the Token in Postman

1. `POST /api/auth/login` → copy the `token` value from the response
2. On every other request, go to the **Authorization** tab
3. Select type: **Bearer Token**
4. Paste the token (without quotes)

### Using the Token in Swagger UI

1. Open `http://localhost:8080/swagger-ui/index.html`
2. Login via `POST /api/auth/login` → copy the token
3. Click the **Authorize** button (top right)
4. Enter: `Bearer <paste-token-here>` (with the word Bearer and a space)
5. Click **Authorize** → all endpoints are now unlocked

---

## Role-Based Access Control

| Endpoint | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| Login | ✅ | ✅ | ✅ |
| View transactions (list + by ID) | ✅ | ✅ | ✅ |
| Create transaction | ❌ | ✅ | ✅ |
| Update transaction | ❌ | ✅ | ✅ |
| Delete transaction | ❌ | ✅ | ✅ |
| View dashboard summary | ✅ | ✅ | ✅ |
| Dashboard date-range summary | ❌ | ✅ | ✅ |
| List categories | ✅ | ✅ | ✅ |
| Create / update / delete category | ❌ | ❌ | ✅ |
| List / view users | ❌ | ❌ | ✅ |
| Create / update / deactivate user | ❌ | ❌ | ✅ |

---

## API Reference

### Auth API

---

#### POST `/api/auth/login`

Authenticate and receive a JWT token. This is the only public endpoint.

**Request Body:**
```json
{
  "email": "admin@finance.com",
  "password": "password"
}
```

**Validation:**
- `email` — required, must be valid email format
- `password` — required, must not be blank

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "email": "admin@finance.com",
    "fullName": "System Admin",
    "role": "ADMIN"
  },
  "timestamp": "2026-04-06T10:00:00"
}
```

**Error Responses:**

| HTTP | Scenario | Message |
|---|---|---|
| 400 | Missing or invalid email format | Validation failed |
| 401 | Wrong password | Invalid email or password |
| 401 | Account is INACTIVE | Your account is inactive. Contact admin. |

---

### Transaction API

All endpoints require authentication. Read endpoints are accessible by all roles. Write endpoints (create, update, delete) require ANALYST or ADMIN.

---

#### GET `/api/transactions`

List transactions with optional multi-parameter filtering. Results are always sorted by date descending (newest first) and paginated.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `type` | `INCOME` \| `EXPENSE` \| `INVESTMENT` | No | Filter by transaction type |
| `categoryId` | Long | No | Filter by category ID |
| `from` | `YYYY-MM-DD` | No | Start of date range (inclusive) |
| `to` | `YYYY-MM-DD` | No | End of date range (inclusive) |
| `page` | Integer | No | Page number, starts at 0 (default: 0) |
| `size` | Integer | No | Records per page (default: 20) |

**Filtering Examples:**

```
# All income transactions
GET /api/transactions?type=INCOME

# All expenses in a category
GET /api/transactions?type=EXPENSE&categoryId=5

# All transactions in January 2026
GET /api/transactions?from=2026-01-01&to=2026-01-31

# Income transactions in Q1 2026 with pagination
GET /api/transactions?type=INCOME&from=2026-01-01&to=2026-03-31&page=0&size=10

# All investment transactions sorted newest first
GET /api/transactions?type=INVESTMENT

# Transactions for a specific category across all time
GET /api/transactions?categoryId=3

# Combined: expense in food category in last month
GET /api/transactions?type=EXPENSE&categoryId=5&from=2026-03-01&to=2026-03-31
```

> All filter parameters are optional and use AND logic when combined. Omitting a parameter means no filter is applied for that field.

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 2,
        "amount": 10000.00,
        "type": "INCOME",
        "categoryName": "Freelance",
        "date": "2026-03-05",
        "notes": "Project payment",
        "createdByEmail": "analyst@finance.com",
        "createdAt": "2026-03-05T10:30:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  },
  "timestamp": "2026-04-06T10:00:00"
}
```

---

#### GET `/api/transactions/{id}`

Get a single transaction by its ID.

**Path Variable:** `id` — transaction ID (Long)

**Error Responses:**

| HTTP | Scenario |
|---|---|
| 404 | Transaction not found |
| 404 | Transaction exists but is soft-deleted |

---

#### POST `/api/transactions`

Create a new financial transaction. Requires ANALYST or ADMIN role.

**Request Body:**
```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "categoryId": 1,
  "date": "2026-04-01",
  "notes": "Monthly salary"
}
```

**Field Rules:**

| Field | Required | Rule |
|---|---|---|
| `amount` | ✅ | Must be greater than 0.01 |
| `type` | ✅ | Must be `INCOME`, `EXPENSE`, or `INVESTMENT` |
| `date` | ✅ | Cannot be a future date |
| `categoryId` | ❌ | Optional — must exist in categories table if provided |
| `notes` | ❌ | Optional — maximum 500 characters |

**Success Response `201`:**
```json
{
  "success": true,
  "message": "Transaction created",
  "data": {
    "id": 3,
    "amount": 5000.00,
    "type": "INCOME",
    "categoryName": "Salary",
    "date": "2026-04-01",
    "notes": "Monthly salary",
    "createdByEmail": "analyst@finance.com",
    "createdAt": "2026-04-06T10:00:00"
  },
  "timestamp": "2026-04-06T10:00:00"
}
```

---

#### PUT `/api/transactions/{id}`

Update an existing transaction. Requires ANALYST or ADMIN role.

All fields must be sent (full replacement). The `createdBy` and `createdAt` fields are never changed.

**Request Body:** Same as create.

**Error Responses:**

| HTTP | Scenario |
|---|---|
| 400 | Validation failure |
| 403 | VIEWER role attempting update |
| 404 | Transaction not found or soft-deleted |

---

#### DELETE `/api/transactions/{id}`

Soft-delete a transaction. Sets `deleted = true` in the database. The record is never physically removed. Requires ANALYST or ADMIN role.

**Success Response `200`:**
```json
{
  "success": true,
  "message": "Transaction deleted",
  "timestamp": "2026-04-06T10:00:00"
}
```

After deletion, `GET /api/transactions/{id}` on that ID returns 404.

---

### Dashboard API

---

#### GET `/api/dashboard/summary`

Full dashboard summary including all-time totals, category breakdowns, monthly trends, and recent transactions. Accessible by all roles.

**Response includes:**
- `totalIncome` — sum of all INCOME transactions
- `totalExpenses` — sum of all EXPENSE transactions
- `totalInvestments` — sum of all INVESTMENT transactions
- `netBalance` — `totalIncome − totalExpenses − totalInvestments`
- `categoryTotals` — per-category totals grouped by type, sorted by amount descending
- `monthlyTrends` — month-by-month totals for all types for the last 6 months
- `recentTransactions` — 10 most recent non-deleted transactions

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "totalIncome": 110000.00,
    "totalExpenses": 25000.00,
    "totalInvestments": 20000.00,
    "netBalance": 65000.00,
    "categoryTotals": [
      { "categoryName": "Salary",     "type": "INCOME",     "total": 100000.00 },
      { "categoryName": "Freelance",  "type": "INCOME",     "total": 10000.00  },
      { "categoryName": "Food",       "type": "EXPENSE",    "total": 15000.00  },
      { "categoryName": "Transport",  "type": "EXPENSE",    "total": 10000.00  },
      { "categoryName": "Investment", "type": "INVESTMENT", "total": 20000.00  }
    ],
    "monthlyTrends": [
      { "year": 2026, "month": 1, "type": "INCOME",     "total": 50000.00 },
      { "year": 2026, "month": 1, "type": "EXPENSE",    "total": 12000.00 },
      { "year": 2026, "month": 2, "type": "INCOME",     "total": 30000.00 },
      { "year": 2026, "month": 2, "type": "INVESTMENT", "total": 20000.00 }
    ],
    "recentTransactions": [ ... ]
  },
  "timestamp": "2026-04-06T10:00:00"
}
```

---

#### GET `/api/dashboard/summary/range`

Summary for a custom date range. Requires ANALYST or ADMIN role.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `from` | `YYYY-MM-DD` | ✅ | Start of date range |
| `to` | `YYYY-MM-DD` | ✅ | End of date range |

**Example:**
```
GET /api/dashboard/summary/range?from=2026-01-01&to=2026-03-31
```

Returns `totalIncome`, `totalExpenses`, `totalInvestments`, and `netBalance` for the given period.

---

### User API

All user endpoints require **ADMIN** role.

---

#### GET `/api/users`

Paginated list of all users with optional status filter.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `status` | `ACTIVE` \| `INACTIVE` | No | Filter by account status |
| `page` | Integer | No | Page number (default: 0) |
| `size` | Integer | No | Page size (default: 20) |

---

#### GET `/api/users/{id}`

Get a single user by ID.

---

#### POST `/api/users`

Create a new user account.

**Request Body:**
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "securePass123",
  "role": "ANALYST"
}
```

**Validation:**
- `fullName` — required, 2–100 characters
- `email` — required, valid email format, must be unique
- `password` — required, minimum 8 characters
- `role` — required, must be `VIEWER`, `ANALYST`, or `ADMIN`

**Error Response when email already exists `400`:**
```json
{
  "success": false,
  "message": "Email already in use: john@example.com"
}
```

---

#### PUT `/api/users/{id}`

Update a user's name, role, or status. All fields are optional.

**Request Body:**
```json
{
  "fullName": "John Updated",
  "role": "ADMIN",
  "status": "INACTIVE"
}
```

---

#### DELETE `/api/users/{id}`

Deactivate a user (sets `status = INACTIVE`). The user record is never deleted. Once deactivated, the user's JWT token is immediately rejected on the next request.

---

### Category API

---

#### GET `/api/categories`

List all categories. Accessible by all roles.

**Response:**
```json
{
  "success": true,
  "data": [
    { "id": 1, "name": "Salary",    "description": "Monthly or periodic salary income" },
    { "id": 5, "name": "Food",      "description": "Groceries and dining expenses" },
    { "id": 3, "name": "Investment","description": "Returns from investments and dividends" }
  ]
}
```

---

#### POST `/api/categories`

Create a new category. Requires ADMIN role.

**Request Body:**
```json
{
  "name": "Bonus",
  "description": "Performance and annual bonuses"
}
```

---

#### PUT `/api/categories/{id}`

Update a category. Requires ADMIN role.

---

#### DELETE `/api/categories/{id}`

Delete a category. Requires ADMIN role.

---

## Request & Response Format

### Every Response Uses This Envelope

```json
{
  "success": true | false,
  "message": "Optional message string",
  "data": { },
  "timestamp": "2026-04-06T10:00:00.123"
}
```

- `success` — always present. `true` for successful operations, `false` for errors
- `message` — present for operation confirmations (e.g. "Transaction created") and all errors
- `data` — present for successful responses that return data. Null for errors (except validation)
- `timestamp` — always present

### Paginated Response Shape

Endpoints that return lists use this shape inside `data`:

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3,
  "last": false
}
```

---

## Validation & Error Handling

### Validation Error Response

When `@Valid` fails on a request body, the response includes each field and its specific error:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "amount": "must not be null",
    "type": "must not be null",
    "date": "must not be null"
  },
  "timestamp": "2026-04-06T10:00:00"
}
```

### HTTP Status Code Reference

| Status | When it occurs |
|---|---|
| 200 | Successful GET, PUT, DELETE |
| 201 | Successful POST (resource created) |
| 400 | Validation failure, duplicate email, duplicate category name |
| 401 | Missing token, invalid token, expired token, wrong credentials, inactive account |
| 403 | Valid token but role not permitted for this action |
| 404 | Resource not found by ID, or soft-deleted transaction accessed |
| 500 | Unexpected server error (message included in response) |

### All Handled Exception Types

| Exception | HTTP | Message |
|---|---|---|
| `ResourceNotFoundException` | 404 | "Transaction not found with id: 5" |
| `ValidationException` | 400 | "Email already in use: x@y.com" |
| `AccessDeniedException` | 403 | "You do not have permission to perform this action" |
| `BadCredentialsException` | 401 | "Invalid email or password" |
| `DisabledException` | 401 | "Your account is inactive. Contact admin." |
| `MethodArgumentNotValidException` | 400 | Field-level validation errors map |
| `HttpMessageNotReadableException` | 400 | "Invalid JSON format" or "Invalid enum value" |
| `MethodArgumentTypeMismatchException` | 400 | "Invalid value 'abc' for parameter 'categoryId'. Expected type: Long" |
| `MissingServletRequestParameterException` | 400 | "Required parameter 'from' is missing" |

---

## Unit Testing

### Test Coverage

| Test Class | What It Tests |
|---|---|
| `TransactionServiceTest` | Create (INCOME, EXPENSE, INVESTMENT), optional category, get active, get deleted, update, soft delete |
| `UserServiceTest` | Create (all three roles), duplicate email, get by ID, update name/role/status, deactivate |
| `DashboardServiceTest` | Net balance calculation, all-zero scenario, negative balance, empty data, date range |
| `AuthControllerTest` | Login success (all three roles), blank email, invalid email format, wrong password, inactive account |

### Running Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=TransactionServiceTest

# Run a specific test method
mvn test -Dtest=TransactionServiceTest#create_shouldSave_forInvestment

# Run tests and see detailed output
mvn test -Dsurefire.useFile=false
```

### Test Strategy

**Unit tests** use Mockito to mock all dependencies. No database or Spring context is loaded — tests run in milliseconds.

```java
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock TransactionRepository transactionRepository;
    @Mock CategoryRepository    categoryRepository;
    @Mock UserRepository        userRepository;

    @InjectMocks TransactionService transactionService;
}
```

**Controller tests** use `@WebMvcTest` with `MockMvc`. Only the web layer is loaded — services are mocked.

```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean AuthService authService;
}
```

### Key Test Cases

```
TransactionServiceTest
  ✅ create_shouldSave_forIncome
  ✅ create_shouldSave_forExpense
  ✅ create_shouldSave_forInvestment          ← INVESTMENT type
  ✅ create_shouldSaveWithoutCategory
  ✅ create_shouldThrow_whenCategoryNotFound
  ✅ create_shouldThrow_whenUserNotFound
  ✅ getById_shouldReturn_whenActive
  ✅ getById_shouldThrow_whenDeleted
  ✅ getById_shouldThrow_whenNotFound
  ✅ update_shouldUpdateAllFields
  ✅ update_shouldThrow_whenDeleted
  ✅ delete_shouldMarkAsDeleted
  ✅ delete_shouldThrow_whenAlreadyDeleted
  ✅ delete_shouldThrow_whenNotFound

UserServiceTest
  ✅ createUser_shouldSucceed_withAnalystRole
  ✅ createUser_shouldSucceed_withViewerRole
  ✅ createUser_shouldSucceed_withAdminRole
  ✅ createUser_shouldEncodePassword
  ✅ createUser_shouldThrow_whenEmailExists
  ✅ getUserById_shouldReturn_whenExists
  ✅ getUserById_shouldThrow_whenNotFound
  ✅ updateUser_shouldUpdateFullName
  ✅ updateUser_shouldUpdateRole
  ✅ updateUser_shouldUpdateStatus
  ✅ deactivateUser_shouldSetStatusInactive
  ✅ deactivateUser_shouldThrow_whenNotFound

DashboardServiceTest
  ✅ getSummary_shouldReturnCorrectTotals
  ✅ getSummary_shouldCalculateNetBalance_withZeroExpenseAndInvestment
  ✅ getSummary_shouldReturnNegativeBalance_whenExpensesExceedIncome
  ✅ getSummary_shouldReturnEmptyLists_whenNoTransactions
  ✅ getSummary_shouldCallSumByType_forAllThreeTypes
  ✅ getSummaryForDateRange_shouldReturnCorrectTotals
  ✅ getSummaryForDateRange_shouldCallRepositoryWithCorrectDates

AuthControllerTest
  ✅ login_shouldReturn200_withTokenAndAdminRole
  ✅ login_shouldReturn200_withAnalystRole
  ✅ login_shouldReturn200_withViewerRole
  ✅ login_shouldReturn400_whenEmailIsBlank
  ✅ login_shouldReturn400_whenPasswordIsBlank
  ✅ login_shouldReturn400_whenEmailIsInvalidFormat
  ✅ login_shouldReturn400_whenBodyIsEmpty
  ✅ login_shouldReturn401_whenCredentialsAreWrong
  ✅ login_shouldReturn401_whenAccountIsInactive
```

---

## AWS Production Deployment

This project is successfully deployed and running on AWS cloud infrastructure using:
- **Amazon EC2** for application hosting
- **Amazon RDS PostgreSQL** for database services

### Live API Documentation
- **Swagger UI**: http://65.0.74.15:8080/swagger-ui/index.html#/

The production deployment demonstrates the application's readiness for real-world use with proper security, scalability, and monitoring.

---

## Swagger UI

The full interactive API documentation is available at:

```
http://localhost:8080/swagger-ui/index.html
```

All endpoints are documented with request/response schemas. To authorize in Swagger:

1. Call `POST /api/auth/login` via Swagger → copy the token value
2. Click **Authorize** (top right, lock icon)
3. Enter: `Bearer <paste-token-here>`
4. Click **Authorize** → all endpoints are now unlocked

OpenAPI JSON spec available at: 'http://localhost:8080/v3/api-docs'

---

## Assumptions & Design Decisions

| Decision | Reasoning |
|---|---|
| Soft delete for transactions | Preserves audit history. Deleted transactions are invisible to all APIs but remain in the database. |
| JWT is stateless | No server-side session storage. Token is self-contained. The trade-off is that revocation requires checking the database (done in JwtAuthFilter on every request for inactive status). |
| Flyway for migrations | Schema is fully versioned. `ddl-auto: validate` means Hibernate never modifies the schema — all changes go through SQL files. Never edit V1–V4 files after first run. |
| Category is optional | Transactions can exist without a category for quick entry. Category adds structure but should not block recording. |
| INVESTMENT as a separate type | Investments are distinct from expenses — they are capital allocation, not consumption. Treating them separately gives a cleaner net balance calculation. |
| Net balance formula | `income − expenses − investments`. Investments are treated as outflows since the money leaves your liquid balance. |
| Analyst can write transactions | Analysts need to record financial data. Only user management and category creation require Admin — these affect system structure, not data. |
| Password not returned in any response | `UserResponse` never includes the password field. `CustomUserDetails` exposes only what Spring Security needs. |
| All timestamps in LocalDateTime | Stored and returned as ISO 8601 without timezone. Assumes all users operate in the same timezone as the server. |

---

*Built By Akarsh Jain*
