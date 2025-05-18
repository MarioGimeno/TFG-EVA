// src/config/index.js

const fs    = require('fs');
const path  = require('path');
require('dotenv').config({ path: path.resolve(__dirname, '../../keys.env') });

const { Pool } = require('pg');

// Parámetro de carpeta temporal para multer
const TMPDIR = process.env.TMPDIR || '/mnt/uploads/tmp';

// ¡Crea TMPDIR si no existiera!
if (!fs.existsSync(TMPDIR)) {
  fs.mkdirSync(TMPDIR, { recursive: true });
}
const magicEnv = process.env.MAGIC;
if (!magicEnv) {
  throw new Error('❌ La variable de entorno MAGIC no está definida');
}
// Pool de Postgres
const pool = new Pool({
  host:     process.env.PGHOST,
  port:     process.env.PGPORT ? parseInt(process.env.PGPORT, 10) : 5432,
  user:     process.env.PGUSER,
  password: process.env.PGPASSWORD,
  database: process.env.PGDATABASE,
  ssl:      { rejectUnauthorized: false }
});

module.exports = {
  PORT: process.env.PORT || 3000,
  TMPDIR,
  GCS_BUCKET:  process.env.BUCKET_NAME,
  GCS_KEYFILE: process.env.GOOGLE_APPLICATION_CREDENTIALS,
  IV_SIZE:     parseInt(process.env.IV_SIZE,  10),
  TAG_SIZE:    parseInt(process.env.TAG_SIZE, 10),
  SECRET_KEY:  process.env.SECRET_KEY,
  MAGIC:       Buffer.from(process.env.MAGIC),
  JWT_SECRET:     process.env.JWT_SECRET,
  JWT_EXPIRES:    process.env.JWT_EXPIRES    || '30d',
  REFRESH_EXPIRES:process.env.REFRESH_EXPIRES || '60d',
  pool
};
