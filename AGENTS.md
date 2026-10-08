# AGENTS.md — Global AI Agent Rules for Orbin School

## Core Mandate: Zero Illusions, Zero Mock Data, Real Persistence

All AI agents working within this workspace MUST strictly follow the directives outlined in [`AGENT_PRODUCTION_DIRECTIVE.md`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/AGENT_PRODUCTION_DIRECTIVE.md).

### Non-Negotiable Agent Rules:
1. **No In-Memory Mock Arrays:** Do not use or introduce `INITIAL_STUDENTS`, `INITIAL_STAFF`, `INITIAL_FEES`, `INITIAL_MARKS`, `INITIAL_SYLLABUS`, or similar dummy data constants. All application state must originate from the Spring Boot backend via REST APIs.
2. **No `localStorage` Business State:** Do not store business entities (students, staff, fees, attendance) in browser `localStorage`. Use `localStorage` ONLY for auth session tokens (`orbin_access_token`, `orbin_refresh_token`).
3. **No Fake JWT Generation:** Never create mock JWTs (e.g. `orbin_jwt_...`) or fake client-side authenticated sessions. Login MUST authenticate against `/api/v1/auth/login`.
4. **Backend-First Implementation:** If the frontend needs data or an endpoint that does not exist in the backend, implement the backend Entity, Repository, Service, and Controller first. Never fake an endpoint in the frontend.
5. **Real Database Seeding:** System bootstrap must be handled via Flyway SQL migrations or Spring Boot data initializers inserting real records with BCrypt password hashes into PostgreSQL.
6. **Graceful Empty & Error States:** When no data exists in the database, render clean empty-state UI components with actionable "Create" / "Import" buttons. If an API call fails, display the real server error with a "Retry" button. Never silently fall back to mock data.
