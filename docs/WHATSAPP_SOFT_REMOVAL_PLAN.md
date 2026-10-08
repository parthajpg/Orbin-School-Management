# 📱 WhatsApp Module — Soft Removal & Decoupling Strategy

> **Status:** Planned (Not Yet Implemented)  
> **Date:** October 2026  
> **Target Module:** `com.orbin.school.whatsapp` & Frontend Communication Features  
> **Objective:** Completely disconnect WhatsApp operations, network dependencies, and runtime calls from Orbin School without destroying code, allowing the core application (Students, Staff, Classes, Attendance, Fees, Exams) to operate independently.

---

## 1. Executive Summary

Orbin School Management System includes a WhatsApp parent notification and two-way bot module backed by the Meta Cloud API. Because this requires active Meta credentials and phone number verification, running it in core development or standard school deployments creates unnecessary operational complexity.

The **Soft Removal Strategy** isolates the WhatsApp module:
- Preserves the codebase for future activation (Phase 2/3).
- Ensures no external HTTP calls are made to Meta.
- Prevents database or validation errors related to WhatsApp from stopping system startup.
- Keeps dependent business services (like Attendance) running cleanly without failure.

---

## 2. Backend Disconnection Plan

### Step A: Configuration Feature Toggle
In `orbin-backend/src/main/resources/application.yml`:
```yaml
whatsapp:
  enabled: false  # Default to false; turn on only when Meta credentials exist
  token: ${WHATSAPP_TOKEN:dummy_token}
  phone-number-id: ${WHATSAPP_PHONE_ID:0000000000}
  verify-token: ${WHATSAPP_VERIFY_TOKEN:dummy_verify}
```

### Step B: Service Guarding (No-Op Strategy)
In `com.orbin.school.whatsapp.service.WhatsAppService`:
- Inject `@Value("${whatsapp.enabled:false}") private boolean enabled;`.
- At the top of `sendAbsenceNotification(...)`, `sendFeeReminder(...)`, and `sendSingleMessage(...)`:
  ```java
  if (!enabled) {
      log.info("[WhatsApp Disabled] Skipping notification to: {}", recipientPhone);
      return;
  }
  ```
- **Result:** Calling services such as `AttendanceService` execute their primary database logic (saving attendance records) without throwing exceptions or attempting external HTTP connections.

### Step C: Controller Inactivation
In `com.orbin.school.whatsapp.controller.WhatsAppWebhookController`:
- Add Spring's condition annotation:
  ```java
  @RestController
  @RequestMapping("/api/v1/whatsapp/webhook")
  @ConditionalOnProperty(name = "whatsapp.enabled", havingValue = "true")
  public class WhatsAppWebhookController { ... }
  ```
- **Result:** The public webhook endpoint is not registered when the feature is disabled, reducing the attack surface.

---

## 3. Database & JPA Schema Alignment

Under Spring Boot's production configuration (`spring.jpa.hibernate.ddl-auto: validate`), Hibernate validates all `@Entity` classes at boot time regardless of whether the service is actively called.

To keep the database clean and prevent boot crashes:
1. **Schema Integrity:**
   - In `V4__whatsapp_logs.sql`, maintain `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()`.
   - This satisfies `BaseEntity` requirements so Hibernate passes startup validation in 0.2s without error.
2. **Zero Runtime Writes:**
   - Because `WhatsAppService` is short-circuited by `whatsapp.enabled: false`, the `whatsapp_logs` and `whatsapp_sessions` tables remain idle with zero read/write overhead.

---

## 4. Frontend UI Disconnection Plan

1. **Sidebar & Navigation:**
   - Audit `src/components/` and `src/app/(school)` for WhatsApp-specific links (e.g., Parent Bot, WhatsApp Settings).
   - Hide or remove the navigation items so users only see operational modules (Dashboard, Students, Staff, Classes, Attendance, Fees, Exams).
2. **Attendance Screen Actions:**
   - On `src/app/(school)/attendance/page.tsx`, ensure the "Notify Parents via WhatsApp" toggle is either hidden or marked as "Feature Optional / Inactive".
   - Submitting attendance will solely persist records to `/api/v1/attendance`.

---

## 5. How to Re-enable in the Future (Phase 2/3)

When the school or tenant is ready to connect their Meta Cloud API account:
1. Add environment variables:
   - `WHATSAPP_ENABLED=true`
   - `WHATSAPP_TOKEN=<meta_bearer_token>`
   - `WHATSAPP_PHONE_ID=<meta_phone_number_id>`
   - `WHATSAPP_VERIFY_TOKEN=<webhook_verification_secret>`
2. Enable the WhatsApp module toggle in the School Admin settings dashboard.
3. No code modifications or refactoring will be required.
