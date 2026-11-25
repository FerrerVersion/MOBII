const express = require('express');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

app.use('/api/auth', require('./routes/auth'));
app.use('/api/usuarios', require('./routes/usuarios'));
app.use('/api/sync', require('./routes/sync'));

// ruta raíz solo para comprobar que el servidor está levantado
app.get('/', (req, res) => {
  res.json({ mensaje: 'MOBII Backend API v1.0', estado: 'activo' });
});

app.use((req, res) => {
  res.status(404).json({ error: 'Ruta no encontrada' });
});

app.listen(PORT, () => {
  console.log(`Servidor MOBII corriendo en http://localhost:${PORT}`);
});
