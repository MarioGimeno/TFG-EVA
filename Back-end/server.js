// server.js (extracto)
const express = require('express');
const cors    = require('cors');
const admin  = require('firebase-admin');
const { pool, PORT } = require('./config/Pool');
const authRoutes   = require('./routes/Auth');
const uploadRoutes = require('./routes/Upload');
const videosRoutes = require('./routes/Videos');
const fileRoutes   = require('./routes/Files');
const contactsRouter = require('./routes/Contacts');
const tokensRouter = require('./routes/Tokens');
const notificationsRouter = require('./routes/Notifications');
const entidadesRouter = require('./routes/Entidades');
const serviciosRouter   = require('./routes/Recursos');
const categoriasRouter = require('./routes/Categorias');

const app = express();
app.use(cors());
app.use(express.json());
app.use((req, res, next) => {
  console.log(new Date().toISOString(), req.method, req.originalUrl);
  next();
});
const serviceAccount = require('./firebase/serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  // opcionalmente, si usas RTDB:
  // databaseURL: "https://<TU-PROYECTO>.firebaseio.com"
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
    app.use('/api/contacts', contactsRouter);

    app.use('/api/notifications', notificationsRouter);
    app.use('/api/servicios', serviciosRouter);
    app.use('/api/categorias', categoriasRouter);
    app.use('/api/entidades', entidadesRouter);
    app.use('/api/tokens', tokensRouter);    // Catch‑all de 404
    app.use((req, res) => res.status(404).json({ error: 'Not Found' }));

    app.listen(PORT, () => {
      console.log(`🚀 Servidor escuchando en puerto ${PORT}`);
    });
  })
  .catch(err => {
    console.error('❌ No se pudo conectar a PostgreSQL:', err);
    process.exit(1);
  });
