-- Datos iniciales: usuario administrador por defecto
-- Contraseña: admin123 (bcrypt hash generado en runtime por database.js)
-- Este script es solo de referencia; la inserción real la hace database.js al arrancar

INSERT INTO usuarios (nombre, email, password, rol)
VALUES ('Administrador', 'admin@mobii.com', '<hash_bcrypt>', 'admin');
