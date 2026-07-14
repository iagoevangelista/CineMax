-- Confiteria DB
CREATE DATABASE IF NOT EXISTS confiteria_db;
USE confiteria_db;

-- Categorias de snacks (ej: Bebidas, Dulces, Combos)
CREATE TABLE IF NOT EXISTS snack_category (
    id_snack_category INT AUTO_INCREMENT PRIMARY KEY,
    name_category VARCHAR(50) NOT NULL UNIQUE
);

-- Snacks (catalogo general, sin relacion directa a sucursal)
CREATE TABLE IF NOT EXISTS snack (
    id_snack INT AUTO_INCREMENT PRIMARY KEY,
    id_venue INT,
    id_snack_category INT NOT NULL,
    name_snack VARCHAR(100) NOT NULL,
    description_snack VARCHAR(250),
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    image_url_snack VARCHAR(250),
    status VARCHAR(20) NOT NULL DEFAULT 'Activo',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_snack_category) REFERENCES snack_category(id_snack_category)
);

-- Stock de cada snack por sucursal (id_venue viene de sucursales-service, sin FK cruzada)
CREATE TABLE IF NOT EXISTS snack_venue_stock (
    id_snack_venue_stock INT AUTO_INCREMENT PRIMARY KEY,
    id_snack INT NOT NULL,
    id_venue INT NOT NULL,
    stock INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Activo',
    FOREIGN KEY (id_snack) REFERENCES snack(id_snack)
);

-- Seed data: categorias
INSERT INTO snack_category (name_category) VALUES
    ('Bebidas'),
    ('Dulces'),
    ('Combos'),
    ('Snacks Salados')
ON DUPLICATE KEY UPDATE name_category = name_category;

-- Seed data: snacks de ejemplo
INSERT INTO snack (id_venue, id_snack_category, name_snack, description_snack, price, stock, image_url_snack, status) VALUES
    (1, 1, 'Gaseosa Grande', 'Bebida gaseosa 700ml', 8.50, 100, NULL, 'Activo'),
    (1, 2, 'Chocolate M&Ms', 'Bolsa de chocolate 150g', 6.00, 80, NULL, 'Activo'),
    (1, 3, 'Combo Pareja', 'Canchita grande + 2 gaseosas', 25.00, 50, NULL, 'Activo'),
    (1, 4, 'Canchita Salada', 'Canchita mediana salada', 10.00, 60, NULL, 'Activo')
ON DUPLICATE KEY UPDATE name_snack = name_snack;

-- Seed data: stock por sucursal
INSERT INTO snack_venue_stock (id_snack, id_venue, stock, status) VALUES
    (1, 1, 100, 'Activo'),
    (2, 1, 80, 'Activo'),
    (3, 1, 50, 'Activo'),
    (4, 1, 60, 'Activo')
ON DUPLICATE KEY UPDATE stock = stock;