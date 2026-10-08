# Orbin School — Production Frontend Architecture Plan

> **Platform:** Next.js 14 App Router + TypeScript + Tailwind CSS  
> **Backend Integration:** Java 21 / Spring Boot 3.3.5 Modular Monolith (`http://localhost:8080/api/v1`)  
> **Target Directory:** `orbin-frontend`

---

## 1. Architectural Principles (No Prototypes / No Mock Shortcuts)

1. **Strict Multi-Tenancy & Tenant Context:**
   - Every authenticated API request carries the JWT bearer token.
   - The token contains `school_id`, `sub` (user ID), and `roles`.
   - The frontend never allows cross-tenant leaks; school context is resolved once upon login or tenant resolution.

2. **Role-Based Access Control (RBAC) Route Groups:**
   - Next.js 14 App Router Route Groups cleanly isolate portals physically:
     - `app/(auth)`: Unauthenticated entry (`/login`, `/forgot-password`).
     - `app/(platform)`: Orbin SaaS Super Admin only (`/platform/schools`, `/platform/billing`, `/platform/modules`).
     - `app/(school)`: School staff portal (`/dashboard`, `/attendance`, `/students`, `/fees`, `/syllabus`, `/exams`).
     - `app/(public)`: Public school landing page & CMS (`/[slug]`, e.g., `/delhi-public`).

3. **True REST API Client with JWT Lifecycle:**
   - Interceptor attached to all `fetch` requests.
   - Automatically injects `Authorization: Bearer <accessToken>`.
   - On `401 Unauthorized`: transparently calls `POST /api/v1/auth/refresh` to rotate tokens. If refresh fails, cleans storage and redirects to `/login`.

4. **1-to-1 TypeScript DTO Alignment:**
   - TypeScript interfaces mirror the backend Spring Boot DTOs (`StudentResponse`, `AttendanceRecordDto`, `MarkAttendanceRequest`, `FeeDashboardDto`, `AuthResponse`, etc.).

---

## 2. Directory Structure

```text
orbin-frontend/
├── src/
│   ├── app/
│   │   ├── (auth)/
│   │   │   ├── login/
│   │   │   │   └── page.tsx           # Real JWT login hitting /api/v1/auth/login
│   │   │   └── layout.tsx             # Centered auth card layout
│   │   ├── (platform)/
│   │   │   ├── layout.tsx             # Super Admin navigation shell (guard: role === 'SUPER_ADMIN')
│   │   │   └── schools/
│   │   │       └── page.tsx           # Multi-tenant schools directory & provisioning
│   │   ├── (school)/
│   │   │   ├── layout.tsx             # School app shell (guard: school staff roles)
│   │   │   ├── dashboard/page.tsx     # School KPIs, today's schedule, absentees alert
│   │   │   ├── students/page.tsx      # Enrolled students directory, admissions modal
│   │   │   ├── attendance/page.tsx    # Section roll call sheet, 1-click status toggles
│   │   │   ├── fees/page.tsx          # Fee dues ledger, payment terminal, receipt printing
│   │   │   ├── syllabus/page.tsx      # Curriculum tracking, chapters & topics progress
│   │   │   └── exams/page.tsx         # Exam gradebook matrix & student report cards
│   │   ├── (public)/
│   │   │   └── [slug]/page.tsx        # Branded school website & admissions inquiry
│   │   ├── globals.css                # Tailwind directives & CSS custom property tokens
│   │   └── layout.tsx                 # Root HTML & body shell with AuthProvider
│   ├── components/
│   │   ├── ui/                        # Button, Card, Table, Modal, Input, Badge
│   │   ├── school/                    # AttendanceTable, FeeReceiptModal, ReportCardModal
│   │   └── platform/                  # SchoolOnboardingForm, ModuleToggleMatrix
│   ├── lib/
│   │   ├── api.ts                     # Type-safe Fetch HTTP client with JWT interceptor
│   │   ├── auth.ts                    # Token decoding & session persistence
│   │   └── types.ts                   # TypeScript interfaces matching backend Java DTOs
│   └── context/
│       └── auth-context.tsx           # React Context for currentUser, token, permissions
├── tailwind.config.ts
├── tsconfig.json
└── package.json
```

---

## 3. Detailed Phase-by-Phase Execution

| Phase | Milestone | Deliverables |
|---|---|---|
| **Phase 1** | **Project Scaffolding & Setup** | Initialize Next.js 14 App Router with TypeScript, Tailwind CSS, and Lucide icons. Configure path aliases (`@/*`). |
| **Phase 2** | **Core Type Definitions & API Client** | Create `types.ts` mirroring backend DTOs. Implement `lib/api.ts` with typed endpoints for `/auth`, `/academic`, `/students`, `/attendance`, `/fees`. |
| **Phase 3** | **Authentication & RBAC Layouts** | Build `AuthContext`, login page (`/login`), token storage, and route guard middleware. Create distinct layouts for `(platform)` and `(school)`. |
| **Phase 4** | **School Operations (Attendance & Students)** | Implement 1-Click attendance marking sheet hitting backend endpoints, student admissions form, and student directory with filtering. |
| **Phase 5** | **Fees & Printable Tax Receipts** | Implement fee dues ledger, payment collection modal, and formatted receipt preview with print stylesheets. |
| **Phase 6** | **Academics & Gradebook (Syllabus & Exams)** | Implement syllabus chapters checklist and marks entry matrix with student report cards. |
| **Phase 7** | **Platform Super Admin Console** | Implement multi-tenant onboarding form and tenant management directory. |

---

## 4. Verification & Testing Strategy

1. **Build Verification:** Run `npm run build` and `npm run lint` to guarantee zero TypeScript or syntax errors.
2. **End-to-End Auth Test:** Test login with mock/live credentials, verifying JWT decode, session restore on page refresh, and automatic redirection based on role.
3. **Responsive UI & Accessibility:** Test across desktop (1440px), tablet (1024px), and mobile (375px) viewports.
