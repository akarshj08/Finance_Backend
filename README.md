# Finance Dashboard Backend

A production-ready REST API backend for a finance dashboard system built with **Spring Boot 3** and **PostgreSQL**. Supports role-based access control, financial record management, and dashboard analytics.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Language | Java 17 |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security + JWT (JJWT) |
| Migrations | Flyway |
| Validation | Jakarta Bean Validation |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |

---

## Project Structure

```
finance-backend/
├── src/main/java/com/finance/
│   ├── FinanceApplication.java
│   ├── config/             # SecurityConfig, AuditConfig, OpenApiConfig
│   ├── controller/         # REST controllers (thin layer, no business logic)
│   ├── domain/
│   │   ├── user/           # User entity, Role enum, UserStatus enum
│   │   └── transaction/    # Transaction entity, Category entity, TransactionType enum
│   ├── dto/
│   │   ├── request/        # Validated inbound payloads
│   │   └── response/       # Outbound response shapes
│   ├── exception/          # Custom exceptions + GlobalExceptionHandler
│   ├── repository/         # Spring Data JPA interfaces with custom @Query methods
│   ├── security/           # JWT filter, token provider, UserDetails impl
│   ├── service/            # All business logic lives here
│   └── util/               # ApiResponse wrapper, RoleGuard, DateRangeUtil
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/       # Flyway versioned SQL migrations
├── src/test/java/com/finance/
│   ├── service/            # Unit tests (Mockito)
│   └── controller/         # Web layer slice tests (MockMvc)
├── docker-compose.yml
├── Dockerfile
└── .env.example
```

---

## Roles and Permissions

| Endpoint / Action | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| Login | ✅ | ✅ | ✅ |
| View transactions | ✅ | ✅ | ✅ |
| View dashboard summary | ✅ | ✅ | ✅ |
| Dashboard date-range summary | ❌ | ✅ | ✅ |
| Create / update / delete transactions | ❌ | ✅ | ✅ |
| List categories | ✅ | ✅ | ✅ |
| Create / update / delete categories | ❌ | ❌ | ✅ |
| Manage users | ❌ | ❌ | ✅ |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 15 (or Docker)

---

### Option 1 — Docker Compose (recommended)

```bash
# Clone and start everything (PostgreSQL + app)
git clone <your-repo-url>
cd finance-backend
docker-compose up --build
```

App runs at: `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`

---

### Option 2 — Run locally

**1. Create the database**

```sql
CREATE DATABASE finance_db;
```

**2. Configure environment**

```bash
cp .env.example .env
# Edit .env with your DB credentials and JWT secret
```

**3. Run the application**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Flyway will automatically run all migrations and seed initial data on first startup.

---

## Default Seed Users

| Role | Email | Password |
|---|---|---|
| Admin | admin@finance.com | Admin@1234 |
| Analyst | analyst@finance.com | Analyst@1234 |
| Viewer | viewer@finance.com | Viewer@1234 |

> **Change these passwords immediately in any non-local environment.**

---

## API Reference

### Authentication

```
POST /api/auth/login
```

```json
{
  "email": "admin@finance.com",
  "password": "Admin@1234"
}
```

Returns a `Bearer` token. Include it in all subsequent requests:

```
Authorization: Bearer <token>
```

---

### Transactions

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/transactions` | All | List with filters |
| GET | `/api/transactions/{id}` | All | Get by ID |
| POST | `/api/transactions` | Analyst, Admin | Create |
| PUT | `/api/transactions/{id}` | Analyst, Admin | Update |
| DELETE | `/api/transactions/{id}` | Analyst, Admin | Soft delete |

**Filter parameters for GET `/api/transactions`:**

| Param | Type | Example |
|---|---|---|
| `type` | `INCOME` or `EXPENSE` | `?type=INCOME` |
| `categoryId` | Long | `?categoryId=1` |
| `from` | ISO date | `?from=2024-01-01` |
| `to` | ISO date | `?to=2024-12-31` |
| `page` | int (default 0) | `?page=0` |
| `size` | int (default 20) | `?size=10` |

---

### Dashboard

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/dashboard/summary` | All | Total income, expenses, net balance, category totals, monthly trends, recent transactions |
| GET | `/api/dashboard/summary/range?from=&to=` | Analyst, Admin | Summary for a custom date range |

---

### Users (Admin only)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users` | List all users (filter by `?status=ACTIVE`) |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create user |
| PUT | `/api/users/{id}` | Update name, role, or status |
| DELETE | `/api/users/{id}` | Deactivate user (soft) |

---

### Categories (read: all | write: Admin)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/categories` | List all |
| POST | `/api/categories` | Create |
| PUT | `/api/categories/{id}` | Update |
| DELETE | `/api/categories/{id}` | Delete |

---

## Response Format

All responses follow a consistent envelope:

```json
{
  "success": true,
  "message": "Optional message",
  "data": {  },
  "timestamp": "2024-01-15T10:30:00"
}
```

Error responses:

```json
{
  "success": false,
  "message": "Resource not found with id: 99",
  "timestamp": "2024-01-15T10:30:01"
}
```

---

## Running Tests

```bash
mvn test
```

Tests cover:
- `TransactionServiceTest` — create, soft delete, not-found handling
- `UserServiceTest` — user creation, duplicate email guard
- `AuthControllerTest` — login success and validation failure (MockMvc)

---

## Assumptions and Design Decisions

- **Soft deletes** — transactions are never hard-deleted. A `deleted` boolean flag is used so historical data is preserved for auditing.
- **Flyway migrations** — schema is managed entirely through versioned SQL files. Hibernate's `ddl-auto` is set to `validate`, never `update`, in all environments.
- **JWT is stateless** — no token revocation is implemented. Tokens expire after 24 hours by default.
- **Analyst write access** — Analysts can create and modify financial records but cannot manage users or categories. Admins have full access.
- **Password in seed data** — the seed migration uses a pre-computed BCrypt hash. All three seed users currently share the same hash for simplicity. Change them before any real deployment.
- **Category is optional on a transaction** — transactions can exist without a category to allow quick entry.

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `finance_db` | Database name |
| `DB_USERNAME` | `postgres` | DB user |
| `DB_PASSWORD` | `postgres` | DB password |
| `JWT_SECRET` | *(see .env.example)* | Must be 32+ characters |
| `JWT_EXPIRATION_MS` | `86400000` | Token TTL in milliseconds (24h) |
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` or `prod` |
