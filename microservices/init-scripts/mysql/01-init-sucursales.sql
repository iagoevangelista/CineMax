-- Sucursales DB
CREATE DATABASE IF NOT EXISTS sucursales_db;
CREATE DATABASE IF NOT EXISTS dulceria_db;

USE sucursales_db;

-- Tablas base de ubicación
CREATE TABLE IF NOT EXISTS department (
    id_department INT AUTO_INCREMENT PRIMARY KEY,
    name_department VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS province (
    id_province INT AUTO_INCREMENT PRIMARY KEY,
    name_province VARCHAR(100) NOT NULL,
    id_department INT NOT NULL,
    FOREIGN KEY (id_department) REFERENCES department(id_department)
);

CREATE TABLE IF NOT EXISTS district (
    id_district INT AUTO_INCREMENT PRIMARY KEY,
    name_district VARCHAR(100) NOT NULL,
    id_province INT NOT NULL,
    FOREIGN KEY (id_province) REFERENCES province(id_province)
);

-- Tipos de asiento
CREATE TABLE IF NOT EXISTS seat_type (
    id_seat_type INT AUTO_INCREMENT PRIMARY KEY,
    name_seat_type VARCHAR(50) NOT NULL
);

-- Seed data: departamentos, provincias, distritos
INSERT INTO department (name_department) VALUES ('Lima'), ('Arequipa'), ('Cusco') ON DUPLICATE KEY UPDATE name_department=name_department;
INSERT INTO seat_type (name_seat_type) VALUES ('General'), ('VIP'), ('Preferencial') ON DUPLICATE KEY UPDATE name_seat_type=name_seat_type;
