CREATE TABLE IF NOT EXISTS snack_category (
    id_snack_category INT AUTO_INCREMENT PRIMARY KEY,
    name_category VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS snack (
    id_snack INT AUTO_INCREMENT PRIMARY KEY,
    id_venue INT,
    id_snack_category INT NOT NULL,
    name_snack VARCHAR(100) NOT NULL,
    description_snack VARCHAR(250),
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    image_url_snack VARCHAR(250),
    status VARCHAR(20) DEFAULT 'Activo',
    created_at DATETIME,
    FOREIGN KEY (id_snack_category) REFERENCES snack_category(id_snack_category)
);

CREATE TABLE IF NOT EXISTS snack_venue_stock (
    id_snack_venue_stock INT AUTO_INCREMENT PRIMARY KEY,
    id_snack INT NOT NULL,
    id_venue INT NOT NULL,
    stock INT NOT NULL,
    status VARCHAR(20) DEFAULT 'Activo',
    FOREIGN KEY (id_snack) REFERENCES snack(id_snack)
);

-- Idempotente: si el script se corre manualmente más de una vez, limpia antes de insertar
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE snack_venue_stock;
TRUNCATE TABLE snack;
TRUNCATE TABLE snack_category;
SET FOREIGN_KEY_CHECKS = 1;

-- ---- SNACK_CATEGORY ----
INSERT INTO snack_category (name_category) VALUES
('Bebidas'),
('Combos'),
('Dulces'),
('Salados');

-- ---- SNACK ----
-- id_venue usa los mismos venueId que ya tienes en los showtimes de cartelera (1 y 2)
-- para que el frontend muestre cosas consistentes en ambas sucursales.
INSERT INTO snack (id_venue, id_snack_category, name_snack, description_snack, price, stock, image_url_snack, status) VALUES
(1, (SELECT id_snack_category FROM snack_category WHERE name_category = 'Bebidas'),
    'Gaseosa Grande', 'Vaso grande de gaseosa a elección', 8.50, 100, '', 'Activo'),

(1, (SELECT id_snack_category FROM snack_category WHERE name_category = 'Bebidas'),
    'Agua Mineral', 'Botella de agua mineral 500ml', 5.00, 150, '', 'Activo'),

(1, (SELECT id_snack_category FROM snack_category WHERE name_category = 'Combos'),
    'Combo Pareja', 'Canchita grande + 2 gaseosas medianas', 25.00, 40, '', 'Activo'),

(1, (SELECT id_snack_category FROM snack_category WHERE name_category = 'Combos'),
    'Combo Familiar', 'Canchita extra grande + 4 gaseosas + dulces', 45.00, 25, '', 'Activo'),

(1, (SELECT id_snack_category FROM snack_category WHERE name_category = 'Dulces'),
    'Chocolate M&Ms', 'Bolsa mediana de chocolates M&Ms', 9.00, 80, '', 'Activo'),

(1, (SELECT id_snack_category FROM snack_category WHERE name_category = 'Salados'),
    'Canchita Grande', 'Canchita salada tamaño grande', 12.00, 60, '', 'Activo'),

(1, (SELECT id_snack_category FROM snack_category WHERE name_category = 'Salados'),
    'Nachos con Queso', 'Nachos con salsa de queso cheddar', 15.00, 50, '', 'Activo');

-- ---- SNACK_VENUE_STOCK ----
-- Stock por sucursal (venueId 1 y 2) para cada snack recién creado.
INSERT INTO snack_venue_stock (id_snack, id_venue, stock, status)
SELECT id_snack, 1, stock, 'Activo' FROM snack;

INSERT INTO snack_venue_stock (id_snack, id_venue, stock, status)
SELECT id_snack, 2, FLOOR(stock * 0.7), 'Activo' FROM snack;