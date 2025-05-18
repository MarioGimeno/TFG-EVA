// src/config/index.js
require('dotenv').config({ path: './keys.env' });

const { Pool } = require('pg');

const pool = new Pool({
    host:     process.env.PGHOST,
    port:     process.env.PGPORT ? parseInt(process.env.PGPORT, 10) : 5432,
    user:     process.env.PGUSER,
    password: process.env.PGPASSWORD,
    database: process.env.PGDATABASE,
    ssl:  { rejectUnauthorized: false }
});
  // ¡Crea TMPDIR si no existiera!
if (!fs.existsSync(TMPDIR)) {
  fs.mkdirSync(TMPDIR, { recursive: true });
}
module.exports = {
  // servidor
  PORT: process.env.PORT || 3000,

  // multer / sistema de ficheros
  TMPDIR: process.env.TMPDIR || '/mnt/uploads/tmp',

  // GCS
  GCS_BUCKET:  process.env.BUCKET_NAME,
  GCS_KEYFILE: process.env.GOOGLE_APPLICATION_CREDENTIALS,

  // encriptación
  IV_SIZE:    parseInt(process.env.IV_SIZE,  10),
  TAG_SIZE:   parseInt(process.env.TAG_SIZE, 10),
  SECRET_KEY: process.env.SECRET_KEY,
  MAGIC:      Buffer.from(process.env.MAGIC),

  // autenticación
  JWT_SECRET:     process.env.JWT_SECRET,
  JWT_EXPIRES:    process.env.JWT_EXPIRES    || '30d',
  REFRESH_EXPIRES: process.env.REFRESH_EXPIRES || '60d',

  // base de datos
  pool
};
