const Database = require('better-sqlite3');
const path = require('path');

const DB_PATH = path.join(__dirname, 'mobii.db');
const db = new Database(DB_PATH);

db.pragma('journal_mode = WAL');
db.pragma('foreign_keys = ON');

db.exec(`
  CREATE TABLE IF NOT EXISTS usuarios (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre       TEXT    NOT NULL,
    email        TEXT    NOT NULL UNIQUE,
    password     TEXT    NOT NULL,
    rol          TEXT    NOT NULL DEFAULT 'usuario',
    activo       INTEGER NOT NULL DEFAULT 1,
    discapacidad TEXT,
    created_at   TEXT    NOT NULL DEFAULT (datetime('now'))
  );
`);

// Migraciones seguras
try { db.exec(`ALTER TABLE usuarios ADD COLUMN discapacidad TEXT;`); } catch (_) {}

db.exec(`
  CREATE TABLE IF NOT EXISTS parking_favoritos (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    user_email TEXT    NOT NULL,
    record_id  TEXT    NOT NULL,
    direccion  TEXT,
    created_at TEXT    NOT NULL DEFAULT (datetime('now'))
  );
  CREATE TABLE IF NOT EXISTS reservas (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    user_email TEXT    NOT NULL,
    nombre     TEXT,
    recogida   TEXT,
    destino    TEXT,
    fecha      TEXT,
    hora       TEXT,
    plazas     INTEGER,
    telefono   TEXT,
    notas      TEXT,
    created_at TEXT    NOT NULL DEFAULT (datetime('now'))
  );
`);

// Crear admin por defecto si no existe
const adminExiste = db.prepare('SELECT id FROM usuarios WHERE rol = ?').get('admin');
if (!adminExiste) {
  const bcrypt = require('bcryptjs');
  const hash = bcrypt.hashSync('admin123', 10);
  db.prepare(`
    INSERT INTO usuarios (nombre, email, password, rol)
    VALUES (?, ?, ?, ?)
  `).run('Administrador', 'admin@mobii.com', hash, 'admin');
  console.log('Admin creado: admin@mobii.com / admin123');
}

module.exports = db;
