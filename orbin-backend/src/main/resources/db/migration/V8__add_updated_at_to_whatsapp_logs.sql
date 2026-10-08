-- ==========================================================================
-- V8__add_updated_at_to_whatsapp_logs.sql
-- Add updated_at column to whatsapp_logs to satisfy BaseEntity requirements
-- ==========================================================================

ALTER TABLE whatsapp_logs
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
