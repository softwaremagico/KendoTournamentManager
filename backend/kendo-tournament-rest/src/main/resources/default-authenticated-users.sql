INSERT INTO authenticated_users (id, username, password, full_name) VALUES (1, 'admin@test.com', '$2a$12$hawW3GfY4/Ib/1.9KdVvVObw2t4FsXjkYApy5xlJf.P5GO3K72OSm', 'Admin User');
INSERT INTO roles (authenticated_user, roles) VALUES (1, 'admin');
INSERT INTO roles (authenticated_user, roles) VALUES (1, 'viewer');