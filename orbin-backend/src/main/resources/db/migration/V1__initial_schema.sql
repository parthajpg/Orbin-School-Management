-- ==========================================================================
-- V1__initial_schema.sql
-- Orbin School – Initial Database Schema
-- Multi-tenant: every school-owned table has school_id FK
-- ==========================================================================

-- ── Extensions ────────────────────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── Schools ───────────────────────────────────────────────────────────────
CREATE TABLE schools (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    short_name     VARCHAR(30),
    slug           VARCHAR(50)  NOT NULL UNIQUE,
    custom_domain  VARCHAR(100) UNIQUE,
    phone          VARCHAR(20),
    email          VARCHAR(100),
    address        TEXT,
    pincode        VARCHAR(10),
    city           VARCHAR(50),
    state          VARCHAR(50),
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── School Branding ───────────────────────────────────────────────────────
CREATE TABLE school_branding (
    id                  BIGSERIAL PRIMARY KEY,
    school_id           BIGINT       NOT NULL UNIQUE REFERENCES schools(id) ON DELETE CASCADE,
    logo_url            VARCHAR(500),
    favicon_url         VARCHAR(500),
    motto               VARCHAR(200),
    primary_color       VARCHAR(10)  NOT NULL DEFAULT '#1E40AF',
    secondary_color     VARCHAR(10)  NOT NULL DEFAULT '#3B82F6',
    accent_color        VARCHAR(10)  NOT NULL DEFAULT '#F59E0B',
    hero_banner_url     VARCHAR(500),
    hero_title          VARCHAR(200),
    hero_subtitle       VARCHAR(300),
    about_text          TEXT,
    admissions_text     TEXT,
    established_year    INT,
    affiliation_board   VARCHAR(50),
    whatsapp_number     VARCHAR(20),
    google_maps_embed   TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── School Modules ────────────────────────────────────────────────────────
CREATE TABLE school_modules (
    id          BIGSERIAL PRIMARY KEY,
    school_id   BIGINT      NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    module_name VARCHAR(60) NOT NULL,
    enabled     BOOLEAN     NOT NULL DEFAULT TRUE,
    config      JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, module_name)
);

-- ── Permissions ───────────────────────────────────────────────────────────
CREATE TABLE permissions (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(200),
    module      VARCHAR(60),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Roles ─────────────────────────────────────────────────────────────────
CREATE TABLE roles (
    id            BIGSERIAL PRIMARY KEY,
    school_id     BIGINT      REFERENCES schools(id) ON DELETE CASCADE,
    name          VARCHAR(60) NOT NULL,
    description   VARCHAR(200),
    is_system_role BOOLEAN    NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, name)
);

-- ── Role Permissions ──────────────────────────────────────────────────────
CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- ── Users ─────────────────────────────────────────────────────────────────
CREATE TABLE users (
    id                    BIGSERIAL    PRIMARY KEY,
    school_id             BIGINT       REFERENCES schools(id) ON DELETE CASCADE,
    email                 VARCHAR(150) NOT NULL UNIQUE,
    password_hash         VARCHAR(200) NOT NULL,
    first_name            VARCHAR(80)  NOT NULL,
    last_name             VARCHAR(80),
    phone                 VARCHAR(20),
    profile_image_url     VARCHAR(500),
    status                VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    force_password_change BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_email     ON users(email);
CREATE INDEX idx_users_school_id ON users(school_id);

-- ── User Roles ────────────────────────────────────────────────────────────
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ── Refresh Tokens ────────────────────────────────────────────────────────
CREATE TABLE refresh_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(512) NOT NULL UNIQUE,
    family      VARCHAR(100) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    revoked_at  TIMESTAMPTZ,
    user_agent  VARCHAR(300),
    ip_address  VARCHAR(45),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_rt_token   ON refresh_tokens(token);
CREATE INDEX idx_rt_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_rt_family  ON refresh_tokens(family);

-- ── Academic Years ────────────────────────────────────────────────────────
CREATE TABLE academic_years (
    id          BIGSERIAL    PRIMARY KEY,
    school_id   BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    name        VARCHAR(20)  NOT NULL,  -- e.g. "2024-25"
    start_date  DATE         NOT NULL,
    end_date    DATE         NOT NULL,
    is_current  BOOLEAN      NOT NULL DEFAULT FALSE,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE(school_id, name)
);
CREATE INDEX idx_ay_school_id ON academic_years(school_id);

-- ── Classes ───────────────────────────────────────────────────────────────
CREATE TABLE classes (
    id               BIGSERIAL   PRIMARY KEY,
    school_id        BIGINT      NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    academic_year_id BIGINT      NOT NULL REFERENCES academic_years(id) ON DELETE CASCADE,
    name             VARCHAR(50) NOT NULL,   -- e.g. "Class 5", "UKG"
    display_order    INT         NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, academic_year_id, name)
);
CREATE INDEX idx_classes_school_id ON classes(school_id, academic_year_id);

-- ── Sections ──────────────────────────────────────────────────────────────
CREATE TABLE sections (
    id          BIGSERIAL   PRIMARY KEY,
    school_id   BIGINT      NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    class_id    BIGINT      NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    name        VARCHAR(10) NOT NULL,   -- "A", "B", "C"
    capacity    INT,
    class_teacher_id BIGINT REFERENCES users(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(class_id, name)
);
CREATE INDEX idx_sections_school_class ON sections(school_id, class_id);

-- ── Subjects ──────────────────────────────────────────────────────────────
CREATE TABLE subjects (
    id          BIGSERIAL    PRIMARY KEY,
    school_id   BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    class_id    BIGINT       NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(20),
    subject_type VARCHAR(30) NOT NULL DEFAULT 'ACADEMIC',  -- ACADEMIC, ACTIVITY, LANGUAGE
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE(class_id, code)
);
CREATE INDEX idx_subjects_school_class ON subjects(school_id, class_id);

-- ── Teacher Assignments ───────────────────────────────────────────────────
CREATE TABLE teacher_assignments (
    id               BIGSERIAL PRIMARY KEY,
    school_id        BIGINT    NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    academic_year_id BIGINT    NOT NULL REFERENCES academic_years(id),
    teacher_id       BIGINT    NOT NULL REFERENCES users(id),
    class_id         BIGINT    NOT NULL REFERENCES classes(id),
    section_id       BIGINT    NOT NULL REFERENCES sections(id),
    subject_id       BIGINT    NOT NULL REFERENCES subjects(id),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, academic_year_id, teacher_id, section_id, subject_id)
);
CREATE INDEX idx_ta_school_teacher ON teacher_assignments(school_id, teacher_id);
CREATE INDEX idx_ta_school_section ON teacher_assignments(school_id, section_id);

-- ── Students ──────────────────────────────────────────────────────────────
CREATE TABLE students (
    id               BIGSERIAL    PRIMARY KEY,
    school_id        BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    academic_year_id BIGINT       NOT NULL REFERENCES academic_years(id),
    admission_number VARCHAR(30)  NOT NULL,
    first_name       VARCHAR(80)  NOT NULL,
    last_name        VARCHAR(80),
    date_of_birth    DATE,
    gender           VARCHAR(10),
    section_id       BIGINT       NOT NULL REFERENCES sections(id),
    admission_date   DATE         NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, TRANSFERRED, ARCHIVED
    address          TEXT,
    profile_image_url VARCHAR(500),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE(school_id, admission_number)
);
CREATE INDEX idx_students_school_id     ON students(school_id);
CREATE INDEX idx_students_school_section ON students(school_id, section_id);
CREATE INDEX idx_students_school_ay     ON students(school_id, academic_year_id);

-- ── Parents / Guardians ───────────────────────────────────────────────────
CREATE TABLE parents (
    id                   BIGSERIAL    PRIMARY KEY,
    school_id            BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    first_name           VARCHAR(80)  NOT NULL,
    last_name            VARCHAR(80),
    phone                VARCHAR(20)  NOT NULL,
    normalized_phone     VARCHAR(20)  NOT NULL,   -- canonical +91XXXXXXXXXX
    email                VARCHAR(150),
    occupation           VARCHAR(100),
    address              TEXT,
    whatsapp_verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_parents_school_id   ON parents(school_id);
CREATE INDEX idx_parents_school_phone ON parents(school_id, normalized_phone);

-- ── Parent–Student Mapping ────────────────────────────────────────────────
CREATE TABLE parent_students (
    id                       BIGSERIAL   PRIMARY KEY,
    parent_id                BIGINT      NOT NULL REFERENCES parents(id) ON DELETE CASCADE,
    student_id               BIGINT      NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    relationship             VARCHAR(30) NOT NULL,   -- FATHER, MOTHER, GUARDIAN
    is_primary               BOOLEAN     NOT NULL DEFAULT FALSE,
    can_receive_notifications BOOLEAN    NOT NULL DEFAULT TRUE,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(parent_id, student_id)
);
CREATE INDEX idx_ps_student_id ON parent_students(student_id);
CREATE INDEX idx_ps_parent_id  ON parent_students(parent_id);

-- ── Attendance ────────────────────────────────────────────────────────────
CREATE TABLE attendance (
    id          BIGSERIAL   PRIMARY KEY,
    school_id   BIGINT      NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    student_id  BIGINT      NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    section_id  BIGINT      NOT NULL REFERENCES sections(id),
    date        DATE        NOT NULL,
    status      VARCHAR(20) NOT NULL,  -- PRESENT, ABSENT, LATE, EXCUSED
    marked_by   BIGINT      REFERENCES users(id),
    notes       VARCHAR(300),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, student_id, date)
);
CREATE INDEX idx_attendance_school_date    ON attendance(school_id, date);
CREATE INDEX idx_attendance_school_student ON attendance(school_id, student_id);
CREATE INDEX idx_attendance_section_date  ON attendance(section_id, date);

-- ── Fee Structures ────────────────────────────────────────────────────────
CREATE TABLE fee_structures (
    id               BIGSERIAL    PRIMARY KEY,
    school_id        BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    academic_year_id BIGINT       NOT NULL REFERENCES academic_years(id),
    name             VARCHAR(100) NOT NULL,
    class_id         BIGINT       REFERENCES classes(id),
    description      TEXT,
    total_amount     NUMERIC(10,2) NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Fee Items (line items within a fee structure) ─────────────────────────
CREATE TABLE fee_items (
    id                BIGSERIAL    PRIMARY KEY,
    fee_structure_id  BIGINT       NOT NULL REFERENCES fee_structures(id) ON DELETE CASCADE,
    school_id         BIGINT       NOT NULL REFERENCES schools(id),
    category          VARCHAR(50)  NOT NULL,  -- TUITION, TRANSPORT, BOOKS, etc.
    name              VARCHAR(100) NOT NULL,
    amount            NUMERIC(10,2) NOT NULL,
    due_date          DATE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Student Fees ──────────────────────────────────────────────────────────
CREATE TABLE student_fees (
    id                BIGSERIAL    PRIMARY KEY,
    school_id         BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    student_id        BIGINT       NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    fee_structure_id  BIGINT       NOT NULL REFERENCES fee_structures(id),
    academic_year_id  BIGINT       NOT NULL REFERENCES academic_years(id),
    total_amount      NUMERIC(10,2) NOT NULL,
    paid_amount       NUMERIC(10,2) NOT NULL DEFAULT 0,
    outstanding       NUMERIC(10,2) GENERATED ALWAYS AS (total_amount - paid_amount) STORED,
    due_date          DATE,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',  -- PENDING, PARTIAL, PAID, OVERDUE
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE(school_id, student_id, fee_structure_id)
);
CREATE INDEX idx_sf_school_student ON student_fees(school_id, student_id);
CREATE INDEX idx_sf_school_status  ON student_fees(school_id, status);

-- ── Payments ──────────────────────────────────────────────────────────────
CREATE TABLE payments (
    id                BIGSERIAL    PRIMARY KEY,
    school_id         BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    student_fee_id    BIGINT       NOT NULL REFERENCES student_fees(id),
    student_id        BIGINT       NOT NULL REFERENCES students(id),
    amount            NUMERIC(10,2) NOT NULL,
    payment_date      DATE         NOT NULL,
    payment_method    VARCHAR(30)  NOT NULL DEFAULT 'CASH',  -- CASH, UPI, CHEQUE, ONLINE
    reference_number  VARCHAR(100),
    receipt_number    VARCHAR(50)  NOT NULL UNIQUE,
    notes             TEXT,
    recorded_by       BIGINT       REFERENCES users(id),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_payments_school_student ON payments(school_id, student_id);
CREATE INDEX idx_payments_school_date    ON payments(school_id, payment_date);

-- ── Syllabus Terms ────────────────────────────────────────────────────────
CREATE TABLE terms (
    id               BIGSERIAL    PRIMARY KEY,
    school_id        BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    academic_year_id BIGINT       NOT NULL REFERENCES academic_years(id),
    name             VARCHAR(50)  NOT NULL,  -- "Term 1", "Quarterly 1"
    start_date       DATE,
    end_date         DATE,
    display_order    INT          NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Syllabus Chapters ─────────────────────────────────────────────────────
CREATE TABLE syllabus_chapters (
    id             BIGSERIAL    PRIMARY KEY,
    school_id      BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    subject_id     BIGINT       NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    term_id        BIGINT       REFERENCES terms(id),
    title          VARCHAR(200) NOT NULL,
    description    TEXT,
    display_order  INT          NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_sc_school_subject ON syllabus_chapters(school_id, subject_id);

-- ── Syllabus Topics ───────────────────────────────────────────────────────
CREATE TABLE syllabus_topics (
    id             BIGSERIAL    PRIMARY KEY,
    school_id      BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    chapter_id     BIGINT       NOT NULL REFERENCES syllabus_chapters(id) ON DELETE CASCADE,
    title          VARCHAR(200) NOT NULL,
    display_order  INT          NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Syllabus Progress ─────────────────────────────────────────────────────
CREATE TABLE syllabus_progress (
    id                       BIGSERIAL   PRIMARY KEY,
    school_id                BIGINT      NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    chapter_id               BIGINT      NOT NULL REFERENCES syllabus_chapters(id) ON DELETE CASCADE,
    section_id               BIGINT      NOT NULL REFERENCES sections(id),
    teacher_id               BIGINT      REFERENCES users(id),
    status                   VARCHAR(25) NOT NULL DEFAULT 'NOT_STARTED',
    planned_start_date       DATE,
    planned_completion_date  DATE,
    actual_completion_date   DATE,
    estimated_periods        INT,
    actual_periods           INT,
    notes                    TEXT,
    revision_date            DATE,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, chapter_id, section_id)
);
CREATE INDEX idx_sp_school_section ON syllabus_progress(school_id, section_id);

-- ── Homework ──────────────────────────────────────────────────────────────
CREATE TABLE homework (
    id               BIGSERIAL    PRIMARY KEY,
    school_id        BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    section_id       BIGINT       NOT NULL REFERENCES sections(id),
    subject_id       BIGINT       NOT NULL REFERENCES subjects(id),
    teacher_id       BIGINT       NOT NULL REFERENCES users(id),
    title            VARCHAR(200) NOT NULL,
    description      TEXT,
    due_date         DATE         NOT NULL,
    attachment_url   VARCHAR(500),
    attachment_key   VARCHAR(300),
    status           VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',  -- DRAFT, PUBLISHED, SCHEDULED
    publish_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_hw_school_section ON homework(school_id, section_id);
CREATE INDEX idx_hw_due_date       ON homework(school_id, due_date);

-- ── Materials ─────────────────────────────────────────────────────────────
CREATE TABLE materials (
    id             BIGSERIAL    PRIMARY KEY,
    school_id      BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    subject_id     BIGINT       NOT NULL REFERENCES subjects(id),
    chapter_id     BIGINT       REFERENCES syllabus_chapters(id),
    teacher_id     BIGINT       NOT NULL REFERENCES users(id),
    title          VARCHAR(200) NOT NULL,
    description    TEXT,
    material_type  VARCHAR(30)  NOT NULL,  -- PDF, IMAGE, VIDEO_LINK, WORKSHEET, LINK, OTHER
    file_url       VARCHAR(500),
    file_key       VARCHAR(300),
    file_size      BIGINT,
    mime_type      VARCHAR(100),
    external_url   VARCHAR(500),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_materials_school_subject ON materials(school_id, subject_id);

-- ── Tests ─────────────────────────────────────────────────────────────────
CREATE TABLE tests (
    id           BIGSERIAL    PRIMARY KEY,
    school_id    BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    section_id   BIGINT       NOT NULL REFERENCES sections(id),
    subject_id   BIGINT       NOT NULL REFERENCES subjects(id),
    teacher_id   BIGINT       NOT NULL REFERENCES users(id),
    title        VARCHAR(200) NOT NULL,
    test_date    DATE,
    duration_min INT,
    max_marks    NUMERIC(6,2),
    instructions TEXT,
    status       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_tests_school_section ON tests(school_id, section_id);

-- ── Results ───────────────────────────────────────────────────────────────
CREATE TABLE results (
    id              BIGSERIAL     PRIMARY KEY,
    school_id       BIGINT        NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    test_id         BIGINT        NOT NULL REFERENCES tests(id) ON DELETE CASCADE,
    student_id      BIGINT        NOT NULL REFERENCES students(id),
    marks_obtained  NUMERIC(6,2),
    max_marks       NUMERIC(6,2),
    percentage      NUMERIC(5,2),
    teacher_note    TEXT,
    entered_by      BIGINT        REFERENCES users(id),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    UNIQUE(school_id, test_id, student_id)
);
CREATE INDEX idx_results_school_student ON results(school_id, student_id);
CREATE INDEX idx_results_school_test    ON results(school_id, test_id);

-- ── Achievements ──────────────────────────────────────────────────────────
CREATE TABLE achievements (
    id          BIGSERIAL    PRIMARY KEY,
    school_id   BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    student_id  BIGINT       NOT NULL REFERENCES students(id),
    awarded_by  BIGINT       REFERENCES users(id),
    category    VARCHAR(60)  NOT NULL,   -- ATTENDANCE_STAR, READING_STAR, etc.
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    award_date  DATE         NOT NULL,
    public      BOOLEAN      NOT NULL DEFAULT FALSE,
    certificate_url VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_achievements_school_student ON achievements(school_id, student_id);

-- ── Events ────────────────────────────────────────────────────────────────
CREATE TABLE events (
    id           BIGSERIAL    PRIMARY KEY,
    school_id    BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    event_type   VARCHAR(50)  NOT NULL DEFAULT 'GENERAL',  -- ANNUAL_DAY, SPORTS, CULTURAL, PTM, GENERAL
    event_date   DATE         NOT NULL,
    end_date     DATE,
    venue        VARCHAR(200),
    public       BOOLEAN      NOT NULL DEFAULT TRUE,
    status       VARCHAR(20)  NOT NULL DEFAULT 'UPCOMING',
    created_by   BIGINT       REFERENCES users(id),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_events_school_date ON events(school_id, event_date);

-- ── Event Awards ──────────────────────────────────────────────────────────
CREATE TABLE event_awards (
    id          BIGSERIAL    PRIMARY KEY,
    school_id   BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    event_id    BIGINT       NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    category    VARCHAR(100) NOT NULL,
    student_id  BIGINT       REFERENCES students(id),
    participant_name VARCHAR(100),
    prize_rank  VARCHAR(30),   -- FIRST, SECOND, THIRD, SPECIAL_MENTION
    notes       TEXT,
    public      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Announcements ─────────────────────────────────────────────────────────
CREATE TABLE announcements (
    id           BIGSERIAL    PRIMARY KEY,
    school_id    BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    audience     VARCHAR(30)  NOT NULL DEFAULT 'ALL',  -- ALL, STAFF, PARENTS, CLASS_SPECIFIC, PUBLIC
    section_id   BIGINT       REFERENCES sections(id),
    urgent       BOOLEAN      NOT NULL DEFAULT FALSE,
    publish_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at   TIMESTAMPTZ,
    created_by   BIGINT       REFERENCES users(id),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_announcements_school    ON announcements(school_id, publish_at);
CREATE INDEX idx_announcements_audience  ON announcements(school_id, audience);

-- ── Website Pages (CMS) ───────────────────────────────────────────────────
CREATE TABLE website_pages (
    id          BIGSERIAL    PRIMARY KEY,
    school_id   BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    slug        VARCHAR(80)  NOT NULL,  -- 'home', 'about', 'admissions', etc.
    title       VARCHAR(200) NOT NULL,
    content     JSONB,   -- flexible structured content blocks
    published   BOOLEAN      NOT NULL DEFAULT FALSE,
    updated_by  BIGINT       REFERENCES users(id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE(school_id, slug)
);

-- ── Gallery ───────────────────────────────────────────────────────────────
CREATE TABLE gallery_items (
    id           BIGSERIAL    PRIMARY KEY,
    school_id    BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    title        VARCHAR(200),
    image_url    VARCHAR(500) NOT NULL,
    image_key    VARCHAR(300),
    category     VARCHAR(60),
    event_id     BIGINT       REFERENCES events(id),
    display_order INT         NOT NULL DEFAULT 0,
    public       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_gallery_school ON gallery_items(school_id, display_order);

-- ── WhatsApp Sessions ─────────────────────────────────────────────────────
CREATE TABLE whatsapp_sessions (
    id              BIGSERIAL   PRIMARY KEY,
    school_id       BIGINT      REFERENCES schools(id),
    phone_number    VARCHAR(20) NOT NULL,
    parent_id       BIGINT      REFERENCES parents(id),
    selected_student_id BIGINT  REFERENCES students(id),
    state           VARCHAR(50) NOT NULL DEFAULT 'IDLE',  -- IDLE, STUDENT_SELECT, MENU, etc.
    context         JSONB,
    last_activity   TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(phone_number)
);
CREATE INDEX idx_wa_phone ON whatsapp_sessions(phone_number);

-- ── Notification Jobs ─────────────────────────────────────────────────────
CREATE TABLE notification_jobs (
    id           BIGSERIAL    PRIMARY KEY,
    school_id    BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    job_type     VARCHAR(50)  NOT NULL,
    channel      VARCHAR(20)  NOT NULL DEFAULT 'WHATSAPP',
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    payload      JSONB,
    scheduled_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    started_at   TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    error        TEXT,
    created_by   BIGINT       REFERENCES users(id),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_nj_school_status ON notification_jobs(school_id, status);

-- ── Audit Logs ────────────────────────────────────────────────────────────
CREATE TABLE audit_logs (
    id             BIGSERIAL    PRIMARY KEY,
    school_id      BIGINT       REFERENCES schools(id),
    user_id        BIGINT       REFERENCES users(id),
    action         VARCHAR(60)  NOT NULL,
    entity_type    VARCHAR(60)  NOT NULL,
    entity_id      VARCHAR(60),
    previous_value JSONB,
    new_value      JSONB,
    ip_address     VARCHAR(45),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_school     ON audit_logs(school_id, created_at DESC);
CREATE INDEX idx_audit_user       ON audit_logs(user_id);
CREATE INDEX idx_audit_entity     ON audit_logs(entity_type, entity_id);
