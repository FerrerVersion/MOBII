-- Historial de migraciones aplicadas sobre la base de datos
-- Estas sentencias se ejecutan de forma segura (try/catch) en database.js

-- v1.1 — añadir campo discapacidad a usuarios
ALTER TABLE usuarios ADD COLUMN discapacidad TEXT;
