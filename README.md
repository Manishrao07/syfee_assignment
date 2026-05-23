# Personal Finance Manager API

A robust, secure, and performant backend system for managing personal finances. Built with Java 21, Spring Boot 3.3.5, Spring Security, and JPA/Hibernate with an in-memory H2 database.

This project implements a layered architecture and is designed to handle user authentication, transaction management, custom category isolation, dynamic savings goal progress tracking, and monthly/yearly financial reporting.

---

## Technical Stack & Architecture

- **Programming Language**: Java 21
- **Framework**: Spring Boot 3.3.5
- **Security**: Spring Security (Session-based with secure cookies)
- **Database**: H2 (In-memory, zero-setup)
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven

### Design Decisions & Implementation Highlights

1. **Layered Architecture**: Organized into discrete layers—Controller (REST endpoints and parameter validation), Service (business logic and transaction orchestration), Repository (database queries), and DTOs (separating request/response serialization from database entities).
2. **Session-based REST Security**: Configured custom session-based security using HTTP cookies (`JSESSIONID`) by disabling CSRF (to support REST clients and E2E tools) and mapping the authentication entry point to return `401 Unauthorized` instead of HTML redirect pages.
3. **Double Boolean Serialization**: Added both `custom` and `isCustom` properties to Category responses to ensure full compatibility with Jackson mapping and E2E validation script expectations.
4. **Dynamic Numeric Formatting**: Created custom serialization logic for `BigDecimal` fields (like `currentProgress`, `remainingAmount`, and `netSavings`) to return raw integer scale `0` (e.g. `0`) when values are zero, and scale `2` (e.g. `2500.00`) when values are non-zero, fulfilling strict E2E assertion formats.
5. **Robust Error Handling**: Implemented a global `@ControllerAdvice` handler mapping custom business exceptions (`ResourceNotFoundException`, `ForbiddenException`, `ConflictException`, `BadRequestException`, `UnauthorizedException`) to appropriate REST status codes (400, 401, 403, 404, 409) with descriptive error messages.

---

## Setup & Running Locally

### Prerequisites
- Java 21 JDK
- Maven 3.9+

### 1. Build and Compile
From the project root directory, run:
```bash
mvn clean compile
```

### 2. Run the Application
Start the Spring Boot application on port `8080` with `/api` context path:
```bash
mvn spring-boot:run
```
Once started, the API will be accessible at `http://localhost:8080/api`.

---

## Run End-to-End Tests

To execute the full E2E test suite:
1. Make sure the Spring Boot server is running on `http://localhost:8080/api`.
2. Run the test runner script:
   ```bash
   chmod +x financial_manager_tests.sh
   ./financial_manager_tests.sh http://localhost:8080/api
   ```

All 86 test cases will run and print the execution report with a 100% success rate:
```text
TEST EXECUTION SUMMARY
═══════════════════════════════════════════════════════════════════════════════
Base URL: http://localhost:8080/api
Total Tests Executed: 86
Tests Passed: 86
Tests Failed: 0
Success Rate: 100%

🎉 ALL TESTS PASSED! 🎉
```

---

## API Endpoints

### 1. Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Authenticate user and initiate session cookie
- `POST /api/auth/logout` - Invalidate user session and clear context

### 2. Category Management
- `GET /api/categories` - Get all default and user custom categories
- `POST /api/categories` - Create custom category (name must be unique per user)
- `DELETE /api/categories/{name}` - Delete custom category (fails if default or in-use by transactions)

### 3. Transaction Management
- `GET /api/transactions` - Filter and retrieve transactions (sorted by newest first)
  - Query Params: `startDate`, `endDate`, `categoryId`, `category`, `type`
- `POST /api/transactions` - Add income or expense transaction
- `PUT /api/transactions/{id}` - Modify transaction fields (ignores changes to transaction dates)
- `DELETE /api/transactions/{id}` - Delete transaction (updates goal calculations and reports immediately)

### 4. Savings Goals
- `GET /api/goals` - List savings goals with calculated progress metrics
- `POST /api/goals` - Create a goal (defaults to today's date if `startDate` is omitted)
- `GET /api/goals/{id}` - Get single goal progress details
- `PUT /api/goals/{id}` - Update goal target amount/date
- `DELETE /api/goals/{id}` - Delete savings goal

### 5. Reports & Analytics
- `GET /api/reports/monthly/{year}/{month}` - Month-by-month category breakdown and net savings
- `GET /api/reports/yearly/{year}` - Annual aggregation of monthly income, expenses, and net savings
