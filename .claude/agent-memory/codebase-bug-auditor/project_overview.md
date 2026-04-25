---
name: Project Overview
description: Core stack, architecture, and conventions for the taskmanager project
type: project
---

Spring Boot 4.0.6 / Java 21 REST API. Build: Maven. Database: PostgreSQL (JPA/Hibernate, ddl-auto=update). Auth: stateless JWT (jjwt 0.11.5, HS256). DI: Lombok + Spring @RequiredArgsConstructor.

**Why:** Understanding this baseline avoids false positives (e.g., raw SQL in migrations) and calibrates Spring-specific advice.
**How to apply:** Tailor all recommendations to Spring Boot idioms. Note jjwt 0.11.5 is NOT the latest (1.x series exists).

Key directories:
- `src/main/java/com/app/taskmanager/controller/` — AuthController, TaskController, TestController
- `src/main/java/com/app/taskmanager/service/` — UserService (concrete), TaskService (interface) + TaskServiceImpl
- `src/main/java/com/app/taskmanager/security/` — JwtFilter (OncePerRequestFilter)
- `src/main/java/com/app/taskmanager/util/` — JwtUtil, SecurityUtil, SpecificationUtils
- `src/main/java/com/app/taskmanager/entity/` — User, Task, Role (enum), Status (enum)
- `src/main/java/com/app/taskmanager/exception/` — GlobalExceptionHandler, ForbiddenException, NotFoundException
- `src/main/resources/application.yml` — DB credentials via env vars (safe), show-sql=true (dev-only concern)
