-- ==========================================================================
-- V4__whatsapp_logs.sql
-- WhatsApp Parent Notification Log Schema
-- ==========================================================================

CREATE TABLE IF NOT EXISTS whatsapp_logs (
    id                BIGSERIAL PRIMARY KEY,
    school_id         BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    recipient_name    VARCHAR(100) NOT NULL,
    recipient_phone   VARCHAR(25) NOT NULL,
    student_id        BIGINT REFERENCES students(id) ON DELETE SET NULL,
    student_name      VARCHAR(100),
    template_type     VARCHAR(50) NOT NULL,
    message_content   TEXT NOT NULL,
    status            VARCHAR(25) NOT NULL DEFAULT 'DELIVERED',
    meta_message_id   VARCHAR(100),
    sent_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_whatsapp_school_id ON whatsapp_logs(school_id);
CREATE INDEX IF NOT EXISTS idx_whatsapp_sent_at ON whatsapp_logs(school_id, sent_at DESC);
