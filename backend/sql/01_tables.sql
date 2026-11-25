-- Esquema de la base de datos MOBII
-- Motor: SQLite (better-sqlite3)

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
