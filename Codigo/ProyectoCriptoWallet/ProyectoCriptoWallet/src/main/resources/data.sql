-- Borra datos si existen para empezar de cero (útil para @DirtiesContext)
DELETE FROM cartera_cripto;
DELETE FROM transacciones;
DELETE FROM historiales;
DELETE FROM carteras;
DELETE FROM criptomonedas;
DELETE FROM usuarios;

-- Reinicia los contadores auto_increment (para MySQL)
ALTER TABLE usuarios AUTO_INCREMENT = 1;
ALTER TABLE criptomonedas AUTO_INCREMENT = 1;
ALTER TABLE carteras AUTO_INCREMENT = 1;
ALTER TABLE historiales AUTO_INCREMENT = 1;
ALTER TABLE transacciones AUTO_INCREMENT = 1;

-- 1. Insertar Usuarios
-- (ID 1)
INSERT INTO usuarios (nombre, email) VALUES ('Ana López', 'ana@email.com');
-- (ID 2)
INSERT INTO usuarios (nombre, email) VALUES ('Luis Marte', 'luis@email.com');
-- (ID 3)
INSERT INTO usuarios (nombre, email) VALUES ('Carla Diaz', 'carla@email.com');

-- 2. Insertar Criptomonedas
-- (ID 1)
INSERT INTO criptomonedas (nombre, simbolo) VALUES ('Bitcoin', 'BTC');
-- (ID 2)
INSERT INTO criptomonedas (nombre, simbolo) VALUES ('Ethereum', 'ETH');
-- (ID 3)
INSERT INTO criptomonedas (nombre, simbolo) VALUES ('Solana', 'SOL');

-- 3. Insertar Carteras (asociadas a usuarios)
-- (ID 1) Cartera de Ana (ID 1)
INSERT INTO carteras (id_usuario, balance_total) VALUES (1, 1500.00);
-- (ID 2) Cartera de Luis (ID 2)
INSERT INTO carteras (id_usuario, balance_total) VALUES (2, 500.00);
-- (ID 3) Cartera de Carla (ID 3)
INSERT INTO carteras (id_usuario, balance_total) VALUES (3, 2500.00);
-- (ID 4) Cartera secundaria de Ana (ID 1)
INSERT INTO carteras (id_usuario, balance_total) VALUES (1, 100.00);

-- 4. Insertar Historiales (1 a 1 con usuarios)
-- (ID 1) Historial de Ana (ID 1)
INSERT INTO historiales (id_usuario, detalle) VALUES (1, 'Historial de Ana López');
-- (ID 2) Historial de Luis (ID 2)
INSERT INTO historiales (id_usuario, detalle) VALUES (2, 'Historial de Luis Marte');
-- (ID 3) Historial de Carla (ID 3)
INSERT INTO historiales (id_usuario, detalle) VALUES (3, 'Historial de Carla Diaz');

-- 5. Insertar Transacciones (Origen, Destino, Cripto)
-- (ID 1) Ana (1) envía 1.5 ETH (2) a Luis (2)
INSERT INTO transacciones (id_usuario_origen, id_usuario_destino, id_cripto, cantidad, fecha)
VALUES (1, 2, 2, 1.5, '2025-10-28 10:30:00');
-- (ID 2) Ana (1) envía 0.5 BTC (1) a Carla (3)
INSERT INTO transacciones (id_usuario_origen, id_usuario_destino, id_cripto, cantidad, fecha)
VALUES (1, 3, 1, 0.5, '2025-10-29 12:00:00');
-- (ID 3) Luis (2) envía 10 SOL (3) a Carla (3)
INSERT INTO transacciones (id_usuario_origen, id_usuario_destino, id_cripto, cantidad, fecha)
VALUES (2, 3, 3, 10.0, '2025-10-30 14:45:00');

-- 6. Insertar Relaciones N:M (Cartera <-> Cripto)
-- Cartera 1 (de Ana) tiene BTC(1) y ETH(2)
INSERT INTO cartera_cripto (id_cartera, id_cripto) VALUES (1, 1);
INSERT INTO cartera_cripto (id_cartera, id_cripto) VALUES (1, 2);
-- Cartera 2 (de Luis) tiene ETH(2)
INSERT INTO cartera_cripto (id_cartera, id_cripto) VALUES (2, 2);
-- Cartera 3 (de Carla) tiene BTC(1) y SOL(3)
INSERT INTO cartera_cripto (id_cartera, id_cripto) VALUES (3, 1);
INSERT INTO cartera_cripto (id_cartera, id_cripto) VALUES (3, 3);
-- Cartera 4 (de Ana) tiene SOL(3)
INSERT INTO cartera_cripto (id_cartera, id_cripto) VALUES (4, 3);
