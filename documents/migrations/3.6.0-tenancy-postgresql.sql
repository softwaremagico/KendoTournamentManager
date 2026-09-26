-- Kendo Tournament Manager 3.6.0
-- Manual migration from a single-tenant installation to tenancy.
-- Run once, after taking a verified backup and before starting version 3.6.0.
-- PostgreSQL 14+

BEGIN;

CREATE TABLE IF NOT EXISTS tenants (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO tenants (name, active)
SELECT 'Legacy organization', TRUE
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE name = 'Legacy organization');

-- The application reserves tenant 1 for legacy/background data. Do not continue
-- against an installation where that invariant cannot be established.
DO $$
BEGIN
    IF (SELECT id FROM tenants WHERE name = 'Legacy organization') <> 1 THEN
        RAISE EXCEPTION 'Legacy organization must have tenant id 1; resolve existing tenant data before migration.';
    END IF;
END $$;

-- Add the ownership column to every entity that inherits Element.
ALTER TABLE achievements ADD COLUMN IF NOT EXISTS tenant_id INTEGER;
ALTER TABLE authenticated_users ADD COLUMN IF NOT EXISTS tenant_id INTEGER;
ALTER TABLE clubs ADD COLUMN IF NOT EXISTS tenant_id INTEGER;
ALTER TABLE duels ADD COLUMN IF NOT EXISTS tenant_id INTEGER;
ALTER TABLE fights ADD COLUMN IF NOT EXISTS tenant_id INTEGER;
ALTER TABLE groups_links ADD COLUMN IF NOT EXISTS tenant_id INTEGER;
ALTER TABLE participant_image ADD COLUMN IF NOT EXISTS tenant_id INTEGER;
ALTER TABLE participants ADD COLUMN IF NOT EXISTS tenant_id INTEGER;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS tenant_id INTEGER;
ALTER TABLE teams ADD COLUMN IF NOT EXISTS tenant_id INTEGER;
ALTER TABLE tournament_extra_properties ADD COLUMN IF NOT EXISTS tenant_id INTEGER;
ALTER TABLE tournament_groups ADD COLUMN IF NOT EXISTS tenant_id INTEGER;
ALTER TABLE tournament_image ADD COLUMN IF NOT EXISTS tenant_id INTEGER;
ALTER TABLE tournament_scores ADD COLUMN IF NOT EXISTS tenant_id INTEGER;
ALTER TABLE tournaments ADD COLUMN IF NOT EXISTS tenant_id INTEGER;

-- Existing data becomes private to the legacy organization.
DO $$
DECLARE
    legacy_tenant_id INTEGER;
    table_name TEXT;
BEGIN
    SELECT id INTO legacy_tenant_id FROM tenants WHERE name = 'Legacy organization';
    FOREACH table_name IN ARRAY ARRAY[
        'achievements', 'authenticated_users', 'clubs', 'duels', 'fights', 'groups_links',
        'participant_image', 'participants', 'roles', 'teams', 'tournament_extra_properties',
        'tournament_groups', 'tournament_image', 'tournament_scores', 'tournaments'
    ] LOOP
        EXECUTE format('UPDATE %I SET tenant_id = $1 WHERE tenant_id IS NULL', table_name) USING legacy_tenant_id;
        EXECUTE format('ALTER TABLE %I ALTER COLUMN tenant_id SET NOT NULL', table_name);
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = table_name || '_tenant_fk') THEN
            EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I FOREIGN KEY (tenant_id) REFERENCES tenants(id)',
                           table_name, table_name || '_tenant_fk');
        END IF;
        EXECUTE format('CREATE INDEX IF NOT EXISTS %I ON %I (tenant_id)', table_name || '_tenant_idx', table_name);
    END LOOP;
END $$;

-- Replace global business uniqueness with tenant-local uniqueness.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM authenticated_users
        GROUP BY tenant_id, username_hash HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate usernames exist in the legacy tenant; resolve them before migration.';
    END IF;
END $$;

ALTER TABLE clubs DROP CONSTRAINT IF EXISTS clubs_name_city_key;
ALTER TABLE tournaments DROP CONSTRAINT IF EXISTS tournaments_name_key;
ALTER TABLE participants DROP CONSTRAINT IF EXISTS participants_id_card_key;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'clubs_tenant_name_city_key') THEN
        ALTER TABLE clubs ADD CONSTRAINT clubs_tenant_name_city_key UNIQUE (tenant_id, name, city);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'tournaments_tenant_name_key') THEN
        ALTER TABLE tournaments ADD CONSTRAINT tournaments_tenant_name_key UNIQUE (tenant_id, name);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'participants_tenant_id_card_key') THEN
        ALTER TABLE participants ADD CONSTRAINT participants_tenant_id_card_key UNIQUE (tenant_id, id_card);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'authenticated_users_tenant_username_hash_key') THEN
        ALTER TABLE authenticated_users ADD CONSTRAINT authenticated_users_tenant_username_hash_key UNIQUE (tenant_id, username_hash);
    END IF;
END $$;

COMMIT;
