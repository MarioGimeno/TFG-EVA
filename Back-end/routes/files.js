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
 * Lista los archivos del usuario y devuelve Signed URLs de lectura.
 */
router.get(
  '/',
  authenticate,
  async (req, res) => {
    try {
      const prefix = `${req.userId}/`;
      const [files] = await bucket.getFiles({ prefix });

      // Generamos una Signed URL para cada fichero
      const result = await Promise.all(files.map(async file => {
        const expires = Date.now() + 60 * 60 * 1000; // 1 hora
        const [signedUrl] = await file.getSignedUrl({
          action: 'read',
          expires
        });
        return {
          name: path.basename(file.name),
          url: signedUrl
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
 * Recibe form-data con campo 'file', lo sube a GCS
 * y devuelve una Signed URL de lectura.
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
      // Almacenamos en privado (resumable: false es opcional)
      await file.save(req.file.buffer, {
        metadata: { contentType: req.file.mimetype },
        resumable: false
      });

      // Ahora generamos la Signed URL
      const expires = Date.now() + 60 * 60 * 1000; // 1 hora
      const [signedUrl] = await file.getSignedUrl({
        action: 'read',
        expires
      });

      res.json({
        name: `${fileId}${ext}`,
        url: signedUrl 
      });
    } catch (err) {
      console.error('Error subiendo a GCS:', err);
      next(err);
    }
  }
);

module.exports = router;
