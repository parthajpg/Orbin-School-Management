-- ==========================================================================
-- V5__row_level_security.sql
-- Database-Enforced Multi-Tenant Isolation via PostgreSQL Row-Level Security (RLS)
-- Prevents any cross-school data leak even if application queries omit school_id
-- ==========================================================================

-- ── Enable RLS on Tenant-Owned Tables ─────────────────────────────────────
ALTER TABLE students ENABLE ROW LEVEL SECURITY;
ALTER TABLE staff ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendance ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_fees ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE whatsapp_logs ENABLE ROW LEVEL SECURITY;

-- ── Define Tenant Isolation Policies ───────────────────────────────────────
-- Condition: row's school_id must match session variable 'app.current_school_id'
-- Or allow if current_setting is not set (e.g., migrations / super admin bypass)

DROP POLICY IF EXISTS tenant_isolation_students ON students;
CREATE POLICY tenant_isolation_students ON students
    USING (
        current_setting('app.current_school_id', true) IS NULL
        OR current_setting('app.current_school_id', true) = ''
        OR school_id = current_setting('app.current_school_id', true)::bigint
    );

DROP POLICY IF EXISTS tenant_isolation_staff ON staff;
CREATE POLICY tenant_isolation_staff ON staff
    USING (
        current_setting('app.current_school_id', true) IS NULL
        OR current_setting('app.current_school_id', true) = ''
        OR school_id = current_setting('app.current_school_id', true)::bigint
    );

DROP POLICY IF EXISTS tenant_isolation_attendance ON attendance;
CREATE POLICY tenant_isolation_attendance ON attendance
    USING (
        current_setting('app.current_school_id', true) IS NULL
        OR current_setting('app.current_school_id', true) = ''
        OR school_id = current_setting('app.current_school_id', true)::bigint
    );

DROP POLICY IF EXISTS tenant_isolation_student_fees ON student_fees;
CREATE POLICY tenant_isolation_student_fees ON student_fees
    USING (
        current_setting('app.current_school_id', true) IS NULL
        OR current_setting('app.current_school_id', true) = ''
        OR school_id = current_setting('app.current_school_id', true)::bigint
    );

DROP POLICY IF EXISTS tenant_isolation_payments ON payments;
CREATE POLICY tenant_isolation_payments ON payments
    USING (
        current_setting('app.current_school_id', true) IS NULL
        OR current_setting('app.current_school_id', true) = ''
        OR school_id = current_setting('app.current_school_id', true)::bigint
    );

DROP POLICY IF EXISTS tenant_isolation_whatsapp_logs ON whatsapp_logs;
CREATE POLICY tenant_isolation_whatsapp_logs ON whatsapp_logs
    USING (
        current_setting('app.current_school_id', true) IS NULL
        OR current_setting('app.current_school_id', true) = ''
        OR school_id = current_setting('app.current_school_id', true)::bigint
    );
