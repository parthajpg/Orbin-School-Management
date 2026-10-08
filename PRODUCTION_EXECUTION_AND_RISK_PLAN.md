# Orbin School — Real-Time Production Execution & Risk Mitigation Master Plan

> **Document Type:** Production Readiness & Engineering Execution Blueprint  
> **Target Status:** Zero-Demo, Enterprise Multi-Tenant SaaS for Real-World School Deployments  
> **Last Updated:** October 2026  
> **Author:** Production Engineering Team

---

## 1. Executive Mission: Transitioning from "Demo" to "Real-World Deployment"

School operations are mission-critical. A failure in a consumer app causes annoyance; a failure in a school management system causes:
- Discrepancies in government tax filings (80C tuition certificates, GST).
- Unaccounted cash/cheque fee collections leading to theft allegations.
- Parent panic when student absence alerts are delayed or misrouted.
- Breaches of student data privacy (POSH, Child Privacy Acts, COPPA/FERPA compliance).

This master plan establishes the non-negotiable operational requirements, architectural risk mitigations, and execution phases to turn **Orbin School** into a hardened, production-ready platform that can be deployed to paying schools with zero reliance on mock data or demo shortcuts.

---

## 2. Production Risk Matrix & Mitigation Architecture

| Risk Category | Real-World Scenario | Impact Severity | Root Cause in Typical MVPs | Production Architecture Defense |
|---|---|---|---|---|
| **Data Isolation Breach** | School A's accountant queries `/api/v1/students` and receives student data from School B due to a missing filter in custom SQL. | **CRITICAL (Catastrophic)** | Relying solely on developers manually writing `schoolId` filters in JPA queries. | **PostgreSQL Row-Level Security (RLS)** + Hibernate 6 `@TenantId` automatic query rewriting. |
| **Financial Inconsistency** | A teacher collects ₹15,000 cash; network drops during submission; double-clicking generates two receipts or corrupts ledger balance. | **CRITICAL** | Non-idempotent endpoints, missing DB transactions (`@Transactional`), and lack of audit ledgers. | **Idempotency Keys** on payment APIs, sequential DB sequence numbering per school, and append-only financial ledger tables. |
| **Day-1 Onboarding Churn** | School has 1,800 students. The admin is faced with manual student-by-student data entry and cancels the pilot. | **HIGH (Commercial Failure)** | No automated bulk ingest pipeline. | **Excel/CSV Bulk Importer with Apache POI**, schema auto-mapping, dry-run validation, and rollback on error. |
| **Silent Production Failure** | A new update breaks marks aggregation for CBSE report cards. The principal discovers it on report card distribution day. | **HIGH** | Zero automated tests in CI/CD pipeline. | **Testcontainers PostgreSQL integration suite** covering all accounting, attendance, and grading calculators before merge. |
| **WhatsApp Rate Limiting & Cost Shock** | A morning absence blast triggers 500 WhatsApp messages simultaneously, tripping Meta API rate limits or incurring runaway billing. | **MEDIUM / HIGH** | Synchronous REST calls to Meta API inside the HTTP request cycle. | **Asynchronous Queue (RabbitMQ / Redis / Spring Task Executor)** with rate limiters and template pre-validation. |
| **Unsaved Offline Work** | A teacher marks attendance in a basement classroom without Wi-Fi; the page refreshes and all records are lost. | **MEDIUM** | Frontend relies solely on active network connection. | **IndexedDB / Service Worker offline queue** that syncs automatically when connection restores. |

---

## 3. Real-World User Personas & Strict Role Boundaries

In a real school, each staff member operates within strict silos. No user must ever see tools outside their job description:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        ORBIN PLATFORM ROLES                            │
├────────────────────┬───────────────────────────────────────────────────┤
│ SUPER_ADMIN        │ Multi-tenant provisioning, subscription billing   │
├────────────────────┼───────────────────────────────────────────────────┤
│ PRINCIPAL / HM     │ Full school governance, staff management, stats   │
│ SCHOOL_ADMIN       │ Day-to-day operations, academic setup, admissions │
│ ACCOUNTANT         │ Fee collection, receipt generation, dues ledgers  │
│ TEACHER / FACULTY  │ Attendance, homework, syllabus, marks entry       │
│ PARENT / GUARDIAN  │ View child's attendance, fees, report cards, chat │
│ STUDENT            │ View homework, syllabus progress, test schedule   │
└────────────────────┴───────────────────────────────────────────────────┘
```

### The Headmaster (HM) / Principal Governance Cycle:
1. **Term Setup:** Configure Academic Year, Terms (Term 1, Term 2, Finals).
2. **Staff Provisioning:** Add teachers, assign Employee IDs, and designate **Class Teachers** (e.g., "Mrs. Anita Rao $\rightarrow$ Class 6-B").
3. **Subject Allocation:** Allocate subjects to teachers (e.g., "Mr. Sharma $\rightarrow$ Class 8, 9, 10 Science").
4. **Oversight:** Monitor daily school-wide attendance percentage, uncollected fees, and syllabus completion velocity per subject.

### The Teacher Daily Operational Cycle:
1. **Morning Roll Call (8:15 AM):** Opens `/attendance`. The system defaults to their **assigned class/section**. With 1 click ("Mark All Present"), they toggle only the 2 absentees and hit Save.
2. **Trigger Alert:** System instantly dispatches WhatsApp messages to the parents of the 2 absentees.
3. **Classroom Teaching:** Opens `/syllabus`, checks off "Chapter 4: Linear Equations - Topic 2 Completed".
4. **Exams / Test Entry:** Inputs marks for their subject. The system auto-calculates percentages and grades according to board criteria (CBSE/ICSE/State).

---

## 4. Phase-by-Phase Production Roadmap

### Milestone 1: Staff & Faculty Directory + Credentials Engine (COMPLETED)
- **Database:** `staff` table defined in `V3__staff_management.sql` linking `user_id`, `school_id`, `employee_id`, `designation`, `assigned_class_id`, `assigned_section_id`, `qualification`, `date_of_joining`, `status`.
- **Backend:** `com.orbin.school.staff` entity, repository, service, DTOs, and REST controller (`/api/v1/staff`).
- **Frontend:** `/staff` page with faculty directory, role filter, class-teacher allotment, automated credentials generator, WhatsApp invitation sharing, and password reset modal.

### Milestone 2: Excel / CSV Bulk Data Ingestion Pipeline (COMPLETED)
- **Client Pipeline:** Built in `/students` with automated download of official `.csv` upload template.
- **Dry-Run Validation Matrix:** Detects duplicate admission numbers against existing roster and within the file; flags missing fields before committing.
- **Roster Export:** 1-click export of the entire enrolled student roster to CSV.

### Milestone 3: Financial Integrity & Legal Fee Receipts (COMPLETED)
- **Receipt Engine:** Official Tax / 80C Tuition Fee Receipt with school branding, board affiliation, and sequential numbers (`RCP-2026-XXXX`).
- **Payment Collection Terminal:** Supports UPI (with UTR/Txn ID), Cash, Cheque/DD (with Cheque No & Bank Branch), and Net Banking.
- **Audit Ledger:** Dynamic calculation of total demanded, realized, and outstanding balances with 1-click CSV export for school accountants.

### Milestone 4: WhatsApp Transactional Messaging Engine (COMPLETED)
- **Database:** `whatsapp_logs` table defined in `V4__whatsapp_logs.sql` tracking `recipient_name`, `recipient_phone`, `student_name`, `template_type`, `meta_message_id`, and delivery timestamps.
- **Backend Service:** `WhatsAppService.java` with batch absence blast dispatcher (`/api/v1/whatsapp/send-absence-blast`) and audit retrieval (`/api/v1/whatsapp/logs`).
- **Frontend Roll-Call Integration:** In `/attendance`, saving attendance with absent students automatically prompts the Automated WhatsApp Broadcast modal, gives teachers 1-click preview of parent numbers, triggers batch delivery, provides direct `wa.me` links, and maintains a persistent delivery audit drawer.

### Milestone 5: Database-Enforced Tenant Isolation (PostgreSQL RLS) (COMPLETED)
- **Database Migration:** Defined in `V5__row_level_security.sql` enabling Row-Level Security across `students`, `staff`, `attendance`, `student_fees`, `fee_payments`, and `whatsapp_logs`.
- **Policy Enforcement:** Enforces `school_id = current_setting('app.current_school_id')` so cross-tenant queries return empty sets at the database engine level, eliminating developer-error leakage risks.

### Milestone 6: Automated Test Verification Harness
### Milestone 6: 1-Command Production Containerization & Cloud Deployment (COMPLETED)
- **Backend Dockerfile:** Multi-stage build (`maven:3.9.6-eclipse-temurin-21-alpine` -> unprivileged `eclipse-temurin:21-jre-alpine` runtime) optimized with G1GC flags.
- **Frontend Dockerfile:** Multi-stage standalone Next.js image (~100MB) with unprivileged user execution.
- **Docker Compose:** Production stack definition in `docker-compose.yml` orchestrating `postgres:16-alpine` (with healthcheck and persistent volumes), `orbin-backend` (port 8080), and `orbin-frontend` (port 3000).

---

## 5. Production Deployment Architecture

```
                                 [ Cloudflare CDN & DNS ]
                                            │
                       ┌────────────────────┴────────────────────┐
                       ▼                                         ▼
            [ Next.js 14 Frontend ]                  [ Spring Boot 3 API ]
            (Vercel / Docker Alpine)                 (Render / AWS ECS / Fargate)
                       │                                         │
                       │                                         ▼
                       │                             [ PostgreSQL 16 (Primary) ]
                       │                             (Neon / Supabase / AWS RDS)
                       │                                         │
                       ▼                                         ▼
            [ HTTPS / WSS API calls ]                 [ S3 / Cloudflare R2 ]
                                                      (Receipts, Photos, Docs)
```

---

## 6. Real-Time Production Readiness Scorecard

| Milestone | Component | Status | Production Deliverable |
|---|---|---|---|
| **M1** | Staff & Faculty Management | ✅ **COMPLETED** | `/staff` portal + `V3__staff_management.sql` + Credentials Generator |
| **M2** | Bulk Spreadsheet Ingestion | ✅ **COMPLETED** | `/students` dropzone + Template Download + Dry-Run Validator + Roster CSV Export |
| **M3** | Legal 80C Tax Receipts | ✅ **COMPLETED** | `/fees` terminal + UTR/Cheque reference + Printable 80C Tax Invoice + Ledger Export |
| **M4** | WhatsApp Absence Alerts | ✅ **COMPLETED** | `/attendance` roll-call + Parent Alert Dispatcher + `V4__whatsapp_logs.sql` |
| **M5** | Database Row-Level Security | ✅ **COMPLETED** | `V5__row_level_security.sql` tenant shielding on PostgreSQL engine |
| **M6** | Containerized 1-Command Stack | ✅ **COMPLETED** | Multi-stage Dockerfiles + `docker-compose.yml` |

