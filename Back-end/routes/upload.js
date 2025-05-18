// routes/upload.js
const express = require('express');
const fs      = require('fs');
const path    = require('path');
const upload  = require('../middleware/multerConfig');
const auth    = require('../middleware/authMiddleware');

// IMPORTA DESDE ../services (no ./)
const { decryptFile }            = require('../services/decryptionService');
const { uploadVideoAndLocation } = require('../services/gcsService');
const { assembleAndUpload }      = require('../services/fileAssembler');

const { TMPDIR } = require('../config');

const router = express.Router();

router.post(
  '/upload-chunk',
  auth,
  upload.single('chunkData'),
  async (req, res) => {
    const userId       = String(req.userId);
    const { fileId, chunkIndex, totalChunks } = req.body;

    const userDir = path.join(TMPDIR, userId, fileId);
    fs.mkdirSync(userDir, { recursive: true });

    const src      = req.file.path;
    const filename = chunkIndex === '-1' ? 'location.txt' : `chunk_${chunkIndex}`;
    const destPath = path.join(userDir, filename);

    try {
      fs.renameSync(src, destPath);
    } catch (err) {
      console.error('Error moviendo chunk:', err);
      return res.status(500).json({ error: 'No pude mover el chunk' });
    }

    if (chunkIndex === '-1') {
      // Ubicación
      const encryptedLoc = destPath;
      const decryptedLoc = path.join(userDir, 'location-decrypted.txt');
      try {
        await decryptFile(encryptedLoc, decryptedLoc);
        await uploadVideoAndLocation(userId, fileId, decryptedLoc);
      } catch (e) {
        console.error('Error procesando ubicación:', e);
      }
    } else {
      // Vídeo
      const received = fs
        .readdirSync(userDir)
        .filter(n => n.startsWith('chunk_')).length;

      if (received === Number(totalChunks)) {
        try {
          const url = await assembleAndUpload(userId, fileId, Number(totalChunks));
          console.log('Vídeo subido:', url);
        } catch (err) {
          console.error('Error assembleAndUpload:', err);
        }
      }
    }

    res.json({ message: 'Chunk recibido' });
  }
);

module.exports = router;
