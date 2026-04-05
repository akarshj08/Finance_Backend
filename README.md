# Finance Dashboard Backend API
 
A comprehensive, production-ready Spring Boot backend API for financial data management, featuring advanced transaction tracking, user management, and real-time dashboard analytics.
 
## 🚀 Features
 
### Core Functionality
- **Multi-user Finance Management**: Role-based access control with VIEWER, ANALYST, and ADMIN roles
- **Advanced Transaction Tracking**: Income, expense, and investment management with categorization
- **Real-time Dashboard Analytics**: Comprehensive financial insights and trends
- **Secure Authentication**: JWT-based authentication with configurable expiration
- **Data Integrity**: Soft delete implementation and audit trails
- **RESTful API Design**: Clean, intuitive API endpoints with OpenAPI 3.0 documentation
 
### Advanced Features
- **Sophisticated Filtering**: Multi-parameter transaction filtering with date ranges
- **Pagination & Sorting**: Efficient data retrieval for large datasets
- **Database Migrations**: Flyway-based schema versioning
- **Comprehensive Validation**: Input validation at multiple layers
- **Error Handling**: Centralized exception handling with meaningful error messages
 
## 📋 Table of Contents
 
- [Technology Stack](#-technology-stack)
- [Database Schema](#-database-schema)
- [API Documentation](#-api-documentation)
- [Installation & Setup](#-installation--setup)
- [Configuration](#️-configuration)
- [Security](#-security)
- [Development](#-development)
- [Testing](#-testing)
- [Deployment](#-deployment)
 
## 🛠 Technology Stack
 
### Backend Framework
- **Spring Boot 3.2.0** - Modern Java framework with auto-configuration
- **Java 17** - Latest LTS Java version with enhanced features
- **Spring Security 6** - Comprehensive security framework
- **Spring Data JPA** - Database abstraction layer
 
### Database & Migration
- **PostgreSQL** - Production-grade relational database
- **Flyway** - Database migration management
- **Hibernate** - ORM implementation with PostgreSQL dialect
 
### Security & Authentication
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password encryption
- **Spring Security** - Method-level security with role-based access
 
### Documentation & Development
- **OpenAPI 3.0** - API documentation with Swagger UI
- **Lombok** - Code generation for boilerplate reduction
- **Maven** - Dependency management and build automation
 
### Testing
- **JUnit 5** - Modern testing framework
- **Spring Boot Test** - Integration testing support
- **H2 Database** - In-memory database for testing
 
## 🗃 Database Schema
 
### Entity Relationship Diagram
 
```
┌─────────────────┐       ┌──────────────────┐       ┌─────────────────┐
│      users      │       │   transactions   │       │    categories   │
├─────────────────┤       ├──────────────────┤       ├─────────────────┤
│ id (PK)         │◄──────┤ id (PK)          │──────►│ id (PK)         │
│ full_name       │       │ amount           │       │ name            │
│ email (UNIQUE)  │       │ type             │       │ description     │
│ password        │       │ category_id (FK) │       └─────────────────┘
│ role            │       │ date             │
│ status          │       │ notes            │
│ created_at      │       │ created_by (FK)  │
│ updated_at      │       │ deleted          │
└─────────────────┘       │ created_at       │
                         │ updated_at       │
                         └──────────────────┘
```
 
### Table Definitions
 
#### Users Table
```sql
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(100)        NOT NULL,
    email       VARCHAR(150)        NOT NULL UNIQUE,
    password    VARCHAR(255)        NOT NULL,
    role        VARCHAR(20)         NOT NULL CHECK (role IN ('VIEWER','ANALYST','ADMIN')),
    status      VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);
```
 
#### Categories Table
```sql
CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)        NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);
```
 
#### Transactions Table
```sql
CREATE TABLE transactions (
    id          BIGSERIAL PRIMARY KEY,
    amount      DECIMAL(15,2)       NOT NULL,
    type        VARCHAR(20)         NOT NULL CHECK (type IN ('INCOME','EXPENSE','INVESTMENT')),
    category_id BIGINT              REFERENCES categories(id),
    date        DATE                NOT NULL,
    notes       VARCHAR(500),
    created_by  BIGINT              NOT NULL REFERENCES users(id),
    deleted     BOOLEAN            NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);
```
 
### Data Relationships
 
- **Users → Transactions**: One-to-many relationship (user can create multiple transactions)
- **Categories → Transactions**: One-to-many relationship (category can have multiple transactions)
- **Soft Delete**: Transactions are marked as deleted rather than physically removed
 
## 📚 API Documentation
 
### Authentication
 
All API endpoints (except `/api/auth/login`) require JWT authentication. Include the token in the Authorization header:
 
```
Authorization: Bearer <your-jwt-token>
```
 
### Standard Response Format
 
All API responses follow a consistent format:
 
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { /* response data */ },
  "timestamp": "2024-01-01T12:00:00"
}
```
 
### Error Response Format
 
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2024-01-01T12:00:00"
}
```
 
---
 
## 🔐 Authentication API
 
### Login
**POST** `/api/auth/login`
 
Authenticate user and receive JWT token.
 
**Request Body:**
```json
{
  "email": "admin@finance.com",
  "password": "password"
}
```
 
**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "email": "admin@finance.com",
      "role": "ADMIN",
      "fullName": "System Admin"
    }
  }
}
```
 
---
 
## 👥 User Management API
 
*All endpoints require ADMIN role*
 
### Get All Users
**GET** `/api/users`
 
Retrieve paginated list of users with optional status filtering.
 
**Query Parameters:**
- `status` (optional): Filter by user status (`ACTIVE` or `INACTIVE`)
- `page` (default: 0): Page number for pagination
- `size` (default: 20): Number of users per page
 
**Example Request:**
```
GET /api/users?status=ACTIVE&page=0&size=10
```
 
**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "fullName": "System Admin",
        "email": "admin@finance.com",
        "role": "ADMIN",
        "status": "ACTIVE",
        "createdAt": "2024-01-01T10:00:00",
        "updatedAt": "2024-01-01T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```
 
### Get User by ID
**GET** `/api/users/{id}`
 
Retrieve specific user details.
 
### Create User
**POST** `/api/users`
 
Create a new user account.
 
**Request Body:**
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "role": "ANALYST"
}
```
 
### Update User
**PUT** `/api/users/{id}`
 
Update user role or status.
 
**Request Body:**
```json
{
  "role": "ADMIN",
  "status": "ACTIVE"
}
```
 
### Deactivate User
**DELETE** `/api/users/{id}`
 
Soft delete user account (sets status to INACTIVE).
 
---
 
## 📊 Transaction Management API
 
### Get All Transactions (Advanced Filtering)
**GET** `/api/transactions`
 
Retrieve transactions with comprehensive filtering capabilities. This endpoint supports multiple filter parameters that can be combined for precise data retrieval.
 
**Query Parameters:**
- `type` (optional): Filter by transaction type (`INCOME`, `EXPENSE`, `INVESTMENT`)
- `categoryId` (optional): Filter by specific category ID
- `from` (optional): Start date for date range filtering (ISO date format: `YYYY-MM-DD`)
- `to` (optional): End date for date range filtering (ISO date format: `YYYY-MM-DD`)
- `page` (default: 0): Page number for pagination
- `size` (default: 20): Number of transactions per page
 
**Filtering Combinations:**
 
1. **By Type Only:**
   ```
   GET /api/transactions?type=EXPENSE
   ```
 
2. **By Category Only:**
   ```
   GET /api/transactions?categoryId=5
   ```
 
3. **By Date Range Only:**
   ```
   GET /api/transactions?from=2024-01-01&to=2024-01-31
   ```
 
4. **Combined Filters:**
   ```
   GET /api/transactions?type=EXPENSE&categoryId=5&from=2024-01-01&to=2024-01-31
   ```
 
5. **Complex Filtering with Pagination:**
   ```
   GET /api/transactions?type=INCOME&from=2024-01-01&page=0&size=50
   ```
 
**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "amount": 5000.00,
        "type": "INCOME",
        "categoryName": "Salary",
        "date": "2024-01-15",
        "notes": "Monthly salary",
        "createdByEmail": "admin@finance.com",
        "createdAt": "2024-01-15T09:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```
 
**Advanced Filtering Features:**
 
- **Date Range Filtering**: Supports both start and end dates, or either one independently
- **Type-based Filtering**: Separate filtering for income, expenses, and investments
- **Category Filtering**: Filter transactions within specific categories
- **Combinatorial Logic**: All filter parameters work together using AND logic
- **Sorting**: Results are automatically sorted by date in descending order (newest first)
- **Pagination**: Efficient handling of large datasets with configurable page sizes
 
### Get Transaction by ID
**GET** `/api/transactions/{id}`
 
Retrieve specific transaction details.
 
**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "amount": 5000.00,
    "type": "INCOME",
    "categoryName": "Salary",
    "date": "2024-01-15",
    "notes": "Monthly salary",
    "createdByEmail": "admin@finance.com",
    "createdAt": "2024-01-15T09:00:00"
  }
}
```
 
### Create Transaction
**POST** `/api/transactions`
 
*Requires ANALYST or ADMIN role*
 
Create a new financial transaction.
 
**Request Body:**
```json
{
  "amount": 1500.00,
  "type": "EXPENSE",
  "categoryId": 5,
  "date": "2024-01-20",
  "notes": "Rent payment for January"
}
```
 
**Validation Rules:**
- `amount`: Must be positive (minimum 0.01)
- `type`: Must be `INCOME`, `EXPENSE`, or `INVESTMENT`
- `date`: Cannot be in the future
- `notes`: Optional, maximum 500 characters
- `categoryId`: Optional, but recommended for better categorization
 
### Update Transaction
**PUT** `/api/transactions/{id}`
 
*Requires ANALYST or ADMIN role*
 
Update existing transaction details.
 
**Request Body:**
```json
{
  "amount": 1600.00,
  "type": "EXPENSE",
  "categoryId": 5,
  "date": "2024-01-20",
  "notes": "Updated rent payment for January"
}
```
 
### Delete Transaction
**DELETE** `/api/transactions/{id}`
 
*Requires ANALYST or ADMIN role*
 
Soft delete transaction (sets `deleted` flag to true). Transaction remains in database but won't appear in normal queries.
 
---
 
## 🏷️ Category Management API
 
### Get All Categories
**GET** `/api/categories`
 
Retrieve list of all available categories.
 
**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Salary",
      "description": "Monthly or periodic salary income",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    },
    {
      "id": 5,
      "name": "Food",
      "description": "Groceries and dining expenses",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ]
}
```
 
### Create Category
**POST** `/api/categories`
 
*Requires ADMIN role*
 
Create a new transaction category.
 
**Request Body:**
```json
{
  "name": "Travel",
  "description": "Business and personal travel expenses"
}
```
 
### Update Category
**PUT** `/api/categories/{id}`
 
*Requires ADMIN role*
 
Update category details.
 
### Delete Category
**DELETE** `/api/categories/{id}`
 
*Requires ADMIN role*
 
Delete a category (only if no transactions are associated).
 
---
 
## 📈 Dashboard Analytics API
 
### Get Dashboard Summary
**GET** `/api/dashboard/summary`
 
*Requires VIEWER, ANALYST, or ADMIN role*
 
Retrieve comprehensive financial summary with analytics.
 
**Response:**
```json
{
  "success": true,
  "data": {
    "totalIncome": 8500.00,
    "totalExpenses": 3200.00,
    "totalInvestments": 2000.00,
    "netBalance": 3300.00,
    "categoryTotals": [
      {
        "categoryName": "Salary",
        "type": "INCOME",
        "total": 8500.00
      },
      {
        "categoryName": "Food",
        "type": "EXPENSE",
        "total": 800.00
      }
    ],
    "monthlyTrends": [
      {
        "year": 2024,
        "month": 1,
        "type": "INCOME",
        "total": 8500.00
      },
      {
        "year": 2024,
        "month": 1,
        "type": "EXPENSE",
        "total": 3200.00
      }
    ],
    "recentTransactions": [
      {
        "id": 1,
        "amount": 5000.00,
        "type": "INCOME",
        "categoryName": "Salary",
        "date": "2024-01-15",
        "notes": "Monthly salary",
        "createdByEmail": "admin@finance.com",
        "createdAt": "2024-01-15T09:00:00"
      }
    ]
  }
}
```
 
### Get Custom Date Range Summary
**GET** `/api/dashboard/summary/range`
 
*Requires ANALYST or ADMIN role*
 
Retrieve financial summary for a specific date range.
 
**Query Parameters:**
- `from` (required): Start date (ISO format: `YYYY-MM-DD`)
- `to` (required): End date (ISO format: `YYYY-MM-DD`)
 
**Example Request:**
```
GET /api/dashboard/summary/range?from=2024-01-01&to=2024-01-31
```
 
---
 
## 🚀 Installation & Setup
 
### Prerequisites
- **Java 17** or higher
- **PostgreSQL 13** or higher
- **Maven 3.8** or higher
- **Git**
 
### Database Setup
 
1. **Create PostgreSQL Database:**
   ```sql
   CREATE DATABASE financebackend;
   CREATE USER finance_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE financebackend TO finance_user;
   ```
 
2. **Update Configuration:**
   Modify `src/main/resources/application.yml` with your database credentials:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/financebackend
       username: finance_user
       password: your_password
   ```
 
### Application Setup
 
1. **Clone Repository:**
   ```bash
   git clone <repository-url>
   cd finance-backend
   ```
 
2. **Build Application:**
   ```bash
   mvn clean install
   ```
 
3. **Run Application:**
   ```bash
   mvn spring-boot:run
   ```
 
4. **Access Application:**
   - API Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - OpenAPI Docs: `http://localhost:8080/v3/api-docs`
 
### Docker Setup
 
1. **Build Docker Image:**
   ```bash
   docker build -t finance-backend .
   ```
 
2. **Run with Docker Compose:**
   ```yaml
   version: '3.8'
   services:
     postgres:
       image: postgres:13
       environment:
         POSTGRES_DB: financebackend
         POSTGRES_USER: finance_user
         POSTGRES_PASSWORD: your_password
       ports:
         - "5432:5432"
 
     app:
       image: finance-backend
       ports:
         - "8080:8080"
       depends_on:
         - postgres
       environment:
         SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/financebackend
         SPRING_DATASOURCE_USERNAME: finance_user
         SPRING_DATASOURCE_PASSWORD: your_password
   ```
 
---
 
## ⚙️ Configuration
 
### Application Configuration
 
The application uses YAML configuration in `src/main/resources/application.yml`:
 
```yaml
spring:
  application:
    name: finance-backend
 
  datasource:
    url: jdbc:postgresql://localhost:5432/financebackend
    username: postgres
    password: root
    driver-class-name: org.postgresql.Driver
 
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
 
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    repair-on-migrate: true
 
server:
  port: ${PORT:8080}
 
app:
  jwt:
    secret: ${JWT_SECRET:ThisIsAVeryLongSecretKeyForJWTThatMustBeAtLeast256BitsLong!!}
    expiration-ms: ${JWT_EXPIRATION_MS:86400000}
 
springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs
  show-actuator: false
 
logging:
  level:
    com.finance: INFO
    org.springframework.security: WARN
```
 
### Environment Variables
 
- `PORT`: Server port (default: 8080)
- `JWT_SECRET`: Secret key for JWT token signing
- `JWT_EXPIRATION_MS`: Token expiration time in milliseconds (default: 24 hours)
- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
 
---
 
## 🔐 Security
 
### Authentication Flow
 
1. **User Login**: Client sends credentials to `/api/auth/login`
2. **Token Generation**: Server validates credentials and generates JWT
3. **Token Storage**: Client stores JWT (typically in localStorage or httpOnly cookie)
4. **Authenticated Requests**: Client includes JWT in Authorization header for subsequent requests
 
### Role-Based Access Control
 
- **VIEWER**: Read-only access to dashboard, transactions, and categories
- **ANALYST**: Full transaction management + dashboard analytics
- **ADMIN**: Complete system access including user management
 
### Security Features
 
- **JWT Authentication**: Stateless authentication with configurable expiration
- **Password Encryption**: BCrypt hashing with salt
- **Method-Level Security**: Fine-grained access control using annotations
- **CORS Configuration**: Configurable cross-origin resource sharing
- **Input Validation**: Comprehensive validation at controller and service layers
 
### Security Headers
 
The application implements several security headers:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
 
---
 
## 🧪 Development
 
### Project Structure
 
```
src/main/java/com/finance/
├── Configuration/          # Security and API configuration
├── Entity/                # JPA entities
│   ├── transaction/       # Transaction-related entities
│   └── user/             # User-related entities
├── controller/           # REST API controllers
├── dto/                  # Data Transfer Objects
│   ├── request/          # Request DTOs
│   └── response/         # Response DTOs
├── exception/            # Custom exceptions and handlers
├── repository/           # JPA repositories
├── security/             # Security components
├── service/              # Business logic layer
└── util/                 # Utility classes
```
 
### Code Quality
 
- **Lombok**: Reduces boilerplate code with annotations
- **Validation**: Jakarta Bean Validation for input constraints
- **Exception Handling**: Centralized error handling with `@ControllerAdvice`
- **Audit Trail**: Automatic timestamp tracking with `@CreatedDate` and `@LastModifiedDate`
 
### Database Migrations
 
Database schema changes are managed through Flyway migrations in `src/main/resources/db/migration/`:
 
- `V1__create_users.sql`: User table creation
- `V2__create_categories.sql`: Category table creation
- `V3__create_transactions.sql`: Transaction table creation
- `V4__seed_data.sql`: Initial seed data
 
### API Documentation
 
The application uses OpenAPI 3.0 for API documentation:
- **Swagger UI**: Interactive API explorer at `/swagger-ui.html`
- **OpenAPI JSON**: Machine-readable spec at `/v3/api-docs`
- **Annotations**: Comprehensive API documentation using `@Operation`, `@Tag`, etc.
 
---
 
## 🧪 Testing
 
### Running Tests
 
```bash
# Run all tests
mvn test
 
# Run specific test class
mvn test -Dtest=TransactionServiceTest
 
# Run tests with coverage
mvn test jacoco:report
```
 
### Test Structure
 
- **Unit Tests**: Service layer business logic testing
- **Integration Tests**: Repository and controller testing with H2 database
- **Security Tests**: Authentication and authorization testing
- **Validation Tests**: Input constraint validation
 
### Test Database
 
Tests use H2 in-memory database for fast, isolated testing:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```
 
---
 
## 🚀 Deployment
 
### Production Considerations
 
1. **Database Security**:
   - Use strong database passwords
   - Enable SSL connections
   - Regular database backups
 
2. **Application Security**:
   - Use environment variables for sensitive configuration
   - Enable HTTPS in production
   - Configure proper CORS policies
 
3. **Performance**:
   - Configure connection pooling
   - Enable database query caching
   - Monitor application metrics
 
4. **Monitoring**:
   - Implement health checks (`/actuator/health`)
   - Configure application logging
   - Set up error tracking
 
### Environment-Specific Configurations
 
Create separate configuration files for different environments:
 
- `application-dev.yml` - Development environment
- `application-staging.yml` - Staging environment
- `application-prod.yml` - Production environment
 
### Deployment Options
 
1. **Traditional Server**: Deploy as WAR file to Tomcat/Jetty
2. **Docker Container**: Containerized deployment with Docker
3. **Cloud Platform**: Deploy to AWS, Azure, or Google Cloud
4. **Kubernetes**: Orchestrate with Kubernetes for scalability
 
---
 
## 📊 Default Seed Data
 
### Default Users
 
All seed users have the password: `password`
 
| Email | Role | Description |
|-------|------|-------------|
| `admin@finance.com` | ADMIN | Full system administration access |
| `analyst@finance.com` | ANALYST | Transaction management and analytics |
| `viewer@finance.com` | VIEWER | Read-only dashboard access |
 
### Default Categories
 
#### Income Categories
- **Salary**: Monthly or periodic salary income
- **Freelance**: Freelance and contract work income
- **Investment**: Returns from investments and dividends
- **Rent**: Rental income from properties
 
#### Expense Categories
- **Food**: Groceries and dining expenses
- **Transport**: Travel, fuel, and commuting costs
- **Utilities**: Electricity, water, internet bills
- **Healthcare**: Medical and healthcare expenses
- **Entertainment**: Movies, subscriptions, hobbies
- **Education**: Courses, books, training
 
---
 
## 🤝 Contributing
 
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
 
### Development Guidelines
 
- Follow Java coding conventions
- Write unit tests for new features
- Update API documentation for new endpoints
- Use meaningful commit messages
- Ensure all tests pass before submitting
 
---
 
## 📞 Support
 
For support and questions:
- Create an issue in the repository
- Check the API documentation at `/swagger-ui.html`
- Review the application logs for troubleshooting
 
---
 
## 🔄 Version History
 
- **v1.0.0** - Initial release with core functionality
  - User management with role-based access
  - Transaction management with advanced filtering
  - Category management
  - Dashboard analytics
  - JWT authentication
  - OpenAPI documentation
 
---

**Built with ❤️ using Spring Boot and modern Java technologies By Akarsh Jain**
