const crypto = require('crypto');
const jwt = require('jsonwebtoken');

// La clave de firma de los tokens se lee de la variable de entorno JWT_SECRET.
// Si no existe (entorno de desarrollo), se genera una aleatoria al arrancar:
// los tokens dejan de valer al reiniciar el servidor, pero nunca hay una clave fija en el código.
const JWT_SECRET = process.env.JWT_SECRET || crypto.randomBytes(48).toString('hex');
if (!process.env.JWT_SECRET) {
  console.warn('[MOBII] JWT_SECRET no definida: usando una clave temporal solo para desarrollo.');
}

function verificarToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];
  if (!token) return res.status(401).json({ error: 'Token requerido' });

  try {
    req.usuario = jwt.verify(token, JWT_SECRET);
    next();
  } catch {
    res.status(403).json({ error: 'Token inválido o expirado' });
  }
}

function soloAdmin(req, res, next) {
  if (req.usuario?.rol !== 'admin') {
    return res.status(403).json({ error: 'Acceso solo para administradores' });
  }
  next();
}

module.exports = { verificarToken, soloAdmin, JWT_SECRET };
