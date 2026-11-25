const express = require('express');
const db = require('../db/database');
const { verificarToken } = require('../middleware/auth');

const router = express.Router();

router.use(verificarToken);

router.post('/favoritos', (req, res) => {
  const { recordId, direccion } = req.body;
  if (!recordId) return res.status(400).json({ error: 'recordId es obligatorio' });

  const userEmail = req.usuario.email;

  // si ya existe lo devolvemos sin duplicar
  const existe = db.prepare(
    'SELECT id FROM parking_favoritos WHERE user_email = ? AND record_id = ?'
  ).get(userEmail, recordId);

  if (existe) return res.json({ id: existe.id });

  const result = db.prepare(
    'INSERT INTO parking_favoritos (user_email, record_id, direccion) VALUES (?, ?, ?)'
  ).run(userEmail, recordId, direccion || '');

  res.status(201).json({ id: result.lastInsertRowid });
});

router.delete('/favoritos/:recordId', (req, res) => {
  const userEmail = req.usuario.email;
  db.prepare(
    'DELETE FROM parking_favoritos WHERE user_email = ? AND record_id = ?'
  ).run(userEmail, req.params.recordId);
  res.json({ ok: true });
});

router.post('/reservas', (req, res) => {
  const { nombre, recogida, destino, fecha, hora, plazas, telefono, notas } = req.body;
  if (!nombre || !fecha) return res.status(400).json({ error: 'nombre y fecha son obligatorios' });

  const result = db.prepare(`
    INSERT INTO reservas (user_email, nombre, recogida, destino, fecha, hora, plazas, telefono, notas)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
  `).run(req.usuario.email, nombre, recogida || '', destino || '', fecha, hora || '',
         plazas || 1, telefono || '', notas || '');

  res.status(201).json({ id: result.lastInsertRowid });
});

router.delete('/reservas/:id', (req, res) => {
  db.prepare(
    'DELETE FROM reservas WHERE id = ? AND user_email = ?'
  ).run(req.params.id, req.usuario.email);
  res.json({ ok: true });
});

module.exports = router;
