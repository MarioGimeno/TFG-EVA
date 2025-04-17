// server.js
const express = require('express');
const cors    = require('cors');
const { pool, PORT } = require('./config');
const authRoutes  = require('./routes/auth');
const uploadRoutes = require('./routes/upload');
const videosRoutes = require('./routes/videos');

const app = express();
app.use(cors());
app.use(express.json());

// Opcional: verificar conexión a PG antes de escuchar
pool.connect()
  .then(() => {
    console.log('✅ Conectado a PostgreSQL');
    // Monta tus rutas protegidas y públicas
    app.use('/auth', authRoutes);
    app.use('/upload', uploadRoutes);
    app.use('/videos', videosRoutes);

    app.listen(PORT, () => {
      console.log(`🚀 Servidor escuchando en puerto ${PORT}`);
    });
  })
  .catch(err => {
    console.error('❌ No se pudo conectar a PostgreSQL:', err);
    process.exit(1);
  });
