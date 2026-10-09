-- ==========================================================================
-- V9__nursery_primary_and_timetable.sql
-- 1. Full School Tiers: Nursery, LKG, UKG, Class 1 - 10 with Sections & Subjects
-- 2. Period Slots (Bell Schedule)
-- 3. Timetable Entries (Class Timetable & Teacher Cockpit)
-- 4. Multi-Tenant Row-Level Security
-- ==========================================================================

-- ── 1. Create Period Slots Table ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS period_slots (
    id          BIGSERIAL PRIMARY KEY,
    school_id   BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    slot_number INT NOT NULL,
    name        VARCHAR(50) NOT NULL,
    start_time  TIME NOT NULL,
    end_time    TIME NOT NULL,
    is_break    BOOLEAN NOT NULL DEFAULT FALSE,
    tier        VARCHAR(20) NOT NULL DEFAULT 'ALL',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, slot_number)
);

CREATE INDEX IF NOT EXISTS idx_period_slots_school ON period_slots(school_id, slot_number);

-- ── 2. Create Timetable Entries Table ─────────────────────────────────────
CREATE TABLE IF NOT EXISTS timetable_entries (
    id                   BIGSERIAL PRIMARY KEY,
    school_id            BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    academic_year_id     BIGINT NOT NULL REFERENCES academic_years(id) ON DELETE CASCADE,
    section_id           BIGINT NOT NULL REFERENCES sections(id) ON DELETE CASCADE,
    period_slot_id       BIGINT NOT NULL REFERENCES period_slots(id) ON DELETE CASCADE,
    day_of_week          INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7), -- 1=Monday, 6=Saturday
    teacher_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject_id           BIGINT REFERENCES subjects(id) ON DELETE SET NULL, -- Null for Nursery/Homeroom
    room_number          VARCHAR(50),
    is_substitution      BOOLEAN NOT NULL DEFAULT FALSE,
    original_teacher_id  BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(school_id, academic_year_id, section_id, period_slot_id, day_of_week)
);

CREATE INDEX IF NOT EXISTS idx_tt_school_section_day ON timetable_entries(school_id, section_id, day_of_week);
CREATE INDEX IF NOT EXISTS idx_tt_school_teacher_day ON timetable_entries(school_id, teacher_id, day_of_week);

-- ── 3. Enable RLS on New Tables ───────────────────────────────────────────
ALTER TABLE period_slots ENABLE ROW LEVEL SECURITY;
ALTER TABLE timetable_entries ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS tenant_isolation_period_slots ON period_slots;
CREATE POLICY tenant_isolation_period_slots ON period_slots
    USING (
        current_setting('app.current_school_id', true) IS NULL
        OR current_setting('app.current_school_id', true) = ''
        OR school_id = current_setting('app.current_school_id', true)::bigint
    );

DROP POLICY IF EXISTS tenant_isolation_timetable_entries ON timetable_entries;
CREATE POLICY tenant_isolation_timetable_entries ON timetable_entries
    USING (
        current_setting('app.current_school_id', true) IS NULL
        OR current_setting('app.current_school_id', true) = ''
        OR school_id = current_setting('app.current_school_id', true)::bigint
    );

-- ── 4. Seed Nursery - Class 10 & Bell Schedule for Existing Schools ────────
DO $$
DECLARE
    r_school RECORD;
    v_acad_year_id BIGINT;
    v_class_id BIGINT;
    v_class_name TEXT;
    v_order INT;
    v_classes TEXT[] := ARRAY['Nursery', 'LKG', 'UKG', 'Class 1', 'Class 2', 'Class 3', 'Class 4', 'Class 5', 'Class 6', 'Class 7', 'Class 8', 'Class 9', 'Class 10'];
    v_sec_name TEXT;
    v_sec_id BIGINT;
    i INT;
BEGIN
    FOR r_school IN SELECT id FROM schools LOOP
        -- Get active or latest academic year
        SELECT id INTO v_acad_year_id FROM academic_years 
        WHERE school_id = r_school.id 
        ORDER BY is_current DESC, id DESC LIMIT 1;

        IF v_acad_year_id IS NOT NULL THEN
            -- Seed Classes & Sections
            FOR i IN 1..array_length(v_classes, 1) LOOP
                v_class_name := v_classes[i];
                v_order := i;

                INSERT INTO classes (school_id, academic_year_id, name, display_order)
                VALUES (r_school.id, v_acad_year_id, v_class_name, v_order)
                ON CONFLICT (school_id, academic_year_id, name) DO UPDATE SET display_order = v_order
                RETURNING id INTO v_class_id;

                IF v_class_id IS NULL THEN
                    SELECT id INTO v_class_id FROM classes 
                    WHERE school_id = r_school.id AND academic_year_id = v_acad_year_id AND name = v_class_name;
                END IF;

                -- Sections A & B
                FOREACH v_sec_name IN ARRAY ARRAY['A', 'B'] LOOP
                    INSERT INTO sections (school_id, class_id, name, capacity)
                    VALUES (r_school.id, v_class_id, v_sec_name, 40)
                    ON CONFLICT (class_id, name) DO NOTHING
                    RETURNING id INTO v_sec_id;
                END LOOP;

                -- Seed Subjects for Middle/High School (Classes 5 to 10)
                IF i >= 8 THEN
                    INSERT INTO subjects (school_id, class_id, name, code, subject_type)
                    VALUES 
                        (r_school.id, v_class_id, 'Mathematics', 'MATH', 'ACADEMIC'),
                        (r_school.id, v_class_id, 'Science', 'SCI', 'ACADEMIC'),
                        (r_school.id, v_class_id, 'English', 'ENG', 'ACADEMIC'),
                        (r_school.id, v_class_id, 'Social Studies', 'SST', 'ACADEMIC'),
                        (r_school.id, v_class_id, 'Hindi', 'HIN', 'LANGUAGE'),
                        (r_school.id, v_class_id, 'Computer Science', 'CS', 'ACADEMIC')
                    ON CONFLICT DO NOTHING;
                END IF;
            END LOOP;

            -- Seed Standard Period Slots (Bell Schedule)
            INSERT INTO period_slots (school_id, slot_number, name, start_time, end_time, is_break, tier)
            VALUES
                (r_school.id, 1, 'Period 1', '08:30:00'::TIME, '09:15:00'::TIME, FALSE, 'ALL'),
                (r_school.id, 2, 'Period 2', '09:15:00'::TIME, '10:00:00'::TIME, FALSE, 'ALL'),
                (r_school.id, 3, 'Morning Break', '10:00:00'::TIME, '10:15:00'::TIME, TRUE, 'ALL'),
                (r_school.id, 4, 'Period 3', '10:15:00'::TIME, '11:00:00'::TIME, FALSE, 'ALL'),
                (r_school.id, 5, 'Period 4', '11:00:00'::TIME, '11:45:00'::TIME, FALSE, 'ALL'),
                (r_school.id, 6, 'Lunch Break', '11:45:00'::TIME, '12:30:00'::TIME, TRUE, 'ALL'),
                (r_school.id, 7, 'Period 5', '12:30:00'::TIME, '13:15:00'::TIME, FALSE, 'ALL'),
                (r_school.id, 8, 'Period 6', '13:15:00'::TIME, '14:00:00'::TIME, FALSE, 'ALL'),
                (r_school.id, 9, 'Period 7', '14:00:00'::TIME, '14:45:00'::TIME, FALSE, 'ALL'),
                (r_school.id, 10, 'Period 8', '14:45:00'::TIME, '15:30:00'::TIME, FALSE, 'ALL')
            ON CONFLICT (school_id, slot_number) DO NOTHING;
        END IF;
    END LOOP;
END $$;
