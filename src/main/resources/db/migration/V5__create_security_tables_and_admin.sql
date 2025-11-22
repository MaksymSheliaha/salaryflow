-- СТВОРЕННЯ ТАБЛИЦІ КОРИСТУВАЧІВ (users)
CREATE TABLE "users" (
                         id UUID PRIMARY KEY,
                         username VARCHAR(50) UNIQUE NOT NULL,
                         password VARCHAR(255) NOT NULL,
                         enabled BOOLEAN NOT NULL DEFAULT TRUE,
                         created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- СТВОРЕННЯ ТАБЛИЦІ РОЛЕЙ (roles)
CREATE TABLE "roles" (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(50) UNIQUE NOT NULL
);

-- СТВОРЕННЯ ЗВ'ЯЗУЮЧОЇ ТАБЛИЦІ (user_roles)
CREATE TABLE "user_roles" (
                              user_id UUID NOT NULL,
                              role_id INTEGER NOT NULL,
                              PRIMARY KEY (user_id, role_id),
                              FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                              FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- ДОДАВАННЯ РОЛЕЙ
INSERT INTO roles (name) VALUES ('ROLE_ADMIN'), ('ROLE_MANAGER');