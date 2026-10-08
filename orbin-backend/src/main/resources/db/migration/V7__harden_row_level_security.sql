-- ==========================================================================
-- V7__harden_row_level_security.sql
-- Hardens PostgreSQL Row-Level Security:
-- Eliminates unauthenticated NULL/empty bypass for normal tenant operations.
-- Strictly enforces: school_id = app.current_school_id OR app.is_platform_admin = 'true'
-- ==========================================================================

DROP POLICY IF EXISTS tenant_isolation_students ON students;
CREATE POLICY tenant_isolation_students ON students
    USING (
        current_setting('app.is_platform_admin', true) = 'true'
        OR school_id = NULLIF(current_setting('app.current_school_id', true), '')::bigint
    );

DROP POLICY IF EXISTS tenant_isolation_staff ON staff;
CREATE POLICY tenant_isolation_staff ON staff
    USING (
        current_setting('app.is_platform_admin', true) = 'true'
        OR school_id = NULLIF(current_setting('app.current_school_id', true), '')::bigint
    );

DROP POLICY IF EXISTS tenant_isolation_attendance ON attendance;
CREATE POLICY tenant_isolation_attendance ON attendance
    USING (
        current_setting('app.is_platform_admin', true) = 'true'
        OR school_id = NULLIF(current_setting('app.current_school_id', true), '')::bigint
    );

DROP POLICY IF EXISTS tenant_isolation_student_fees ON student_fees;
CREATE POLICY tenant_isolation_student_fees ON student_fees
    USING (
        current_setting('app.is_platform_admin', true) = 'true'
        OR school_id = NULLIF(current_setting('app.current_school_id', true), '')::bigint
    );

DROP POLICY IF EXISTS tenant_isolation_payments ON payments;
CREATE POLICY tenant_isolation_payments ON payments
    USING (
        current_setting('app.is_platform_admin', true) = 'true'
        OR school_id = NULLIF(current_setting('app.current_school_id', true), '')::bigint
    );

DROP POLICY IF EXISTS tenant_isolation_whatsapp_logs ON whatsapp_logs;
CREATE POLICY tenant_isolation_whatsapp_logs ON whatsapp_logs
    USING (
        current_setting('app.is_platform_admin', true) = 'true'
        OR school_id = NULLIF(current_setting('app.current_school_id', true), '')::bigint
    );
