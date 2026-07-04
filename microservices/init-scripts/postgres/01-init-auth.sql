-- Auth DB: crear esquema y seed data
CREATE DATABASE auth_db;

\c auth_db;

-- Roles base
CREATE TABLE IF NOT EXISTS role (
    id_role SERIAL PRIMARY KEY,
    name_role VARCHAR(30) NOT NULL UNIQUE
);

-- Permisos base
CREATE TABLE IF NOT EXISTS permission (
    id_permission SERIAL PRIMARY KEY,
    name_permission VARCHAR(50) NOT NULL UNIQUE
);

-- Tipos de documento
CREATE TABLE IF NOT EXISTS document_type (
    id_document_type SERIAL PRIMARY KEY,
    name_document VARCHAR(30) NOT NULL UNIQUE
);

-- Seed data
INSERT INTO role (name_role) VALUES ('ADMIN'), ('CLIENTE') ON CONFLICT (name_role) DO NOTHING;
INSERT INTO document_type (name_document) VALUES ('DNI'), ('CE'), ('PASAPORTE') ON CONFLICT (name_document) DO NOTHING;
INSERT INTO permission (name_permission) VALUES ('READ'), ('WRITE'), ('ADMIN') ON CONFLICT (name_permission) DO NOTHING;
