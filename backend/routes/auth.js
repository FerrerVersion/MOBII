const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const db = require('../db/database');
const { JWT_SECRET, verificarToken } = require('../middleware/auth');

const router = express.Router();

router.post('/register', (req, res) => {
  const { nombre, email, password } = req.body;
  if (!nombre || !email || !password) {
    return res.status(400).json({ error: 'Todos los campos son obligatorios' });
  }
  if (password.length < 6) {
    return res.status(400).json({ error: 'La contraseña debe tener al menos 6 caracteres' });
  }

  const existe = db.prepare('SELECT id FROM usuarios WHERE email = ?').get(email);
  if (existe) return res.status(409).json({ error: 'El email ya está registrado' });

  const hash = bcrypt.hashSync(password, 10);
  const result = db.prepare(`
    INSERT INTO usuarios (nombre, email, password, rol)
    VALUES (?, ?, ?, 'usuario')
  `).run(nombre, email, hash);

  const usuario = db.prepare('SELECT id, nombre, email, rol, created_at FROM usuarios WHERE id = ?').get(result.lastInsertRowid);
  const token = jwt.sign({ id: usuario.id, email: usuario.email, rol: usuario.rol }, JWT_SECRET, { expiresIn: '7d' });

  res.status(201).json({ token, usuario });
});

router.post('/login', (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ error: 'Email y contraseña son obligatorios' });
  }

  const usuario = db.prepare('SELECT * FROM usuarios WHERE email = ? AND activo = 1').get(email);
  if (!usuario) return res.status(401).json({ error: 'Credenciales incorrectas' });

  const valido = bcrypt.compareSync(password, usuario.password);
  if (!valido) return res.status(401).json({ error: 'Credenciales incorrectas' });

  const token = jwt.sign({ id: usuario.id, email: usuario.email, rol: usuario.rol }, JWT_SECRET, { expiresIn: '7d' });
  const { password: _, ...usuarioSinPass } = usuario;

  res.json({ token, usuario: usuarioSinPass });
});

// obtener datos del usuario autenticado
router.get('/perfil', verificarToken, (req, res) => {
  const usuario = db.prepare(
    'SELECT id, nombre, email, rol, activo, discapacidad, created_at FROM usuarios WHERE id = ?'
  ).get(req.usuario.id);
  if (!usuario) return res.status(404).json({ error: 'Usuario no encontrado' });
  res.json(usuario);
});

router.put('/perfil', verificarToken, (req, res) => {
  const { nombre, discapacidad } = req.body;
  const usuario = db.prepare('SELECT * FROM usuarios WHERE id = ?').get(req.usuario.id);
  if (!usuario) return res.status(404).json({ error: 'Usuario no encontrado' });

  const nuevoNombre = nombre?.trim() || usuario.nombre;
  // discapacidad puede ser null/vacío para borrarla
  const nuevaDisc = discapacidad !== undefined ? (discapacidad.trim() || null) : usuario.discapacidad;

  db.prepare('UPDATE usuarios SET nombre=?, discapacidad=? WHERE id=?')
    .run(nuevoNombre, nuevaDisc, req.usuario.id);

  const actualizado = db.prepare(
    'SELECT id, nombre, email, rol, activo, discapacidad, created_at FROM usuarios WHERE id = ?'
  ).get(req.usuario.id);
  res.json(actualizado);
});

// el usuario puede borrar su propia cuenta desde ajustes
router.delete('/cuenta', verificarToken, (req, res) => {
  const usuario = db.prepare('SELECT id FROM usuarios WHERE id = ?').get(req.usuario.id);
  if (!usuario) return res.status(404).json({ error: 'Usuario no encontrado' });

  db.prepare('DELETE FROM usuarios WHERE id = ?').run(req.usuario.id);
  res.json({ mensaje: 'Cuenta eliminada correctamente' });
});

module.exports = router;
