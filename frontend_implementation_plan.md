# Frontend Implementation Plan: Orbin School SaaS

**Platform:** Orbin School Multi-Tenant School Management Platform  
**Target Directory:** `c:\Users\ParthasarathyE\Downloads\JavaPrograms\Orbin School Management\orbin-frontend`  
**Architecture:** Modular Single-Page Application (HTML5 + ES Modules + Vanilla CSS Design System)  

---

## 1. System Architecture & Directory Structure

To deliver high performance, zero build complexity, and maximum maintainability, the frontend is organized cleanly into modular components:

```text
orbin-frontend/
├── index.html                  # Main entry point & view shell
├── css/
│   ├── variables.css           # Design tokens, color palette, typography
│   ├── layout.css              # Grid, sidebar, headers, responsive containers
│   ├── components.css          # Cards, buttons, tables, badges, modals, forms
│   └── whatsapp-sim.css        # Interactive smartphone mockup styling
├── js/
│   ├── app.js                  # App bootstrap, router, view switching
│   ├── state.js                # Global state (tenant context, user, active school)
│   ├── api.js                  # Dual-mode API client (Live Spring Boot Backend / Demo Seed Mode)
│   ├── views/
│   │   ├── publicPortal.js     # Public-facing school website view
│   │   ├── dashboard.js        # School Admin KPI metrics & quick actions
│   │   ├── students.js         # Student admissions & directory
│   │   ├── attendance.js       # 1-click attendance marking sheet
│   │   ├── fees.js             # Fee management & instant printable receipts
│   │   ├── syllabus.js         # Syllabus chapters & progress tracking
│   │   ├── exams.js            # Exams gradebook & report card generation
│   │   ├── whatsappSim.js      # Interactive WhatsApp bot phone simulator
│   │   └── superAdmin.js       # Multi-tenant onboarding & module toggles
│   └── mockData.js             # Realistic default seed data for 3 demo schools
└── assets/                     # School logos, icons, banners
```

---

## 2. Dual-Mode API Integration (Live + Interactive Demo)

To ensure the frontend is **instantly testable and demonstrable without requiring a running PostgreSQL database**, the `api.js` client provides an automatic fallback:

1. **Live Backend Mode:** Connects to `http://localhost:8080/api/v1/...` with JWT bearer tokens.
2. **Interactive Mock Mode:** If the backend is offline or during testing, it seamlessly serves realistic in-memory state with complete create/read/update capabilities.
3. **Tenant Context Switching:** Changing the active school in the top navbar instantly swaps branding colors, logos, students, classes, and fee records.

---

## 3. Detailed View Specifications

### View 1: Public School Website Portal (`/portal/:slug`)
* **Dynamic Branding:** Auto-adapts to the school's primary color, logo, and motto.
* **Hero Banner:** Headline, subtitle, and an *"Inquire for Admission"* call-to-action button.
* **Academic Highlights:** Affiliation board (CBSE/ICSE/State), student-teacher ratio, facilities.
* **Notice Board:** Public circulars and upcoming school events.
* **Photo Gallery:** Clean masonry image grid showcasing school activities.

### View 2: School Executive Dashboard
* **KPI Metrics Cards:**
  * Total Enrolled Students (with month-over-month trend)
  * Active Teaching Staff
  * Today's Attendance Rate (radial percentage gauge)
  * Fees Collected vs Outstanding dues
* **Today's Schedule & Absentees Alert:** Quick alert showing students absent today with option to trigger WhatsApp alerts.
* **Quick Action Buttons:** Admit Student, Mark Attendance, Record Fee Payment, Announce.

### View 3: Student Management & Admission
* **Filter Bar:** Search by admission number/name, filter by Class and Section.
* **Admission Modal:** Step-by-step form capturing student personal info, section assignment, and parent WhatsApp contact details.
* **Student Profile Card:** Modal showing attendance rate, parent contacts, assigned fees, and academic report card.

### View 4: 1-Click Interactive Attendance Register
* **Date & Section Selector:** Select class and section (e.g. Class 5 - Section A).
* **Grid Sheet:** Renders all students in the section with status buttons (`Present`, `Absent`, `Late`).
* **Bulk Controls:** Single button to *"Mark All Present"* followed by one-click toggling of absentees.
* **Instant Save:** Persists attendance and provides a summary badge.

### View 5: Fees & Payment Terminal with Printable Receipts
* **Dues Ledger:** Lists students with pending balances, overdue indicators, and total fees.
* **Payment Modal:** Collect payment with Cash, UPI, Cheque, or Online transfer options.
* **Printable Receipt Modal:**
  * Formatted official school receipt with school branding, receipt number (`RCP-2026-XXXX`), student details, line items, and authorized stamp placeholder ready for `window.print()`.

### View 6: Syllabus Progress Tracker
* **Subject & Term Accordion:** Visual view of Chapters and Topics.
* **Progress Slider:** Mark chapters as `Not Started`, `In Progress`, or `Completed` with period counts.
* **Subject Completion Gauge:** Overall percentage bar for the class.

### View 7: Exams, Marks Entry & Report Cards
* **Test Creator:** Add new exam/test with date, maximum marks, and instructions.
* **Marks Matrix:** Rapid input table to enter marks obtained for every student in the section.
* **Report Card View:** Formatted student progress report card with marks, grades, and teacher remarks.

### View 8: Interactive WhatsApp Bot Live Simulator 💬
* **Smartphone Mockup Frame:** Realistically styled mobile phone screen with WhatsApp-like header, chat bubbles, and timestamp.
* **Multi-Turn Bot Engine:**
  * Type *"Hi"* or *"Hello"*.
  * Bot recognizes the phone number and displays enrolled children.
  * Interactive numbered menu:
    1. Today's Attendance
    2. Fee Dues & Payment Link
    3. Today's Homework
    4. School Announcements
    5. Contact School
  * Instant simulated replies showcasing the SaaS WhatsApp integration in action.

### View 9: Super Admin (Platform) Console
* **Schools Directory:** View all tenant schools (e.g., Orbin International, St. Xavier's, Green Valley).
* **Onboard School Form:** Create a new school with custom subdomain/slug and initial admin.
* **Module Toggles:** Granular feature flags to enable/disable Attendance, Fees, WhatsApp, or CMS.

---

## 4. Visual Design System (Enterprise Aesthetics)

* **Palette:** Tailored modern slate (`#0f172a`), deep cobalt (`#1e40af`), electric cyan (`#0284c7`), emerald green (`#10b981`), and warm amber (`#f59e0b`).
* **Typography:** `Plus Jakarta Sans` / `Inter` from Google Fonts.
* **Elevation & Cards:** Subtle frosted glass cards (`backdrop-filter: blur(8px)`), rounded corners (`border-radius: 12px`), smooth hover transitions (`all 0.2s cubic-bezier(0.4, 0, 0.2, 1)`).
* **Status Badges:** Color-coded badges for Active, Paid, Partial, Pending, Absent, and Present.

---

## 5. Execution Steps

1. **Step 1:** Create `orbin-frontend` directory and define `index.html` structure with all views and modals.
2. **Step 2:** Write `css/` design system (`variables.css`, `layout.css`, `components.css`, `whatsapp-sim.css`).
3. **Step 3:** Implement `js/mockData.js` with 3 complete sample schools, students, teachers, fee structures, and attendance records.
4. **Step 4:** Implement `js/api.js` and `js/state.js` for dual-mode live/mock data operations and tenant switching.
5. **Step 5:** Implement all functional view modules (`dashboard.js`, `students.js`, `attendance.js`, `fees.js`, `syllabus.js`, `exams.js`, `publicPortal.js`, `whatsappSim.js`, `superAdmin.js`).
6. **Step 6:** Launch a local HTTP server and verify complete interactivity.
