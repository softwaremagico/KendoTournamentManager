-- Kendo Tournament Manager 3.6.0
-- Manual migration from a single-tenant installation to tenancy.
-- Run once, after taking a verified backup and before starting version 3.6.0.
-- MySQL 8.0.29+ (required for ADD COLUMN IF NOT EXISTS)
-- MySQL commits DDL implicitly. Restore the backup rather than rerunning a
-- partially completed script.

CREATE TABLE IF NOT EXISTS tenants (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    active BIT NOT NULL DEFAULT b'1',
    PRIMARY KEY (id),
    UNIQUE KEY tenants_name_key (name)
);

INSERT INTO tenants (name, active)
SELECT 'Legacy organization', b'1'
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE name = 'Legacy organization');

SET @legacy_tenant_id = (SELECT id FROM tenants WHERE name = 'Legacy organization');

-- The application reserves tenant 1 for legacy/background data.
-- Abort manually if this query does not return 1 before continuing:
SELECT id AS legacy_tenant_id_must_be_1 FROM tenants WHERE name = 'Legacy organization';

-- Add, backfill and protect every entity that inherits Element.
ALTER TABLE achievements ADD COLUMN IF NOT EXISTS tenant_id INT NULL;
ALTER TABLE authenticated_users ADD COLUMN IF NOT EXISTS tenant_id INT NULL;
ALTER TABLE clubs ADD COLUMN IF NOT EXISTS tenant_id INT NULL;
ALTER TABLE duels ADD COLUMN IF NOT EXISTS tenant_id INT NULL;
ALTER TABLE fights ADD COLUMN IF NOT EXISTS tenant_id INT NULL;
ALTER TABLE groups_links ADD COLUMN IF NOT EXISTS tenant_id INT NULL;
ALTER TABLE participant_image ADD COLUMN IF NOT EXISTS tenant_id INT NULL;
ALTER TABLE participants ADD COLUMN IF NOT EXISTS tenant_id INT NULL;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS tenant_id INT NULL;
ALTER TABLE teams ADD COLUMN IF NOT EXISTS tenant_id INT NULL;
ALTER TABLE tournament_extra_properties ADD COLUMN IF NOT EXISTS tenant_id INT NULL;
ALTER TABLE tournament_groups ADD COLUMN IF NOT EXISTS tenant_id INT NULL;
ALTER TABLE tournament_image ADD COLUMN IF NOT EXISTS tenant_id INT NULL;
ALTER TABLE tournament_scores ADD COLUMN IF NOT EXISTS tenant_id INT NULL;
ALTER TABLE tournaments ADD COLUMN IF NOT EXISTS tenant_id INT NULL;

UPDATE achievements SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;
UPDATE authenticated_users SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;
UPDATE clubs SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;
UPDATE duels SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;
UPDATE fights SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;
UPDATE groups_links SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;
UPDATE participant_image SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;
UPDATE participants SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;
UPDATE roles SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;
UPDATE teams SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;
UPDATE tournament_extra_properties SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;
UPDATE tournament_groups SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;
UPDATE tournament_image SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;
UPDATE tournament_scores SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;
UPDATE tournaments SET tenant_id = @legacy_tenant_id WHERE tenant_id IS NULL;

ALTER TABLE achievements MODIFY tenant_id INT NOT NULL;
ALTER TABLE authenticated_users MODIFY tenant_id INT NOT NULL;
ALTER TABLE clubs MODIFY tenant_id INT NOT NULL;
ALTER TABLE duels MODIFY tenant_id INT NOT NULL;
ALTER TABLE fights MODIFY tenant_id INT NOT NULL;
ALTER TABLE groups_links MODIFY tenant_id INT NOT NULL;
ALTER TABLE participant_image MODIFY tenant_id INT NOT NULL;
ALTER TABLE participants MODIFY tenant_id INT NOT NULL;
ALTER TABLE roles MODIFY tenant_id INT NOT NULL;
ALTER TABLE teams MODIFY tenant_id INT NOT NULL;
ALTER TABLE tournament_extra_properties MODIFY tenant_id INT NOT NULL;
ALTER TABLE tournament_groups MODIFY tenant_id INT NOT NULL;
ALTER TABLE tournament_image MODIFY tenant_id INT NOT NULL;
ALTER TABLE tournament_scores MODIFY tenant_id INT NOT NULL;
ALTER TABLE tournaments MODIFY tenant_id INT NOT NULL;

-- Remove old global unique indexes regardless of their generated names.
SET @drop_clubs_index = (
    SELECT index_name FROM (
        SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns_in_index
        FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'clubs' AND non_unique = 0 AND index_name <> 'PRIMARY'
        GROUP BY index_name
    ) indexes WHERE columns_in_index = 'name,city' LIMIT 1
);
SET @drop_tournaments_index = (
    SELECT index_name FROM (
        SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns_in_index
        FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'tournaments' AND non_unique = 0 AND index_name <> 'PRIMARY'
        GROUP BY index_name
    ) indexes WHERE columns_in_index = 'name' LIMIT 1
);
SET @drop_participants_index = (
    SELECT index_name FROM (
        SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns_in_index
        FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'participants' AND non_unique = 0 AND index_name <> 'PRIMARY'
        GROUP BY index_name
    ) indexes WHERE columns_in_index = 'id_card' LIMIT 1
);
SET @sql = IF(@drop_clubs_index IS NULL, 'SELECT 1', CONCAT('ALTER TABLE clubs DROP INDEX `', @drop_clubs_index, '`'));
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;
SET @sql = IF(@drop_tournaments_index IS NULL, 'SELECT 1', CONCAT('ALTER TABLE tournaments DROP INDEX `', @drop_tournaments_index, '`'));
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;
SET @sql = IF(@drop_participants_index IS NULL, 'SELECT 1', CONCAT('ALTER TABLE participants DROP INDEX `', @drop_participants_index, '`'));
PREPARE statement FROM @sql; EXECUTE statement; DEALLOCATE PREPARE statement;

ALTER TABLE clubs ADD UNIQUE KEY clubs_tenant_name_city_key (tenant_id, name, city);
ALTER TABLE tournaments ADD UNIQUE KEY tournaments_tenant_name_key (tenant_id, name);
ALTER TABLE participants ADD UNIQUE KEY participants_tenant_id_card_key (tenant_id, id_card);
ALTER TABLE authenticated_users ADD UNIQUE KEY authenticated_users_tenant_username_hash_key (tenant_id, username_hash);

CREATE INDEX achievements_tenant_idx ON achievements (tenant_id);
CREATE INDEX authenticated_users_tenant_idx ON authenticated_users (tenant_id);
CREATE INDEX duels_tenant_idx ON duels (tenant_id);
CREATE INDEX fights_tenant_idx ON fights (tenant_id);
CREATE INDEX groups_links_tenant_idx ON groups_links (tenant_id);
CREATE INDEX participant_image_tenant_idx ON participant_image (tenant_id);
CREATE INDEX roles_tenant_idx ON roles (tenant_id);
CREATE INDEX teams_tenant_idx ON teams (tenant_id);
CREATE INDEX tournament_extra_properties_tenant_idx ON tournament_extra_properties (tenant_id);
CREATE INDEX tournament_groups_tenant_idx ON tournament_groups (tenant_id);
CREATE INDEX tournament_image_tenant_idx ON tournament_image (tenant_id);
CREATE INDEX tournament_scores_tenant_idx ON tournament_scores (tenant_id);

ALTER TABLE achievements ADD CONSTRAINT achievements_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE authenticated_users ADD CONSTRAINT authenticated_users_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE clubs ADD CONSTRAINT clubs_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE duels ADD CONSTRAINT duels_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE fights ADD CONSTRAINT fights_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE groups_links ADD CONSTRAINT groups_links_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE participant_image ADD CONSTRAINT participant_image_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE participants ADD CONSTRAINT participants_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE roles ADD CONSTRAINT roles_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE teams ADD CONSTRAINT teams_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE tournament_extra_properties ADD CONSTRAINT tournament_extra_properties_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE tournament_groups ADD CONSTRAINT tournament_groups_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE tournament_image ADD CONSTRAINT tournament_image_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE tournament_scores ADD CONSTRAINT tournament_scores_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE tournaments ADD CONSTRAINT tournaments_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id);
