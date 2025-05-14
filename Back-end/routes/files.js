// routes/files.js
const express = require('express');
const multer  = require('multer');
const path    = require('path');
const { v4: uuidv4 } = require('uuid');
const { Storage } = require('@google-cloud/storage');
const { GCS_BUCKET, GCS_KEYFILE } = require('../config');
const authenticate = require('../middleware/authMiddleware');

const router = express.Router();
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 50 * 1024 * 1024 }
});

const storage = new Storage({ keyFilename: GCS_KEYFILE });
const bucket = storage.bucket(GCS_BUCKET);

/**
 * GET /api/files
 * Lista los archivos del usuario y devuelve Signed URLs de lectura
 * junto con 'created' (ISO date) y 'size' (bytes).
 */
router.get(
  '/',
  authenticate,
  async (req, res) => {
    try {
      const prefix = `${req.userId}/`;
      const [files] = await bucket.getFiles({ prefix });

      const result = await Promise.all(files.map(async file => {
        // 1) generar URL firmada
        const expires = Date.now() + 60 * 60 * 1000; // 1 hora
        const [signedUrl] = await file.getSignedUrl({
          action: 'read',
          expires
        });

        // 2) obtener metadata real desde GCS
        const [meta] = await file.getMetadata();
        // meta.timeCreated es string ISO; meta.size es string de bytes

        return {
          name:    path.basename(file.name),
          url:     signedUrl,
          created: meta.timeCreated,             // ej. "2025-05-06T17:12:56.054Z"
          size:    parseInt(meta.size, 10)       // convertir a number
        };
      }));

      res.json({ files: result });
    } catch (err) {
      console.error('Error al listar archivos:', err);
      res.status(500).json({ error: 'Error interno listando tus archivos' });
    }
  }
);

/**
 * POST /api/files
 * Sube un archivo y devuelve sólo name+url; los campos created/size
 * se poblarán cuando el cliente vuelva a llamar al GET /api/files.
 */
router.post(
  '/',
  authenticate,
  upload.single('file'),
  async (req, res, next) => {
    if (!req.file) {
      return res.status(400).json({ error: 'No enviaste ningún archivo.' });
    }
    try {
      const userId = String(req.userId);
      const ext = path.extname(req.file.originalname);
      const fileId = uuidv4();
      const gcsPath = `${userId}/${fileId}${ext}`;

      const file = bucket.file(gcsPath);
      await file.save(req.file.buffer, {
        metadata: { contentType: req.file.mimetype },
        resumable: false
      });

      const expires = Date.now() + 60 * 60 * 1000;
      const [signedUrl] = await file.getSignedUrl({
        action: 'read',
        expires
      });

      res.json({
        name: `${fileId}${ext}`,
        url: signedUrl
        // NOTA: aquí NO devolvemos created/size; eso lo hará tu GET
      });
    } catch (err) {
      console.error('Error subiendo a GCS:', err);
      next(err);
    }
  }
);

module.exports = router;
