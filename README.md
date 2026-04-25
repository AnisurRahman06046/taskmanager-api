# Task Manager API

A RESTful task management API built with Spring Boot 4 and Java 21. Users register and log in with JWT-based authentication, then create, update, list (with pagination, filtering, and sorting), and delete tasks they own.

## Tech Stack

- **Java** 21
- **Spring Boot** 4.0.6 (Web MVC, Data JPA, Security, Validation)
- **PostgreSQL** for persistence
- **JJWT** 0.11.5 for JSON Web Tokens
- **Lombok** for boilerplate reduction
- **Maven** for build

## Project Structure

```
src/main/java/com/app/taskmanager/
├── TaskmanagerApplication.java     # Entry point
├── common/dto/                     # ApiResponse, PaginateResponse wrappers
├── config/SecurityConfig.java      # Spring Security + JWT wiring
├── controller/                     # AuthController, TaskController, TestController
├── dto/                            # Request/response DTOs
├── entity/                         # User, Task, Role, Status
├── exception/                      # Global handler + custom exceptions
├── repository/                     # JPA repositories
├── security/JwtFilter.java         # Per-request JWT validation
├── service/                        # Business logic (UserService, TaskService)
├── specification/                  # JPA Specifications for dynamic queries
└── util/                           # JwtUtil, SecurityUtil, SpecificationUtils
```

## Prerequisites

- JDK 21
- Maven 3.6+ (a wrapper `./mvnw` is included)
- PostgreSQL running on `localhost:5432`

## Database Setup

Create a PostgreSQL database that matches the dev profile:

```sql
CREATE DATABASE "taskmanagerDB";
```

Default dev credentials (see `src/main/resources/application-dev.yml`):

| Property | Value |
|----------|-------|
| URL      | `jdbc:postgresql://localhost:5432/taskmanagerDB` |
| Username | `anis` |
| Password | `anis1234` |

Hibernate is configured with `ddl-auto: update` in dev, so schema is created/updated automatically on startup.

## Configuration

Two profiles are provided:

- **dev** (default) — local PostgreSQL, SQL logging on, schema auto-update
- **prod** — reads `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` from the environment, schema validate-only

Switch profiles via `--spring.profiles.active=prod` or the `SPRING_PROFILES_ACTIVE` env var.

The server listens on port `8080`.

## Build & Run

```bash
# Run in dev mode
./mvnw spring-boot:run

# Build a runnable JAR
./mvnw clean package
java -jar target/taskmanager-0.0.1-SNAPSHOT.jar

# Run tests
./mvnw test
```

## Authentication

All endpoints under `/api/v1/auth/**` are public. Every other endpoint requires a `Bearer` token in the `Authorization` header.

Tokens are HS256-signed and expire after 1 hour. The token's subject is the user's email.

```
Authorization: Bearer <accessToken>
```

> **Note:** the JWT signing secret is hardcoded in `JwtUtil` for development. Move it to configuration / environment before deploying.

## API Reference

All responses are wrapped in:

```json
{
  "success": true,
  "message": "...",
  "data": { ... },
  "timestamp": "2026-04-25T10:30:00"
}
```

### Auth

#### `POST /api/v1/auth/register`

Public. Creates a new user with the `USER` role and BCrypt-encoded password.

```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "secret"
}
```

Validation: `name` not blank, `email` valid email, `password` min length 4.

#### `POST /api/v1/auth/login`

Public. Returns a JWT access token.

```json
{
  "email": "jane@example.com",
  "password": "secret"
}
```

Response:

```json
{
  "success": true,
  "message": "Login successfull",
  "data": { "accessToken": "eyJhbGciOiJIUzI1NiJ9..." }
}
```

### Tasks

All task endpoints require authentication. A user can only read, update, or delete tasks they own — attempting to touch another user's task returns `403 forbidden`.

#### `POST /api/v1/tasks/add`

Create a task owned by the authenticated user.

```json
{
  "title": "Write README",
  "description": "Describe the API",
  "status": "TODO"
}
```

Validation: `title` not blank, `status` must match `TODO|IN_PROGRESS|DONE`.

#### `GET /api/v1/tasks`

List the authenticated user's tasks. Supports pagination, sorting, and filtering.

| Query param | Type   | Default     | Notes |
|-------------|--------|-------------|-------|
| `page`      | int    | `0`         | Zero-indexed |
| `size`      | int    | `10`        | |
| `sortBy`    | string | `createdAt` | Allowed: `createdAt`, `title`, `status` (descending) |
| `status`    | string | —           | Filter by `TODO`, `IN_PROGRESS`, or `DONE` |
| `title`     | string | —           | Case-insensitive partial match |

Response wraps a `PaginateResponse<TaskResponse>` with `data`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`.

#### `PUT /api/v1/tasks/{id}`

Update a task. All fields are optional; only non-null fields are applied.

```json
{
  "title": "Updated title",
  "status": "IN_PROGRESS"
}
```

#### `DELETE /api/v1/tasks/{id}`

Delete a task you own.

## Domain Model

**User** (`users` table)

| Field    | Type        | Notes |
|----------|-------------|-------|
| id       | Long        | PK |
| name     | String      | |
| email    | String      | unique, not null |
| password | String      | BCrypt-encoded |
| role     | enum `Role` | `USER` or `ADMIN` |

**Task** (`tasks` table)

| Field       | Type           | Notes |
|-------------|----------------|-------|
| id          | Long           | PK |
| title       | String         | |
| description | String         | |
| createdAt   | LocalDateTime  | set on creation |
| status      | enum `Status`  | `TODO`, `IN_PROGRESS`, `DONE` |
| user        | User           | many-to-one, lazy |

## Error Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) wraps:

- `RuntimeException` → `ApiResponse` with `success=false` and the exception message
- `ForbiddenException` → HTTP `403` with body `forbidden`

Bean Validation errors fall back to Spring's default `400` response.

## Quick cURL Walkthrough

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane","email":"jane@example.com","password":"secret"}'

# Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"secret"}' \
  | jq -r '.data.accessToken')

# Create a task
curl -X POST http://localhost:8080/api/v1/tasks/add \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"First task","description":"Hello","status":"TODO"}'

# List tasks
curl "http://localhost:8080/api/v1/tasks?page=0&size=10&status=TODO" \
  -H "Authorization: Bearer $TOKEN"
```
