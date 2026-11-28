-- Kendo Tournament Manager 3.7.1
-- Manual migration for credential-free self-registration.
-- Run once after taking a verified backup, before starting 3.7.1 with
-- spring.kendo.datasource.jpa.hibernate.ddl-auto=update.
-- PostgreSQL 14+

BEGIN;

-- 1. Revocable invitation version. Existing tournaments keep version 1, so
--    links generated after the upgrade remain valid until explicitly renewed.
ALTER TABLE tournaments ADD COLUMN IF NOT EXISTS registration_version INTEGER NOT NULL DEFAULT 1;

-- 2. Public registration metadata on roles.
ALTER TABLE roles ADD COLUMN IF NOT EXISTS team_preference VARCHAR(255);
ALTER TABLE roles ADD COLUMN IF NOT EXISTS pending_review BOOLEAN NOT NULL DEFAULT FALSE;

-- 3. A participant can hold several roles in the same tournament (for example
--    COMPETITOR and REFEREE). Remove historical duplicates before adding the
--    unique constraint; keep the oldest role row in each group.
DELETE FROM roles
WHERE id IN (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY tenant_id, tournament, participant, role_type
                   ORDER BY id
               ) AS duplicate_number
        FROM roles
    ) ranked
    WHERE ranked.duplicate_number > 1
);

-- PostgreSQL does not support ADD CONSTRAINT IF NOT EXISTS.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_roles_tenant_tournament_participant_type') THEN
        ALTER TABLE roles
            ADD CONSTRAINT uk_roles_tenant_tournament_participant_type
            UNIQUE (tenant_id, tournament, participant, role_type);
    END IF;
END $$;

COMMIT;
