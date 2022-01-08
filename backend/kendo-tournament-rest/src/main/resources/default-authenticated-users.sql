***REMOVED***
***REMOVED***
***REMOVED***
***REMOVED***
***REMOVED***Copyright (C) 2021 - 2022 Softwaremagico
***REMOVED***
***REMOVED***This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
***REMOVED***<softwaremagico@gmail.com> Valencia (Spain).
***REMOVED*** 
***REMOVED***This program is free software; you can redistribute it and/or modify it under
***REMOVED***the terms of the GNU General Public License as published by the Free Software
***REMOVED***Foundation; either version 2 of the License, or (at your option) any later
***REMOVED***version.
***REMOVED*** 
***REMOVED***This program is distributed in the hope that it will be useful, but WITHOUT
***REMOVED***ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
***REMOVED***FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
***REMOVED***details.
***REMOVED*** 
***REMOVED***You should have received a copy of the GNU General Public License along with
***REMOVED***this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
***REMOVED***#L%
***REMOVED***
INSERT INTO authenticated_users (id, username, password, full_name) VALUES (1, 'admin@test.com', '$2a$12$hawW3GfY4/Ib/1.9KdVvVObw2t4FsXjkYApy5xlJf.P5GO3K72OSm', 'Admin User');
INSERT INTO roles (authenticated_user, roles) VALUES (1, 'admin');
INSERT INTO roles (authenticated_user, roles) VALUES (1, 'viewer');
