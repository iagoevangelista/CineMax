-- ============================================
-- 01-init-usuarios.sql
-- Esquema RBAC para usuarios_db (PostgreSQL)
-- Los nombres de columna DEBEN coincidir exactamente
-- con las anotaciones @Column de Role, Permission y
-- DocumentType. Las tablas de negocio (user_account)
-- las crea Hibernate con ddl-auto=update.
-- ============================================

CREATE TABLE IF NOT EXISTS role (
    id_role   SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS permission (
    id_permission   SERIAL PRIMARY KEY,
    permission_name VARCHAR(50) NOT NULL UNIQUE
);

-- Tabla intermedia que exige el @ManyToMany de Role.java
CREATE TABLE IF NOT EXISTS role_permission (
    id_role       INT NOT NULL REFERENCES role(id_role) ON DELETE CASCADE,
    id_permission INT NOT NULL REFERENCES permission(id_permission) ON DELETE CASCADE,
    PRIMARY KEY (id_role, id_permission)
);

CREATE TABLE IF NOT EXISTS document_type (
    id_doc_type SERIAL PRIMARY KEY,
    doc_name    VARCHAR(50) NOT NULL UNIQUE
);

-- ============================================
-- SEED: Roles → el "QUÉ ES" el usuario
-- ============================================
INSERT INTO role (role_name) VALUES
    ('GERENTE_GENERAL'),
    ('GERENTE_MARKETING'),
    ('GERENTE_OPERACIONES'),
    ('CLIENTE')
ON CONFLICT (role_name) DO NOTHING;

-- ============================================
-- SEED: Permisos → el "QUÉ PUEDE HACER"
-- (nunca un nombre de rol aquí)
-- ============================================
INSERT INTO permission (permission_name) VALUES
    ('USER_READ'),
    ('USER_WRITE'),
    ('USER_DELETE'),
    ('ROLE_MANAGE'),
    ('VENUE_MANAGE'),
    ('BILLING_READ'),
    ('CATALOG_MANAGE')
ON CONFLICT (permission_name) DO NOTHING;

-- ============================================
-- SEED: Tipos de documento
-- ============================================
INSERT INTO document_type (doc_name) VALUES
    ('DNI'), ('CE'), ('PASAPORTE')
ON CONFLICT (doc_name) DO NOTHING;

-- ============================================
-- SEED: Relación Rol <-> Permiso (RBAC real)
-- Ajusta esta matriz a tus reglas de negocio reales;
-- lo importante es la estructura, no estos valores.
-- ============================================
INSERT INTO role_permission (id_role, id_permission)
SELECT r.id_role, p.id_permission
FROM role r, permission p
WHERE r.role_name = 'GERENTE_GENERAL'          -- admin del sistema: todos los permisos
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (id_role, id_permission)
SELECT r.id_role, p.id_permission
FROM role r, permission p
WHERE r.role_name = 'GERENTE_MARKETING'
  AND p.permission_name IN ('USER_READ', 'CATALOG_MANAGE')
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (id_role, id_permission)
SELECT r.id_role, p.id_permission
FROM role r, permission p
WHERE r.role_name = 'GERENTE_OPERACIONES'
  AND p.permission_name IN ('USER_READ', 'VENUE_MANAGE')
ON CONFLICT DO NOTHING;

INSERT INTO role_permission (id_role, id_permission)
SELECT r.id_role, p.id_permission
FROM role r, permission p
WHERE r.role_name = 'CLIENTE'
  AND p.permission_name = 'USER_READ'
ON CONFLICT DO NOTHING;