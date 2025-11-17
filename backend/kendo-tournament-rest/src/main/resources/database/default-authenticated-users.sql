---
-- #%L
-- Kendo Tournament Manager (Rest)
-- %%
-- Copyright (C) 2021 - 2025 Softwaremagico
-- %%
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
-- 
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
-- 
-- You should have received a copy of the GNU Affero General Public License
-- along with this program.  If not, see <http://www.gnu.org/licenses/>.
-- #L%
---
INSERT INTO authenticated_users (id, username, password, name, lastname) VALUES (1, 'admin@test.com', '$2y$salt$redactedhash:19000:0:99999:7:::', 'Admin', 'User');
INSERT INTO authenticated_users (id, username, password, name, lastname) VALUES (2, 'viewer@test.com', '$2y$salt$redactedhash:19000:0:99999:7:::', 'Viewer', 'User');
INSERT INTO authenticated_users (id, username, password, name, lastname) VALUES (3, 'editor@test.com', '$2y$salt$redactedhash:19000:0:99999:7:::', 'Editor', 'User');
INSERT INTO authenticated_user_roles (authenticated_user, roles) VALUES (1, 'admin');
INSERT INTO authenticated_user_roles (authenticated_user, roles) VALUES (1, 'viewer');
INSERT INTO authenticated_user_roles (authenticated_user, roles) VALUES (2, 'viewer');
INSERT INTO authenticated_user_roles (authenticated_user, roles) VALUES (3, 'editor');
