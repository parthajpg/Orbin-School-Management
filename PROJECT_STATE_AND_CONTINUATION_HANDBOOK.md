# 📘 ORBIN SCHOOL MANAGEMENT SYSTEM — MASTER CONTINUATION HANDBOOK
> **Purpose:** Persistent context document for developers and AI agents to resume work seamlessly across power cycles, system reboots, and new sessions without memory loss.  
> **Last Updated:** October 2026  
> **Repository Root:** `c:\Users\ParthasarathyE\Downloads\JavaPrograms\Orbin School Management`

---

## 1. PROJECT OVERVIEW & ARCHITECTURE

Orbin School is an **Enterprise Multi-Tenant SaaS School Management Platform** designed for K-12 institutions.

### Tech Stack
* **Backend:** Java 21, Spring Boot 3.3, Spring Security (JWT), Hibernate 6 / Spring Data JPA, Flyway migrations, PostgreSQL 16.
  * Directory: `orbin-backend/`
  * Default Port: `http://localhost:8080` (API Base: `http://localhost:8080/api/v1`)
  * Build Tool: Apache Maven (`mvn clean compile`, `mvn test`)
  * **JDK Note:** The project targets Java 21 (`<java.version>21</java.version>`). The Docker build uses `eclipse-temurin-21` which compiles cleanly. If compiling directly on the host with JDK 25+, use Java 21 or execute inside Docker container (`docker-compose build`).
* **Frontend:** Next.js 14.2 (App Router), React 18, TypeScript, Tailwind CSS, Lucide Icons.
  * Directory: `orbin-frontend/`
  * Default Port: `http://localhost:3000`
  * Package Manager: `npm run dev`, `npm run build`, `npx tsc --noEmit`
* **Database & Infrastructure:**
  * Docker Compose (`docker-compose.yml`): PostgreSQL 16 Alpine container, orbin-backend, orbin-frontend.
  * Database Name: `orbin_school_db`
  * Port: `5432`
  * User/Pass: `postgres` / `postgres`

---

## 2. RECENT FIXES COMPLETED IN THIS SESSION

1. **Backend Compilation Errors Resolved:**
   * **`StudentService.java`**: Added missing `import java.util.List;`.
   * **`ResourceNotFoundException.java` & `BusinessException.java`**: Added required single-parameter and multi-parameter constructors.
   * **`AuthService.java`**: Implemented missing `logoutAll(Long userId)` method to revoke all refresh tokens for a user.
   * **`PhoneNormalizer.java`**: Made `normalize()` and `normalizeOrNull()` static utility methods.
   * **`AuditService.java`**: Replaced illegal `.id(schoolId)` builder calls on `School` and `User` with proper entity references.
   * **`StudentRepository.java`**: Added `List<Student> findBySchoolId(Long schoolId)` query method.
   * **`StudentFeeRepository.java` & `FeeService.java` / `FeeController.java`**: Added `findBySchoolId(Long schoolId)` and exposed `GET /api/v1/fees/students`.

2. **Frontend Styling & CSS Pipeline:**
   * **`tailwind.config.ts`**: Corrected content glob paths to `["./src/**/*.{js,ts,jsx,tsx,mdx}"]`.
   * **`postcss.config.js`**: Created CommonJS configuration enabling `tailwindcss` and `autoprefixer`.

3. **Database Schema & Migration Fixes (Flyway & Hibernate):**
   * **`V5__row_level_security.sql` & `V7__harden_row_level_security.sql`**: Fixed table name mismatch: changed invalid table `fee_payments` to `payments`.
   * **`V1__initial_schema.sql`**: Added missing `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()` to tables `event_awards`, `parent_students`, and `gallery_items` to satisfy `BaseEntity` validation.

4. **Schema Validation Issue Identified & Fix Planned:**
   * **Issue:** During backend Docker startup with `jpa.hibernate.ddl-auto: validate`, Hibernate threw:
     `Schema-validation: missing column [updated_at] in table [whatsapp_logs]`
   * **Root Cause:** `WhatsAppLog.java` extends `BaseEntity` (requiring `updated_at`), but `V4__whatsapp_logs.sql` omitted the column.
   * **Audit Findings:** Verified all 34 backend JPA entities. All other entities matching `BaseEntity` have both `created_at` and `updated_at` columns present in `V1` and `V3`.
   * **Resolution Required:** Add `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()` to `V4__whatsapp_logs.sql`, recompile container image with `docker compose build backend`, and reset DB volume.

5. **WhatsApp Module Disconnection Strategy Documented:**
   * **Document:** Detailed architectural plan created in [`docs/WHATSAPP_SOFT_REMOVAL_PLAN.md`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/docs/WHATSAPP_SOFT_REMOVAL_PLAN.md).
   * **Scope:** Decouples Meta Cloud API calls, adds `whatsapp.enabled: false` toggle, no-ops notifications from `AttendanceService`, inactivates webhook endpoints, and keeps core school modules running independently without external credentials.

---

## 3. AUDIT OF CURRENT STATE & REMAINING PROBLEMS

While the codebase now compiles, the following critical items must be resolved to achieve full commercial production readiness according to [`AGENTS.md`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/AGENTS.md) and [`AGENT_PRODUCTION_DIRECTIVE.md`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/AGENT_PRODUCTION_DIRECTIVE.md):

### A. Frontend Mock Data Purge (Violates "Zero Illusions" Rule)
* **`src/context/auth-context.tsx`**:
  * Still contains `INITIAL_ACCOUNTS` array and fallback logic creating fake JWTs (`dummyJwt = orbin_jwt_...`) when backend connection fails.
  * **Fix Needed:** Delete fallback JWT generator and mock accounts. Connect strictly to `/api/v1/auth/login`. Display actionable error message on login failure.
* **`src/app/(school)/attendance/page.tsx`**:
  * Uses hardcoded `INITIAL_STUDENTS` and saves logs to `localStorage`.
  * **Fix Needed:** Fetch real student roster via `api.getStudents({ sectionId })` and submit via `api.markAttendance(...)`.
* **`src/app/(school)/fees/page.tsx`**:
  * Uses hardcoded `INITIAL_FEES` and saves state to `localStorage.setItem('orbin_fees_list', ...)`.
  * **Fix Needed:** Fetch via `api.getStudentFees()` and submit via `api.recordPayment(...)`.
* **`src/app/(school)/staff/page.tsx`**:
  * Uses hardcoded `INITIAL_STAFF` and writes to `localStorage`.
  * **Fix Needed:** Fetch via `api.getStaffList()` and create via `api.createStaff(...)`.
* **`src/app/(school)/students/page.tsx`**:
  * Uses hardcoded `INITIAL_STUDENTS` and `localStorage.setItem('orbin_students_list', ...)`.
  * **Fix Needed:** Fetch from `/api/v1/students` and wire real bulk import to `/api/v1/students/bulk`.
* **`src/app/(school)/exams/page.tsx` & `syllabus/page.tsx`**:
  * Rely on static mock datasets (`INITIAL_MARKS`, `INITIAL_SYLLABUS`).
  * **Fix Needed:** Connect to `/api/v1/exams` and `/api/v1/syllabus`.

### B. Backend Database Seeding
* Ensure PostgreSQL has seeded accounts with BCrypt password hashes via Flyway migration:
  * Super Admin: `parthasarathye2256@gmail.com` (Partha) / `Partha@2256`
  * School Admin: `admin@delhipublicacademy.edu` / `school123`
  * Teacher: `teacher@delhipublicacademy.edu` / `teacher123`

---

## 4. HOW TO RUN THE PROJECT (AFTER SYSTEM REBOOT)

When you turn on the computer, execute the following commands:

### Option A: Running via Docker Compose (Recommended full stack)
Open PowerShell in project root:
```powershell
cd "c:\Users\ParthasarathyE\Downloads\JavaPrograms\Orbin School Management"
docker-compose up -d
```
* Frontend available at: `http://localhost:3000`
* Backend available at: `http://localhost:8080`
* PostgreSQL available at: `localhost:5432`

---

### Option B: Running Locally (Native Dev Mode)

#### 1. Start PostgreSQL (Local or Docker)
If PostgreSQL is running in Docker:
```powershell
docker run --name orbin-postgres -e POSTGRES_DB=orbin_school_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16-alpine
```

#### 2. Start Spring Boot Backend
Open a terminal in `orbin-backend`:
```powershell
cd "c:\Users\ParthasarathyE\Downloads\JavaPrograms\Orbin School Management\orbin-backend"
mvn spring-boot:run
```
* Verifies Flyway migrations automatically.
* Runs on port `8080`.

#### 3. Start Next.js Frontend
Open a second terminal in `orbin-frontend`:
```powershell
cd "c:\Users\ParthasarathyE\Downloads\JavaPrograms\Orbin School Management\orbin-frontend"
npm run dev
```
* Runs on `http://localhost:3000`.

---

## 5. USER CREDENTIALS & DEMO ACCESS

| Portal / Role | Email | Password | Primary Purpose |
|---|---|---|---|
| **Platform Super Admin (Partha)** | `parthasarathye2256@gmail.com` | `Partha@2256` | Onboard schools, toggle tenant modules |
| **School Admin / HM** | `admin@delhipublicacademy.edu` | `school123` | Manage faculty, classes, school oversight |
| **Teacher / Faculty** | `teacher@delhipublicacademy.edu` | `teacher123` | 1-Click attendance, mark tests, syllabus |
| **Accountant** | `accounts@delhipublicacademy.edu` | `school123` | Fee collection terminal, 80C receipts |

---

## 6. PROJECT DIRECTIVES & MANDATES FOR FUTURE SESSIONS

All AI agents and developers working on this codebase must adhere to the following rules from [`AGENTS.md`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/AGENTS.md):
1. **Zero Illusions, Zero Mock Data:** Never re-introduce `INITIAL_*` arrays or `localStorage` data stores.
2. **Real Persistence:** Every state change must persist into PostgreSQL via Spring Boot REST APIs.
3. **No Fake JWTs:** Authentication must hit `/api/v1/auth/login`.
4. **Backend-First:** When frontend requires new data, implement the backend entity, repository, and controller first.
5. **Graceful Empty States:** Render clean UI empty states (e.g., *"No students enrolled yet. Click Admit Student"*) instead of falling back to mock data.

---

## 7. IMMEDIATE NEXT TASK CHECKLIST

When resuming work, proceed in this exact sequence:
- [ ] **Step 1:** Replace mock data in `src/context/auth-context.tsx` with pure live API calls to `/api/v1/auth/login`.
- [ ] **Step 2:** Refactor `src/app/(school)/students/page.tsx` to read from `api.getStudents()`.
- [ ] **Step 3:** Refactor `src/app/(school)/attendance/page.tsx` to load students dynamically from the selected section.
- [ ] **Step 4:** Refactor `src/app/(school)/fees/page.tsx` to read from `api.getStudentFees()` and post real transactions.
- [ ] **Step 5:** Refactor `src/app/(school)/staff/page.tsx` to read from `api.getStaffList()`.
- [ ] **Step 6:** Run `docker-compose up` and verify end-to-end data round-trip to PostgreSQL.
