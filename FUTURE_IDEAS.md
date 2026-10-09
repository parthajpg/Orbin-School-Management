# 🚀 ARCHITECTURAL ROADMAP & IMPLEMENTATION STATUS — ORBIN SCHOOL

> **Document:** `FUTURE_IDEAS.md`  
> **Repository:** Orbin School Management System (Multi-Tenant SaaS)  
> **Last Updated:** October 2026  
> **Target Status:** Zero-Mock, Real Relational Persistence, Multi-Tenant Production Platform  

---

## 📊 EXECUTIVE IMPLEMENTATION DASHBOARD

| Strategic Architecture / Module | Status | Backend / DB Implementation | Frontend Implementation |
|---|---|---|---|
| **Period Slots & Bell Schedules** | ✅ **IMPLEMENTED** | `period_slots` table, `PeriodSlot.java`, `GET /api/v1/academic/timetable/slots` | `/timetable` (Grid & Slot headers) |
| **Timetable Matrix & Storage** | ✅ **IMPLEMENTED** | `timetable_entries` table, `TimetableController.java`, RLS isolation | `/timetable` (Interactive Weekly Grid) |
| **Real-Time Collision Detection** | ✅ **IMPLEMENTED** | `TimetableService.java` (Teacher & Section collision checks) | Handled via API validation alerts |
| **Smart Proxy & Substitution** | ✅ **IMPLEMENTED** | `POST /academic/timetable/{id}/substitute`, `original_teacher_id` | Modal in `/timetable` (Assign Proxy) |
| **Teacher Personal Cockpit** | ✅ **IMPLEMENTED** | `GET /academic/timetable/my-schedule`, `TimetableService` | `/timetable` (Tab: "My Daily Cockpit") |
| **Full School Tiers (Nursery - 10)** | ✅ **IMPLEMENTED** | `V9__nursery_primary_and_timetable.sql` (Nursery, LKG, UKG, Classes 1-10) | Section filters across all pages |
| **Live Exam Gradebook Desk** | ✅ **IMPLEMENTED** | `POST /exams`, `POST /exams/marks`, `GET /exams/section/{id}` | `/exams` (Live marks entry & commit) |
| **Official Student Statement of Marks** | ✅ **IMPLEMENTED** | `TestDto`, `ResultDto`, backend test aggregations | Printable Report Card Modal in `/exams` |
| **Digital Homework & Diary** | 🔄 **PARTIAL (Backend Only)** | `com.orbin.school.homework` (Entity, Controller, DTOs) | ⏳ Frontend UI page pending |
| **Dynamic Attendance % Engine** | 🔄 **PARTIALLY IMPLEMENTED** | `AttendanceService.getStudentStats()` | Roll-call register in `/attendance` |
| **Role-Decoupled Attendance Cockpit** | 🔄 **PARTIALLY IMPLEMENTED** | Attendance mark & batch WhatsApp alerts | ⏳ Dedicated HM morning checklist pending |
| **Single HM Master Onboarding Sheet** | ⏳ **PLANNED** | Domain-specific bulk endpoints (`/students/bulk`, `/timetable/commit-import`) | Separate CSV uploaders |
| **Universal 360° Student Dossier** | ⏳ **PLANNED** | Dispersed across `students`, `attendance`, `fees`, `exams` | ⏳ Unified modal/page pending |
| **Dynamic Leaderboard & Ranks** | ⏳ **PLANNED** | Database stores raw test marks | ⏳ Live position shift & badges pending |
| **Algorithmic CSP Auto-Scheduler** | ⏳ **PLANNED (Tier 2)** | Manual grid + collision detection working | ⏳ Constraint solver pending |
| **Bus Transport, Library, PTM** | ⏳ **PLANNED** | Domain categories defined | ⏳ Full module lifecycle pending |

---

## 1. MASTER SCHOOL LIFECYCLE: ONBOARDING, STAFF RESPONSIBILITY & 360° VISIBILITY

```
┌───────────────────────────┐
│     1. SUPER ADMIN        │  Provisions Tenant School & creates Headmaster (HM) master login.
└─────────────┬─────────────┘
              │
              ▼
┌───────────────────────────┐
│     2. HEADMASTER (HM)    │  Uploads Master Sheet (Classes, Sections, Staff, Timings, Rooms)
│     INITIAL ONBOARDING    │  or inputs via rapid form wizard.
└─────────────┬─────────────┘
              │
              ├────────────────────────────────────────────────────────────────────────┐
              ▼                                                                        ▼
┌───────────────────────────┐                                            ┌───────────────────────────┐
│ 3. AUTO-GENERATED STAFF   │                                            │ 4. CONTINUOUS ENGINE      │
│    ACCOUNTS & ROLES       │                                            │                           │
│ • Unique Staff IDs issued │                                            │ • Live Attendance %       │
│ • Staff log in to portal  │                                            │ • Dynamic Exam Aggregation│
│ • Daily Attendance duty   │                                            │ • Defaulter Alerts (<75%) │
│ • Subject Marks entry     │                                            │                           │
└─────────────┬─────────────┘                                            └─────────────┬─────────────┘
              │                                                                        │
              └───────────────────────────────────┬────────────────────────────────────┘
                                                  │
                                                  ▼
                               ┌─────────────────────────────────────┐
                               │ 5. UNIVERSAL 360° STUDENT PROFILE   │
                               │    (Shared Staff & HM Transparency) │
                               │ • Live Attendance % & Full Ledger   │
                               │ • Test Marks & Performance Trends   │
                               │ • Parent Details & Medical Info     │
                               └─────────────────────────────────────┘
```

### A. Stage 1: Super Admin Tenant Provisioning `[✅ IMPLEMENTED]`
* Super Admin boards the new institution: School Name, Tenant ID, Board (CBSE/ICSE/State), and Academic Year.
* System generates the primary School Admin / Headmaster (HM) root credentials.
* **Code Implementation:** [`app/(platform)/schools/page.tsx`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-frontend/src/app/(platform)/schools/page.tsx) and [`PlatformSchoolController.java`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-backend/src/main/java/com/orbin/school/platform/controller/PlatformSchoolController.java).

### B. Stage 2: HM Master Onboarding `[⏳ PLANNED — PARTIALLY SERVED VIA MODULAR BULK IMPORTS]`
When the HM logs in for the first time, they will be prompted to set up their institutional structure in one step:
1. **Master Excel / CSV Sheet Upload:**
   * Single master sheet: `[Class_Grade | Section | Staff_Name | Email | Phone | Employee_Code | Room_No | Shift_Timing]`.
2. **Current Interim Implementation:**
   * School classes and sections (Nursery to 10) are seeded in PostgreSQL via [`V9__nursery_primary_and_timetable.sql`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-backend/src/main/resources/db/migration/V9__nursery_primary_and_timetable.sql).
   * Student admissions are handled via bulk CSV import in [`/students`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-frontend/src/app/(school)/students/page.tsx) (`/api/v1/students/bulk`).
   * Timetable schedule is imported via CSV in [`/timetable`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-frontend/src/app/(school)/timetable/page.tsx) (`/api/v1/academic/timetable/commit-import`).
   * *Next Step:* Unify these pipelines into a single-pass onboarding wizard.

### C. Stage 3: Staff Autonomy & Daily Operational Ownership `[✅ IMPLEMENTED]`
* **Classroom Attendance Duty:** Assigned Class Teachers mark roll call via [`/attendance`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-frontend/src/app/(school)/attendance/page.tsx), which commits directly to `/api/v1/attendance/mark`.
* **Subject Marks Duty:** Subject teachers record and submit scores via the live gradebook in [`/exams`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-frontend/src/app/(school)/exams/page.tsx) (`/api/v1/exams/marks`).

### D. Stage 4: Always-Calculated Live Attendance Percentage Engine `[🔄 PARTIALLY IMPLEMENTED]`
Attendance is recalculated dynamically:
$$\text{Attendance Percentage} = \left( \frac{\text{Total Days Present} + 0.5 \times \text{Half Days}}{\text{Total School Working Days Held to Date}} \right) \times 100$$
* **Current Implementation:** Backend [`AttendanceService.getStudentStats()`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-backend/src/main/java/com/orbin/school/attendance/service/AttendanceService.java#L98-L106) calculates exact attendance rates directly from PostgreSQL.
* **Next Step:** Implement automated threshold alerts:
  * 🟢 **Good Standing:** $\ge 85\%$
  * 🟡 **Attendance Warning:** $75\% - 84.9\%$
  * 🔴 **Critical / Defaulter:** $< 75\%$ (automatic notification dispatch to parents and exam hall-ticket block).

### E. Stage 5: Universal 360° Student Dossier `[⏳ PLANNED]`
Unified screen consolidating:
* Live attendance ledger & trendline.
* Multi-subject test progression and class rankings.
* Parent contacts, emergency phone numbers, and blood group.
* Faculty behavioral notes and extracurricular achievements.

---

## 2. TEACHER ALLOCATION & CLASSROOM GOVERNANCE

### A. The Homeroom / Class Teacher Model (Pre-K to Grade 5) `[🔄 PARTIALLY IMPLEMENTED]`
* **Operational Reality:** Early childhood and primary classes stay with a single dedicated primary teacher throughout the day.
* **Current Implementation:**
  * `Staff` entity contains `assignedClass` and `assignedSection` ([`Staff.java`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-backend/src/main/java/com/orbin/school/staff/entity/Staff.java#L61-L68)).
  * `TimetableEntry` supports `subject_id = null` specifically for homeroom periods where a single teacher conducts all core learning.
* **Next Step:** Introduce an explicit `assignment_type: HOMEROOM | SPECIALIST` enum on `teacher_assignments`.

### B. The Subject-Specialist Departmental Model (Grades 6 to 12) `[✅ IMPLEMENTED]`
* Multi-dimensional binding: $\text{Teacher} \longleftrightarrow \text{Subject} \longleftrightarrow \text{Class/Section}$.
* Handled in `teacher_assignments` and enforced via `timetable_entries`.
* *Next Step:* Workload load capping (e.g., flagging $> 30$ periods/week).

---

## 3. TIMETABLE & SCHEDULING ENGINE `[✅ TIER 1 FULLY IMPLEMENTED]`

### A. Core Schema Architecture `[✅ IMPLEMENTED]`
1. **Bell Schedules & Period Slots (`period_slots`):**
   * Configurable per shift: defines slot number, name, start time, end time, and break flags.
   * Defined in [`V9__nursery_primary_and_timetable.sql`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-backend/src/main/resources/db/migration/V9__nursery_primary_and_timetable.sql), managed via [`TimetableController.java`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-backend/src/main/java/com/orbin/school/academic/controller/TimetableController.java).
2. **Timetable Matrix (`timetable_entries`):**
   * Relational binding: `(school_id, academic_year_id, section_id, day_of_week, period_slot_id)` $\longrightarrow$ `(subject_id, teacher_id, room_number)`.
   * Enforces PostgreSQL Row-Level Security (RLS) across all tenant queries.

### B. Tier 1: Interactive Grid with Conflict Detection `[✅ IMPLEMENTED]`
* **Interactive Weekly Matrix:** Full visual schedule grid on [`/timetable`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-frontend/src/app/(school)/timetable/page.tsx).
* **Collision Detection Engine:**
  * **Teacher Collision:** Checks if teacher is assigned to another class in the same slot and throws `DuplicateResourceException` ([`TimetableService.java:116-124`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-backend/src/main/java/com/orbin/school/academic/service/TimetableService.java#L116-L124)).
  * **Section Slot Clash:** Prevents double-booking a section slot.
* **Smart Substitution & Proxy Manager:**
  * Implemented via `POST /api/v1/academic/timetable/{id}/substitute`.
  * Assigns proxy coverage while preserving `original_teacher_id` and setting `is_substitution = true`.
* **Teacher Personal Schedule:**
  * Live timeline of the teacher's daily periods via `GET /api/v1/academic/timetable/my-schedule` ("My Daily Cockpit" tab in `/timetable`).

### C. Tier 2: Algorithmic CSP Auto-Scheduler `[⏳ PLANNED]`
* Automated constraint satisfaction solver to optimize period distribution based on weekly subject quotas and cognitive load.

---

## 4. REAL-TIME EXAM GRADEBOOK & DYNAMIC LEADERBOARD

### A. Live Marks Entry & Persistence `[✅ IMPLEMENTED]`
* **Eliminated Mock Approximations:** Purged hardcoded `85 + (idx % 12)` formulas.
* **Faculty Gradebook Flow:**
  * Schedule new test via modal hitting `POST /api/v1/exams`.
  * Select scheduled tests and enter student scores (`0` to `maxMarks`).
  * Commit marks batch directly to PostgreSQL via `POST /api/v1/exams/marks`.
  * View official printable Statement of Marks / Report Card modal based on actual recorded scores.
* **Code Implementation:** [`app/(school)/exams/page.tsx`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-frontend/src/app/(school)/exams/page.tsx) and [`ExamController.java`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-backend/src/main/java/com/orbin/school/exam/controller/ExamController.java).

### B. Dynamic Leaderboard & Completion Heatmap `[⏳ PLANNED]`
* **Section & Grade Leaderboard:** Top rank holders in sections and combined batches.
* **Subject Toppers:** Highlighting the highest score per subject.
* **Subject Completion Heatmap (HM View):** Track departments that have finished entering marks versus pending teachers.
* **Holistic Recognition:** "Most Improved Student" and "Effort & Consistency" awards.

---

## 5. ROLE-DECOUPLED ATTENDANCE: ROLL CALL VS. HM MORNING COCKPIT

### A. Classroom Staff Roll Call `[✅ IMPLEMENTED]`
* 1-Click roll-call status toggles ("Mark All Present", absent toggles) in [`/attendance`](file:///c:/Users/ParthasarathyE/Downloads/JavaPrograms/Orbin%20School%20Management/orbin-frontend/src/app/(school)/attendance/page.tsx).
* Direct persistence to `/api/v1/attendance/mark`.
* Automated WhatsApp absence alerts via `/api/v1/whatsapp/send-absence-blast`.

### B. Headmaster (HM) Morning Cockpit `[⏳ PLANNED]`
* **Live Class Submission Tracker:** Real-time checklist of all sections in the school (*Class 5-A: Submitted*, *Class 8-A: Pending* with a *"Nudge Teacher"* button).
* **Late Arrival & Front-Office Overrides:** Gate clerk overrides without interrupting ongoing lectures.
* **Parent Leave Approvals:** Pre-planned leave applications automatically populating on teacher roll calls.

---

## 6. ADDITIONAL STRATEGIC FUTURE EXPANSIONS

1. **Digital Student Diary & Homework Broadcast `[🔄 BACKEND IMPLEMENTED, UI PENDING]`**
   * Fully implemented in Spring Boot backend (`com.orbin.school.homework`: `Homework.java`, `HomeworkSubmission.java`, `HomeworkController.java`).
   * *Next Step:* Build dedicated frontend page under `orbin-frontend/src/app/(school)/homework/`.
2. **Multi-Tier Fee Concessions & Scholarships `[⏳ PLANNED]`**
   * Custom concession rules (Sibling discounts, Staff child concessions) with automated ledger splits.
3. **Integrated Bus Transport & Route Tracking `[⏳ PLANNED]`**
   * Fleet routes, stops, vehicle capacities, driver assignments, and emergency route broadcasts.
4. **Library & Inventory Management `[⏳ PLANNED]`**
   * ISBN scanning, book issue/return tracking, overdue fine ledgers, and laboratory equipment tracking.
5. **Parent-Teacher Meeting (PTM) Slot Booking `[⏳ PLANNED]`**
   * 10-minute slot scheduler with individual subject teachers.

---

## 7. ARCHITECTURAL MANDATES ENFORCED

In accordance with [`AGENT_PRODUCTION_DIRECTIVE.md`](./AGENT_PRODUCTION_DIRECTIVE.md) and [`AGENTS.md`](./AGENTS.md):
* **Zero Mock Data:** All application entities (timetables, exams, results, staff, attendance, fees) round-trip directly to PostgreSQL.
* **Strict Multi-Tenancy:** Row-Level Security (RLS) policies enforce `school_id` isolation at the database engine level.
* **Clean Empty & Error States:** All UI views render actionable empty states and specific server error banners with retry triggers.
