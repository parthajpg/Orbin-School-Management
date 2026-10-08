# Orbin School Management System — API Contracts

## Base URL
`/api/v1`

## Standard Response Wrapper
All endpoints return JSON responses conforming to `ApiResponse<T>`:
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed successfully",
  "timestamp": "2026-10-08T11:00:00Z"
}
```
Errors return:
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_FAILED | UNAUTHORIZED | NOT_FOUND | INTERNAL_ERROR",
    "message": "Detailed error message",
    "details": { ... }
  },
  "timestamp": "2026-10-08T11:00:00Z"
}
```

---

## 1. Authentication & Multi-Tenancy (`/auth`)

### `POST /auth/login`
- **Request**:
  ```json
  { "email": "admin@school.edu", "password": "Password@123" }
  ```
- **Response**:
  ```json
  {
    "accessToken": "ey...",
    "refreshToken": "ey...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userId": 1,
    "schoolId": 1,
    "email": "admin@school.edu",
    "fullName": "Partha Admin",
    "roles": ["ORBIN_ADMIN"]
  }
  ```

### `POST /auth/refresh`
- **Request**:
  ```json
  { "refreshToken": "ey..." }
  ```
- **Response**: Refreshed `accessToken`.

---

## 2. Schools Directory & Onboarding (`/schools`)

### `GET /schools`
- Returns all tenant schools for Platform Super Admin.
- Returns current school for tenant-bound users.

### `GET /schools/current`
- Returns active school metadata for authenticated user.

### `POST /schools`
- Onboards a new tenant school.
- **Request**:
  ```json
  {
    "name": "Heritage Academy",
    "slug": "heritage",
    "board": "CBSE",
    "city": "Bengaluru",
    "branding": {
      "primaryColor": "#2563eb",
      "secondaryColor": "#1d4ed8",
      "accentColor": "#38bdf8"
    }
  }
  ```

---

## 3. Academic Structure (`/academic`)

- `GET /academic/years` — Fetch academic sessions.
- `GET /academic/classes` — Fetch classes.
- `GET /academic/classes/{classId}/sections` — Fetch sections for class.
- `GET /academic/subjects` — Fetch subjects for class.

---

## 4. Student Management (`/students`)

### `GET /students`
- Query params: `classId`, `sectionId`, `query`, `page`, `size`.
- Returns paginated or list of `StudentResponse`.

### `POST /students`
- Enrolls a new student.
- **Request**:
  ```json
  {
    "firstName": "Aarav",
    "lastName": "Sharma",
    "gender": "MALE",
    "dob": "2015-05-12",
    "classId": 1,
    "sectionId": 1,
    "parentName": "Rajesh Sharma",
    "parentPhone": "+919876543210",
    "parentEmail": "parent@example.com"
  }
  ```

---

## 5. Attendance & Roll Call (`/attendance`)

### `GET /attendance/section/{sectionId}?date=YYYY-MM-DD`
- Returns attendance records for students in section on date.

### `POST /attendance/mark`
- Persists section roll call.
- **Request**:
  ```json
  {
    "sectionId": 1,
    "date": "2026-10-08",
    "entries": [
      { "studentId": 101, "status": "PRESENT" },
      { "studentId": 102, "status": "ABSENT", "notes": "Sick leave" }
    ]
  }
  ```

### `GET /attendance/section/{sectionId}/absent?date=YYYY-MM-DD`
- Returns absent students for automated WhatsApp alert dispatch.

---

## 6. Fees & Receipts (`/fees`)

### `GET /fees/students`
- Returns all student fee ledger records.

### `GET /fees/dashboard`
- Returns financial metrics: `totalExpected`, `totalCollected`, `totalOutstanding`, `overdueStudents`.

### `POST /fees/payments`
- Records payment and generates receipt.
- **Request**:
  ```json
  {
    "studentFeeId": 1,
    "amount": 25000.00,
    "paymentDate": "2026-10-08",
    "paymentMethod": "UPI",
    "referenceNumber": "UPI-Ref-12345",
    "notes": "Term 1 installment"
  }
  ```

---

## 7. Staff & Faculty (`/staff`)

### `GET /staff`
- Lists all active and registered faculty members.

### `POST /staff`
- Creates employee record and provisions login credentials.

### `POST /staff/{id}/reset-credentials`
- Resets credentials and returns fresh temporary password.

---

## 8. Syllabus Progress (`/syllabus`)

### `GET /syllabus/chapters?subjectId={subjectId}`
- Lists chapters and topic progress.

### `POST /syllabus/progress`
- Toggles topic completion status.

---

## 9. Examinations & Gradebook (`/exams`)

### `GET /exams/section/{sectionId}`
- Lists exams/tests.

### `GET /exams/{testId}/results`
- Lists student marks for test.

### `POST /exams/marks`
- Submits marks for test.

---

## 10. WhatsApp Notifications (`/whatsapp`)

### `GET /whatsapp/logs`
- Audit trail of sent WhatsApp notifications.

### `POST /whatsapp/send-absence-blast`
- Dispatches transactional WhatsApp absence alerts via Meta Cloud API.
