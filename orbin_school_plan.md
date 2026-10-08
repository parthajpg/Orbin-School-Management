# Orbin School — Master Implementation Plan

## Architecture Summary

```
ONE CODEBASE → MANY SCHOOLS (Multi-Tenant SaaS)

Frontend (Next.js 14 + TypeScript + Tailwind CSS)
  └── Vercel

Backend (Java 21 + Spring Boot 3 + Spring Security)
  └── Render Paid Web Service

Database (PostgreSQL 16)
  └── Managed PostgreSQL (Render / Neon / Supabase)

Object Storage (Cloudflare R2 / AWS S3)
WhatsApp (Meta Cloud API)
```

---

## Repository Layout

```
orbin-school/
├── orbin-backend/          ← Spring Boot modular monolith
│   ├── src/main/java/com/orbin/school/
│   │   ├── auth/
│   │   ├── security/
│   │   ├── tenant/
│   │   ├── platform/
│   │   ├── school/
│   │   ├── user/
│   │   ├── role/
│   │   ├── academic/
│   │   ├── student/
│   │   ├── parent/
│   │   ├── staff/
│   │   ├── attendance/
│   │   ├── fee/
│   │   ├── syllabus/
│   │   ├── homework/
│   │   ├── material/
│   │   ├── test/
│   │   ├── achievement/
│   │   ├── announcement/
│   │   ├── event/
│   │   ├── website/
│   │   ├── whatsapp/
│   │   ├── notification/
│   │   ├── storage/
│   │   ├── audit/
│   │   ├── reporting/
│   │   └── common/
│   └── src/main/resources/
│       └── db/migration/   ← Flyway SQL files
│
└── orbin-frontend/         ← Next.js 14 App Router
    ├── app/
    │   ├── (public)/       ← Public school websites
    │   ├── (auth)/         ← Login / forgot password
    │   ├── (school)/       ← School app (teacher/admin/principal)
    │   └── (platform)/     ← Orbin super admin
    ├── components/
    │   ├── ui/             ← Design system components
    │   ├── public/
    │   ├── school/
    │   └── platform/
    └── lib/
```

---

## Phase Checklist

| Phase | Description | Status |
|-------|-------------|--------|
| 1 | Architecture + Project Skeleton | 🔄 In Progress |
| 2 | Database Schema + Flyway | ⏳ Pending |
| 3 | Authentication + Refresh Tokens | ⏳ Pending |
| 4 | Tenant Context + Isolation | ⏳ Pending |
| 5 | Roles + Permissions (RBAC) | ⏳ Pending |
| 6 | School + Academic Year + Classes + Sections + Subjects | ⏳ Pending |
| 7 | Students + Parents + Staff | ⏳ Pending |
| 8 | Attendance | ⏳ Pending |
| 9 | Fees + Payments + Receipts | ⏳ Pending |
| 10 | Syllabus Management | ⏳ Pending |
| 11 | Homework + Materials | ⏳ Pending |
| 12 | Tests + Results | ⏳ Pending |
| 13 | Achievements + Events + Announcements | ⏳ Pending |
| 14 | Public School Website CMS | ⏳ Pending |
| 15 | WhatsApp Parent Bot | ⏳ Pending |
| 16 | Audit + Security Hardening | ⏳ Pending |
| 17 | Frontend Polish | ⏳ Pending |
| 18 | Automated Tests | ⏳ Pending |
| 19 | Load Testing | ⏳ Pending |
| 20 | Production Deployment | ⏳ Pending |

---

## Key Security Rules

1. **Never trust frontend-supplied schoolId** — always resolve from JWT
2. **Every query must be scoped to the resolved school_id**
3. **Tenant isolation tests are mandatory** before declaring any feature complete
4. **All sensitive ops must write to audit_logs**
5. **No stack traces in production responses**
6. **Refresh token rotation + revocation on logout**
7. **BCrypt or Argon2 for passwords**
