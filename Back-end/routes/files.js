const express = require('express');
const router = express.Router();
const authenticate = require('../middleware/authMiddleware');
const { listUserFiles } = require('../services/gcsService');

/**
 * GET /api/files
 * Retorna JSON { files: [...] } con las URLs públicas
 * de los archivos del usuario en GCS.
 */
router.get('/', authenticate, async (req, res) => {
  try {
    const userId = String(req.userId);
    const files = await listUserFiles(userId);
    res.json({ files });
  } catch (err) {
    console.error('Error al listar archivos:', err);
    res.status(500).json({ error: 'Error interno listando tus archivos' });
  }
});

module.exports = router;
