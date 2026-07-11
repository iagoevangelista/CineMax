-- ============================================================
-- sales_db: tablas y seed data para facturacion-service
-- Motor: SQL Server
-- Ejecutar DESPUÉS de 01-init-databases.sql (que crea sales_db)
-- ============================================================

USE sales_db;
GO

-- Catálogo interno de estados de la transacción (vive dentro de facturación,
-- no depende de ningún otro microservicio)
CREATE TABLE transaction_status (
    id_transaction_status INT IDENTITY(1,1) PRIMARY KEY,
    name_status VARCHAR(30) NOT NULL UNIQUE
);
GO

INSERT INTO transaction_status (name_status) VALUES
    ('PENDIENTE'),
    ('PAGADO'),
    ('CANCELADO');
GO

-- Transacción de venta. id_user es plano (dato dueño de usuarios-service).
-- No hay FK hacia usuarios_db porque son bases de datos distintas.
CREATE TABLE sale_transaction (
    id_transaction INT IDENTITY(1,1) PRIMARY KEY,
    id_user INT NOT NULL,
    id_transaction_status INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    payment_date DATETIME2 NULL,
    qr_code_data VARCHAR(250) NULL,
    CONSTRAINT fk_sale_transaction_status
        FOREIGN KEY (id_transaction_status) REFERENCES transaction_status(id_transaction_status)
);
GO

-- Detalle de entradas. id_showtime / id_seat son planos
-- (dueños: cartelera-service y sucursales-service respectivamente).
CREATE TABLE sale_ticket_detail (
    id_ticket INT IDENTITY(1,1) PRIMARY KEY,
    id_transaction INT NOT NULL,
    id_showtime INT NOT NULL,
    id_seat INT NOT NULL,
    ticket_price DECIMAL(10,2) NOT NULL,
    is_used BIT NOT NULL DEFAULT 0,
    CONSTRAINT fk_ticket_transaction
        FOREIGN KEY (id_transaction) REFERENCES sale_transaction(id_transaction),
    CONSTRAINT uq_ticket_seat_showtime UNIQUE (id_showtime, id_seat)
);
GO

-- Detalle de snacks. id_snack es plano (dueño: dulceria-service).
CREATE TABLE sale_snack_detail (
    id_detail INT IDENTITY(1,1) PRIMARY KEY,
    id_transaction INT NOT NULL,
    id_snack INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL,
    is_delivered BIT NOT NULL DEFAULT 0,
    CONSTRAINT fk_snack_detail_transaction
        FOREIGN KEY (id_transaction) REFERENCES sale_transaction(id_transaction)
);
GO