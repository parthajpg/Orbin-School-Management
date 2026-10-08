-- ==========================================================================
-- V6__seed_initial_production_baseline.sql
-- Production Baseline Seeding:
-- 1. Orbin Platform Super Admin (admin@orbin.edu / admin123)
-- 2. Primary Tenant School: Delhi Public Academy (delhi-public)
-- 3. School Branding & Active Modules
-- 4. School-level Roles & Permission Assignments
-- 5. Primary School Admin (admin@delhipublicacademy.edu / school123)
-- 6. Current Academic Year (2026-27), Classes (Class 5, Class 6) & Sections
-- 7. Initial Faculty & Staff User (teacher@delhipublicacademy.edu / teacher123)
-- ==========================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── 1. Assign All Permissions to ORBIN_ADMIN Platform Role ───────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ORBIN_ADMIN' AND r.school_id IS NULL
ON CONFLICT DO NOTHING;

-- ── 2. Platform Super Admin User (parthasarathye2256@gmail.com / Partha@2256) ────────────
INSERT INTO users (
    school_id, email, password_hash, first_name, last_name, phone, status, force_password_change
) VALUES (
    NULL,
    'parthasarathye2256@gmail.com',
    crypt('Partha@2256', gen_salt('bf', 12)),
    'Partha',
    'E',
    '+91 98100 00001',
    'ACTIVE',
    FALSE
) ON CONFLICT (email) DO UPDATE SET
    password_hash = crypt('Partha@2256', gen_salt('bf', 12)),
    first_name = 'Partha',
    last_name = 'E',
    status = 'ACTIVE';

-- Link Super Admin user to ORBIN_ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ORBIN_ADMIN' AND r.school_id IS NULL
WHERE u.email = 'parthasarathye2256@gmail.com'
ON CONFLICT DO NOTHING;

-- ── 3. Primary Tenant School: Delhi Public Academy ───────────────────────
INSERT INTO schools (
    name, short_name, slug, phone, email, address, pincode, city, state, status
) VALUES (
    'Delhi Public Academy',
    'DPA',
    'delhi-public',
    '+91 11 2689 4500',
    'admin@delhipublicacademy.edu',
    'Sector 14, R.K. Puram',
    '110022',
    'New Delhi',
    'Delhi',
    'ACTIVE'
) ON CONFLICT (slug) DO NOTHING;

-- Retrieve DPA School ID for related tables
DO $$
DECLARE
    v_school_id BIGINT;
    v_role_school_admin_id BIGINT;
    v_role_teacher_id BIGINT;
    v_role_accountant_id BIGINT;
    v_user_school_admin_id BIGINT;
    v_user_teacher_id BIGINT;
    v_acad_year_id BIGINT;
    v_class_5_id BIGINT;
    v_class_6_id BIGINT;
    v_sec_5a_id BIGINT;
    v_sec_5b_id BIGINT;
    v_sec_6a_id BIGINT;
    v_sec_6b_id BIGINT;
BEGIN
    SELECT id INTO v_school_id FROM schools WHERE slug = 'delhi-public';

    -- ── 4. School Branding ───────────────────────────────────────────────
    INSERT INTO school_branding (
        school_id, motto, primary_color, secondary_color, accent_color,
        established_year, affiliation_board, whatsapp_number, about_text
    ) VALUES (
        v_school_id,
        'Service Before Self',
        '#2563eb',
        '#1d4ed8',
        '#38bdf8',
        1985,
        'CBSE Affiliation #213089',
        '+911126894500',
        'Empowering generations through holistic education, academic excellence, and character building.'
    ) ON CONFLICT (school_id) DO UPDATE SET
        affiliation_board = 'CBSE Affiliation #213089',
        motto = 'Service Before Self';

    -- ── 5. School Modules ────────────────────────────────────────────────
    INSERT INTO school_modules (school_id, module_name, enabled) VALUES
    (v_school_id, 'attendance', true),
    (v_school_id, 'fees',       true),
    (v_school_id, 'syllabus',   true),
    (v_school_id, 'exams',      true),
    (v_school_id, 'whatsapp',   true),
    (v_school_id, 'website',    true)
    ON CONFLICT (school_id, module_name) DO NOTHING;

    -- ── 6. Tenant-Specific Roles ────────────────────────────────────────
    INSERT INTO roles (school_id, name, description, is_system_role)
    VALUES (v_school_id, 'SCHOOL_ADMIN', 'School Principal & Operations Administrator', FALSE)
    ON CONFLICT (school_id, name) DO UPDATE SET description = EXCLUDED.description
    RETURNING id INTO v_role_school_admin_id;

    IF v_role_school_admin_id IS NULL THEN
        SELECT id INTO v_role_school_admin_id FROM roles WHERE school_id = v_school_id AND name = 'SCHOOL_ADMIN';
    END IF;

    INSERT INTO roles (school_id, name, description, is_system_role)
    VALUES (v_school_id, 'TEACHER', 'Teaching Faculty & Class In-Charge', FALSE)
    ON CONFLICT (school_id, name) DO UPDATE SET description = EXCLUDED.description
    RETURNING id INTO v_role_teacher_id;

    IF v_role_teacher_id IS NULL THEN
        SELECT id INTO v_role_teacher_id FROM roles WHERE school_id = v_school_id AND name = 'TEACHER';
    END IF;

    INSERT INTO roles (school_id, name, description, is_system_role)
    VALUES (v_school_id, 'ACCOUNTANT', 'School Accounts & Fee Collection Desk', FALSE)
    ON CONFLICT (school_id, name) DO UPDATE SET description = EXCLUDED.description
    RETURNING id INTO v_role_accountant_id;

    IF v_role_accountant_id IS NULL THEN
        SELECT id INTO v_role_accountant_id FROM roles WHERE school_id = v_school_id AND name = 'ACCOUNTANT';
    END IF;

    -- Attach permissions to SCHOOL_ADMIN (all non-platform permissions)
    INSERT INTO role_permissions (role_id, permission_id)
    SELECT v_role_school_admin_id, p.id
    FROM permissions p
    WHERE p.module != 'Platform'
    ON CONFLICT DO NOTHING;

    -- Attach permissions to TEACHER
    INSERT INTO role_permissions (role_id, permission_id)
    SELECT v_role_teacher_id, p.id
    FROM permissions p
    WHERE p.name IN (
        'attendance.read', 'attendance.write',
        'students.read',
        'academic.read',
        'syllabus.read', 'syllabus.write',
        'homework.read', 'homework.write',
        'materials.read', 'materials.write',
        'tests.read', 'tests.manage',
        'results.read', 'results.manage',
        'notifications.send'
    )
    ON CONFLICT DO NOTHING;

    -- Attach permissions to ACCOUNTANT
    INSERT INTO role_permissions (role_id, permission_id)
    SELECT v_role_accountant_id, p.id
    FROM permissions p
    WHERE p.name IN (
        'fees.read', 'fees.write', 'fees.manage', 'payments.record',
        'students.read', 'reports.view'
    )
    ON CONFLICT DO NOTHING;

    -- ── 7. Primary School Admin (admin@delhipublicacademy.edu / school123) ─
    INSERT INTO users (
        school_id, email, password_hash, first_name, last_name, phone, status, force_password_change
    ) VALUES (
        v_school_id,
        'admin@delhipublicacademy.edu',
        crypt('school123', gen_salt('bf', 12)),
        'Dr. Arvind',
        'Saxena',
        '+91 98111 22334',
        'ACTIVE',
        FALSE
    ) ON CONFLICT (email) DO UPDATE SET
        password_hash = crypt('school123', gen_salt('bf', 12)),
        school_id = v_school_id,
        status = 'ACTIVE'
    RETURNING id INTO v_user_school_admin_id;

    IF v_user_school_admin_id IS NULL THEN
        SELECT id INTO v_user_school_admin_id FROM users WHERE email = 'admin@delhipublicacademy.edu';
    END IF;

    INSERT INTO user_roles (user_id, role_id)
    VALUES (v_user_school_admin_id, v_role_school_admin_id)
    ON CONFLICT DO NOTHING;

    -- ── 8. Initial Faculty User (teacher@delhipublicacademy.edu / teacher123) ─
    INSERT INTO users (
        school_id, email, password_hash, first_name, last_name, phone, status, force_password_change
    ) VALUES (
        v_school_id,
        'teacher@delhipublicacademy.edu',
        crypt('teacher123', gen_salt('bf', 12)),
        'Ms. Sunita',
        'Rao',
        '+91 98222 33445',
        'ACTIVE',
        FALSE
    ) ON CONFLICT (email) DO UPDATE SET
        password_hash = crypt('teacher123', gen_salt('bf', 12)),
        school_id = v_school_id,
        status = 'ACTIVE'
    RETURNING id INTO v_user_teacher_id;

    IF v_user_teacher_id IS NULL THEN
        SELECT id INTO v_user_teacher_id FROM users WHERE email = 'teacher@delhipublicacademy.edu';
    END IF;

    INSERT INTO user_roles (user_id, role_id)
    VALUES (v_user_teacher_id, v_role_teacher_id)
    ON CONFLICT DO NOTHING;

    -- ── 9. Academic Year (2026-27) ───────────────────────────────────────
    INSERT INTO academic_years (
        school_id, name, start_date, end_date, is_current, status
    ) VALUES (
        v_school_id,
        '2026-27',
        '2026-04-01',
        '2027-03-31',
        TRUE,
        'ACTIVE'
    ) ON CONFLICT (school_id, name) DO UPDATE SET is_current = TRUE
    RETURNING id INTO v_acad_year_id;

    IF v_acad_year_id IS NULL THEN
        SELECT id INTO v_acad_year_id FROM academic_years WHERE school_id = v_school_id AND name = '2026-27';
    END IF;

    -- ── 10. Core Classes (Class 5 & Class 6) ──────────────────────────────
    INSERT INTO classes (
        school_id, academic_year_id, name, display_order
    ) VALUES (
        v_school_id, v_acad_year_id, 'Class 5', 5
    ) ON CONFLICT (school_id, academic_year_id, name) DO UPDATE SET display_order = 5
    RETURNING id INTO v_class_5_id;

    IF v_class_5_id IS NULL THEN
        SELECT id INTO v_class_5_id FROM classes WHERE school_id = v_school_id AND academic_year_id = v_acad_year_id AND name = 'Class 5';
    END IF;

    INSERT INTO classes (
        school_id, academic_year_id, name, display_order
    ) VALUES (
        v_school_id, v_acad_year_id, 'Class 6', 6
    ) ON CONFLICT (school_id, academic_year_id, name) DO UPDATE SET display_order = 6
    RETURNING id INTO v_class_6_id;

    IF v_class_6_id IS NULL THEN
        SELECT id INTO v_class_6_id FROM classes WHERE school_id = v_school_id AND academic_year_id = v_acad_year_id AND name = 'Class 6';
    END IF;

    -- ── 11. Core Sections ────────────────────────────────────────────────
    INSERT INTO sections (
        school_id, class_id, name, capacity, class_teacher_id
    ) VALUES (
        v_school_id, v_class_5_id, 'A', 40, v_user_teacher_id
    ) ON CONFLICT (class_id, name) DO UPDATE SET capacity = 40, class_teacher_id = v_user_teacher_id
    RETURNING id INTO v_sec_5a_id;

    IF v_sec_5a_id IS NULL THEN
        SELECT id INTO v_sec_5a_id FROM sections WHERE class_id = v_class_5_id AND name = 'A';
    END IF;

    INSERT INTO sections (
        school_id, class_id, name, capacity
    ) VALUES (
        v_school_id, v_class_5_id, 'B', 40
    ) ON CONFLICT (class_id, name) DO UPDATE SET capacity = 40
    RETURNING id INTO v_sec_5b_id;

    INSERT INTO sections (
        school_id, class_id, name, capacity
    ) VALUES (
        v_school_id, v_class_6_id, 'A', 40
    ) ON CONFLICT (class_id, name) DO UPDATE SET capacity = 40
    RETURNING id INTO v_sec_6a_id;

    INSERT INTO sections (
        school_id, class_id, name, capacity
    ) VALUES (
        v_school_id, v_class_6_id, 'B', 40
    ) ON CONFLICT (class_id, name) DO UPDATE SET capacity = 40
    RETURNING id INTO v_sec_6b_id;

    -- ── 12. Staff Record for Faculty Teacher ─────────────────────────────
    INSERT INTO staff (
        school_id, user_id, employee_id, first_name, last_name, email, phone,
        role, designation, department, assigned_class_id, assigned_section_id,
        qualification, date_of_joining, status
    ) VALUES (
        v_school_id,
        v_user_teacher_id,
        'EMP-DPA-001',
        'Sunita',
        'Rao',
        'teacher@delhipublicacademy.edu',
        '+91 98222 33445',
        'TEACHER',
        'Senior Mathematics Faculty & Class Teacher',
        'Mathematics',
        v_class_5_id,
        v_sec_5a_id,
        'M.Sc. Mathematics, B.Ed',
        '2021-06-15',
        'ACTIVE'
    ) ON CONFLICT (school_id, employee_id) DO UPDATE SET
        assigned_class_id = v_class_5_id,
        assigned_section_id = v_sec_5a_id,
        status = 'ACTIVE';

END $$;
