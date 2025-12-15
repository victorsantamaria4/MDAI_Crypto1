DROP TABLE IF EXISTS cartera_cripto;

-- =================================================================
-- LIMPIEZA INICIAL
-- =================================================================
DELETE FROM activos;
DELETE FROM transacciones;
DELETE FROM historiales;
DELETE FROM carteras;
DELETE FROM criptomonedas;
DELETE FROM usuarios;

-- Reinicia los contadores auto_increment
ALTER TABLE usuarios AUTO_INCREMENT = 1;
ALTER TABLE criptomonedas AUTO_INCREMENT = 1;
ALTER TABLE carteras AUTO_INCREMENT = 1;
ALTER TABLE historiales AUTO_INCREMENT = 1;
ALTER TABLE transacciones AUTO_INCREMENT = 1;
ALTER TABLE activos AUTO_INCREMENT = 1;

-- =================================================================
-- CARGA DE DATOS
-- =================================================================

-- 1. USUARIOS
INSERT INTO usuarios (nombre, email) VALUES ('Ana López', 'ana@email.com');   -- ID 1
INSERT INTO usuarios (nombre, email) VALUES ('Luis Marte', 'luis@email.com');  -- ID 2
INSERT INTO usuarios (nombre, email) VALUES ('Carla Diaz', 'carla@email.com'); -- ID 3

-- 2. CRIPTOMONEDAS
INSERT INTO criptomonedas (nombre, simbolo, precio_actual) VALUES ('Bitcoin', 'BTC', 90000.0);   -- ID 1
INSERT INTO criptomonedas (nombre, simbolo, precio_actual) VALUES ('Ethereum', 'ETH', 3000.0);   -- ID 2
INSERT INTO criptomonedas (nombre, simbolo, precio_actual) VALUES ('Solana', 'SOL', 140.0);      -- ID 3

-- 3. CARTERAS (REORDENADO PARA ID CONSECUTIVO)
-- Cartera Principal de Ana (ID 1)
INSERT INTO carteras (id_usuario, balance_total) VALUES (1, 1500.00);
-- Cartera Secundaria de Ana (ID 2)
INSERT INTO carteras (id_usuario, balance_total) VALUES (1, 100.00);

-- Cartera de Luis (ID 3)
INSERT INTO carteras (id_usuario, balance_total) VALUES (2, 500.00);

-- Cartera de Carla (ID 4)
INSERT INTO carteras (id_usuario, balance_total) VALUES (3, 2500.00);


-- 4. ACTIVOS (ACTUALIZADO A LOS NUEVOS IDs DE CARTERA)
-- Tabla: activos (id_cartera, id_cripto, cantidad)

-- Cartera 1 (Ana Principal): Tiene BTC y ETH
INSERT INTO activos (id_cartera, id_cripto, cantidad) VALUES (1, 1, 0.05);
INSERT INTO activos (id_cartera, id_cripto, cantidad) VALUES (1, 2, 2.0);

-- Cartera 2 (Ana Secundaria): Tiene SOL
INSERT INTO activos (id_cartera, id_cripto, cantidad) VALUES (2, 3, 10.0);

-- Cartera 3 (Luis): Tiene ETH
INSERT INTO activos (id_cartera, id_cripto, cantidad) VALUES (3, 2, 5.0);

-- Cartera 4 (Carla): Tiene BTC y SOL
INSERT INTO activos (id_cartera, id_cripto, cantidad) VALUES (4, 1, 0.1);
INSERT INTO activos (id_cartera, id_cripto, cantidad) VALUES (4, 3, 50.0);


-- 5. TRANSACCIONES
-- Ana (1) envía 1.5 ETH (2) a Luis (2)
INSERT INTO transacciones (id_usuario_origen, id_usuario_destino, id_cripto, cantidad, fecha)
VALUES (1, 2, 2, 1.5, '2025-10-28 10:30:00');

-- Ana (1) envía 0.01 BTC (1) a Carla (3)
INSERT INTO transacciones (id_usuario_origen, id_usuario_destino, id_cripto, cantidad, fecha)
VALUES (1, 3, 1, 0.01, '2025-10-29 12:00:00');

-- Luis (2) envía 10 SOL (3) a Carla (3)
INSERT INTO transacciones (id_usuario_origen, id_usuario_destino, id_cripto, cantidad, fecha)
VALUES (2, 3, 3, 10.0, '2025-10-30 14:45:00');


-- 6. HISTORIALES
INSERT INTO historiales (id_usuario, detalle)
VALUES (1, 'Historial de Ana López\n[ENV] TX 1.50 ETH a Luis Marte\n[ENV] TX 0.01 BTC a Carla Diaz');

INSERT INTO historiales (id_usuario, detalle)
VALUES (2, 'Historial de Luis Marte\n[REC] TX 1.50 ETH de Ana López\n[ENV] TX 10.00 SOL a Carla Diaz');

INSERT INTO historiales (id_usuario, detalle)
VALUES (3, 'Historial de Carla Diaz\n[REC] TX 0.01 BTC de Ana López\n[REC] TX 10.00 SOL de Luis Marte');