INSERT INTO roles (id, role_name) VALUES (1, 'ADMIN');
INSERT INTO roles (id, role_name) VALUES (2, 'USER');

INSERT INTO permissions (id, permission_name) VALUES (1, 'READ');
INSERT INTO permissions (id, permission_name) VALUES (2, 'WRITE');
INSERT INTO permissions (id, permission_name) VALUES (3, 'UPDATE');
INSERT INTO permissions (id, permission_name) VALUES (4, 'DELETE');

INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 1);
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 2);
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 3);
INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 4);
INSERT INTO role_permissions (role_id, permission_id) VALUES (2, 1);

INSERT INTO users (first_name, last_name, email, password_hash) VALUES ("Admin", "Admin", "admin@admin.com", "$2a$10$4vev/0wKiDZ0KHHkNPZVeO1TCq6AcrpCupuN5EoYm/WP4NUGGv/l2");

INSERT INTO user_role (user_id, role_id) VALUES (1, 1);