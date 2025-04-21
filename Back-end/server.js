// server.js (extracto)
const express = require('express');
const cors    = require('cors');
const { pool, PORT } = require('./config');
const authRoutes   = require('./routes/auth');
const uploadRoutes = require('./routes/upload');
const videosRoutes = require('./routes/videos');
const fileRoutes   = require('./routes/files');

const app = express();
app.use(cors());
app.use(express.json());
app.use((req, res, next) => {
  console.log(new Date().toISOString(), req.method, req.originalUrl);
  next();
});

pool.connect()
  .then(() => {
    console.log('✅ Conectado a PostgreSQL');

    // Rutas públicas y protegidas
    app.use('/auth', authRoutes);
    app.use('/upload', uploadRoutes);
    app.use('/videos', videosRoutes);

    // ¡Importante! monta *antes* del 404 genérico:
    app.use('/api/files', fileRoutes);

    // Catch‑all de 404
    app.use((req, res) => res.status(404).json({ error: 'Not Found' }));

    app.listen(PORT, () => {
      console.log(`🚀 Servidor escuchando en puerto ${PORT}`);
    });
  })
  .catch(err => {
    console.error('❌ No se pudo conectar a PostgreSQL:', err);
    process.exit(1);
  });
