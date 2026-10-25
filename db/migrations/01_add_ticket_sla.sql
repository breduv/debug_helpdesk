-- Apply once to an existing database; rerunning is safe.
BEGIN;

ALTER TABLE tickets ADD COLUMN IF NOT EXISTS due_at TIMESTAMPTZ;

-- Existing created_at values use the PostgreSQL session time zone.
-- This backfill is only for legacy data; new deadlines are calculated in Service.
UPDATE tickets
SET due_at = (created_at AT TIME ZONE current_setting('TimeZone'))
    + CASE priority
        WHEN 'CRITICAL' THEN INTERVAL '1 hour'
        WHEN 'HIGH' THEN INTERVAL '4 hours'
        WHEN 'MEDIUM' THEN INTERVAL '8 hours'
        WHEN 'LOW' THEN INTERVAL '24 hours'
      END
WHERE due_at IS NULL;

ALTER TABLE tickets ALTER COLUMN due_at SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_tickets_overdue ON tickets(due_at, id)
    WHERE status NOT IN ('RESOLVED', 'CLOSED');

COMMIT;
