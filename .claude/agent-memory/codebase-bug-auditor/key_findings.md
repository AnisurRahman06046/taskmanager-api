---
name: Key Findings
description: Recurring anti-patterns and confirmed issues from the initial April 2025 audit
type: project
---

**Why:** Provides a fast baseline for future audits so known issues are not re-investigated from scratch.
**How to apply:** Cross-check these on subsequent audits; flag if fixed or if new instances appear.

## Hardcoded JWT Secret (Critical)
`JwtUtil.java:14` — `private final String SECRET = "mysecretkeymysecretkeymysecretkey"` — hardcoded in source. Must be externalised to env var.

## JWT isValid Uses `parse` Not `parseClaimsJws` (High)
`JwtUtil.java:39` — `parse()` does not verify signature on JWS tokens; use `parseClaimsJws()`. Expired tokens silently treated as invalid (swallowed exception) but the signature bypass is the primary risk.

## Missing Session Stateless Config (High)
`SecurityConfig.java` — No `sessionManagement(s -> s.sessionCreationPolicy(STATELESS))`. Spring Security creates HTTP sessions by default, undermining JWT-only design and enabling session fixation.

## GlobalExceptionHandler ForbiddenException Shadowed by RuntimeException (High)
`GlobalExceptionHandler.java:14,24` — `handleRuntime(RuntimeException)` declared before `handleForbidden(ForbiddenException)`. Spring picks the more-specific handler correctly BUT `handleRuntime` returns HTTP 200 with success=false for all RuntimeExceptions, including "User not found" and "Invalid sort field" — these should be 4xx, not 200.

## No Input Validation on LoginRequest / UpdateTaskRequest (Medium)
Both DTOs lack `@Valid`/`@NotBlank`/`@NotNull` — null username/password accepted at login; null status string passed to `Status.valueOf()` causing NPE/500.

## N+1 Pattern in getTaskForCurrentUser (Medium)
`TaskServiceImpl.java:51-53` — every task operation (update, delete) fires one DB query for the user by email AND one for the task. Could be resolved with a single join query.

## Missing Database Indexes (Medium)
`Task.java` — no `@Index` on `userId` FK (high-cardinality filter in every query). `User.java` — email has `unique=true` (creates index) which is correct.

## Unused Variable `tasks` (Low)
`TaskServiceImpl.java:112` — `Page<Task> tasks;` declared but never assigned or used.

## Dead Code: `parseStatus` Method (Low)
`TaskServiceImpl.java:63-69` — `parseStatus()` is defined but never called anywhere.

## Stale anti-patterns to avoid re-flagging:
- Raw SQL in specifications (`SpecificationUtils`) uses JPA Criteria API — not SQL injection risk.
- DB credentials are env-var referenced in application.yml — not hardcoded.
- `show-sql=true` is a config concern, not a security bug (no passwords in queries).
