const express = require('express');
const bcrypt = require('bcryptjs');
const db = require('../db/database');
const { verificarToken, soloAdmin } = require('../middleware/auth');

const router = express.Router();

router.use(verificarToken, soloAdmin);

router.get('/', (req, res) => {
  const usuarios = db.prepare(
    'SELECT id, nombre, email, rol, activo, discapacidad, created_at FROM usuarios ORDER BY id ASC'
  ).all();
  res.json(usuarios);
});

router.get('/:id/favoritos', (req, res) => {
  const usuario = db.prepare('SELECT email FROM usuarios WHERE id = ?').get(req.params.id);
  if (!usuario) return res.status(404).json({ error: 'Usuario no encontrado' });
  const favoritos = db.prepare(
    'SELECT id, record_id, direccion, created_at FROM parking_favoritos WHERE user_email = ? ORDER BY created_at DESC'
  ).all(usuario.email);
  res.json(favoritos);
});

router.get('/:id/reservas', (req, res) => {
  const usuario = db.prepare('SELECT email FROM usuarios WHERE id = ?').get(req.params.id);
  if (!usuario) return res.status(404).json({ error: 'Usuario no encontrado' });
  const reservas = db.prepare(
    'SELECT id, nombre, recogida, destino, fecha, hora, plazas, telefono, notas, created_at FROM reservas WHERE user_email = ? ORDER BY fecha DESC'
  ).all(usuario.email);
  res.json(reservas);
});

router.get('/:id', (req, res) => {
  const usuario = db.prepare(
    'SELECT id, nombre, email, rol, activo, discapacidad, created_at FROM usuarios WHERE id = ?'
  ).get(req.params.id);
  if (!usuario) return res.status(404).json({ error: 'Usuario no encontrado' });
  res.json(usuario);
});

// el admin puede asignar rol al crear
router.post('/', (req, res) => {
  const { nombre, email, password, rol = 'usuario' } = req.body;
  if (!nombre || !email || !password) {
    return res.status(400).json({ error: 'nombre, email y password son obligatorios' });
  }

  const existe = db.prepare('SELECT id FROM usuarios WHERE email = ?').get(email);
  if (existe) return res.status(409).json({ error: 'El email ya está registrado' });

  const hash = bcrypt.hashSync(password, 10);
  const result = db.prepare(`
    INSERT INTO usuarios (nombre, email, password, rol)
    VALUES (?, ?, ?, ?)
  `).run(nombre, email, hash, rol);

  const nuevo = db.prepare('SELECT id, nombre, email, rol, activo, created_at FROM usuarios WHERE id = ?').get(result.lastInsertRowid);
  res.status(201).json(nuevo);
});

router.put('/:id', (req, res) => {
  const { nombre, email, password, rol, activo } = req.body;
  const usuario = db.prepare('SELECT * FROM usuarios WHERE id = ?').get(req.params.id);
  if (!usuario) return res.status(404).json({ error: 'Usuario no encontrado' });

  const nuevoNombre = nombre ?? usuario.nombre;
  const nuevoEmail = email ?? usuario.email;
  const nuevoRol = rol ?? usuario.rol;
  const nuevoActivo = activo !== undefined ? (activo ? 1 : 0) : usuario.activo;

  if (email && email !== usuario.email) {
    const duplicado = db.prepare('SELECT id FROM usuarios WHERE email = ? AND id != ?').get(email, req.params.id);
    if (duplicado) return res.status(409).json({ error: 'El email ya está en uso' });
  }

  if (password) {
    const hash = bcrypt.hashSync(password, 10);
    db.prepare(`
      UPDATE usuarios SET nombre=?, email=?, password=?, rol=?, activo=? WHERE id=?
    `).run(nuevoNombre, nuevoEmail, hash, nuevoRol, nuevoActivo, req.params.id);
  } else {
    db.prepare(`
      UPDATE usuarios SET nombre=?, email=?, rol=?, activo=? WHERE id=?
    `).run(nuevoNombre, nuevoEmail, nuevoRol, nuevoActivo, req.params.id);
  }

  const actualizado = db.prepare('SELECT id, nombre, email, rol, activo, created_at FROM usuarios WHERE id = ?').get(req.params.id);
  res.json(actualizado);
});

router.delete('/:id', (req, res) => {
  const usuario = db.prepare('SELECT id, rol FROM usuarios WHERE id = ?').get(req.params.id);
  if (!usuario) return res.status(404).json({ error: 'Usuario no encontrado' });
  if (usuario.rol === 'admin' && usuario.id === req.usuario.id) {
    return res.status(400).json({ error: 'No puedes eliminar tu propio usuario admin' });
  }

  db.prepare('DELETE FROM usuarios WHERE id = ?').run(req.params.id);
  res.json({ mensaje: 'Usuario eliminado correctamente' });
});

module.exports = router;
