-- Kendo Tournament Manager 3.7.1
-- Manual migration for credential-free self-registration.
-- Run once after taking a verified backup, before starting 3.7.1 with
-- spring.kendo.datasource.jpa.hibernate.ddl-auto=update.
-- MySQL 8.0.29+ (required for ADD COLUMN IF NOT EXISTS and window functions).
-- MySQL commits DDL implicitly, so restore the backup rather than rerunning a
-- partially applied script.

-- 1. Revocable invitation version. Existing tournaments keep version 1, so
--    links generated after the upgrade remain valid until explicitly renewed.
ALTER TABLE tournaments ADD COLUMN IF NOT EXISTS registration_version INT NOT NULL DEFAULT 1;

-- 2. Public registration metadata on roles.
ALTER TABLE roles ADD COLUMN IF NOT EXISTS team_preference VARCHAR(255) NULL;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS pending_review BIT NOT NULL DEFAULT b'0';

-- 3. A participant can hold several roles in the same tournament (for example
--    COMPETITOR and REFEREE). Remove historical duplicates before adding the
--    unique constraint; keep the oldest role row in each group.
DELETE r FROM roles r
JOIN (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY tenant_id, tournament, participant, role_type
               ORDER BY id
           ) AS duplicate_number
    FROM roles
) ranked ON ranked.id = r.id
WHERE ranked.duplicate_number > 1;

-- MySQL does not support ADD CONSTRAINT IF NOT EXISTS, so inspect the unique
-- indexes by their columns before creating the new one.
SET @role_unique_name = (
    SELECT index_name
    FROM (
        SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns_in_index
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'roles'
          AND non_unique = 0
          AND index_name <> 'PRIMARY'
        GROUP BY index_name
    ) indexes
    WHERE columns_in_index = 'tenant_id,tournament,participant,role_type'
    LIMIT 1
);

SET @add_role_unique = IF(@role_unique_name IS NULL,
    'ALTER TABLE roles ADD CONSTRAINT uk_roles_tenant_tournament_participant_type UNIQUE (tenant_id, tournament, participant, role_type)',
    'SELECT 1');
PREPARE role_unique_statement FROM @add_role_unique;
EXECUTE role_unique_statement;
DEALLOCATE PREPARE role_unique_statement;
