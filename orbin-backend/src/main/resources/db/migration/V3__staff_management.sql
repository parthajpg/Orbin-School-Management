-- ==========================================================================
-- V3__staff_management.sql
-- Staff and Faculty Management Schema
-- ==========================================================================

CREATE TABLE IF NOT EXISTS staff (
    id                   BIGSERIAL PRIMARY KEY,
    school_id            BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    user_id              BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    employee_id          VARCHAR(40)  NOT NULL,
    first_name           VARCHAR(80)  NOT NULL,
    last_name            VARCHAR(80)  NOT NULL,
    email                VARCHAR(150) NOT NULL,
    phone                VARCHAR(25)  NOT NULL,
    role                 VARCHAR(40)  NOT NULL DEFAULT 'TEACHER',
    designation          VARCHAR(100),
    department           VARCHAR(100),
    assigned_class_id    BIGINT       REFERENCES classes(id) ON DELETE SET NULL,
    assigned_section_id  BIGINT       REFERENCES sections(id) ON DELETE SET NULL,
    subjects_taught      TEXT[],
    qualification        VARCHAR(150),
    date_of_joining      DATE,
    status               VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE(school_id, employee_id),
    UNIQUE(school_id, email)
);

CREATE INDEX IF NOT EXISTS idx_staff_school_id ON staff(school_id);
CREATE INDEX IF NOT EXISTS idx_staff_user_id ON staff(user_id);
CREATE INDEX IF NOT EXISTS idx_staff_assigned_section ON staff(school_id, assigned_section_id);
