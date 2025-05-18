// routes/upload.js
const express = require('express');
const fs      = require('fs');
const path    = require('path');
const upload  = require('../middleware/multerConfig');
const auth    = require('../middleware/authMiddleware');
const { decryptFile }          = require('../services/decryptionService');
const { uploadVideoAndLocation } = require('../services/gcsService');
const { assembleAndUpload }    = require('../services/fileAssembler');
const { TMPDIR }               = require('../config');

const router = express.Router();

router.post(
  '/upload-chunk',
  auth,
  upload.single('chunkData'),
  async (req, res) => {
    const userId       = String(req.userId);
    const { fileId, chunkIndex, totalChunks } = req.body;

    // 1) Directorio destino bajo el mismo TMPDIR
    const userDir = path.join(TMPDIR, userId, fileId);
    console.log('→ userDir:', userDir);

    // 2) Asegurarnos de que exista antes de renombrar
    try {
      fs.mkdirSync(userDir, { recursive: true });
      console.log('✅ userDir creado o ya existente');
    } catch (err) {
      console.error('❌ Error al crear userDir:', err);
      return res.status(500).json({ error: 'No pude crear carpeta de usuario' });
    }

    // 3) Paths de origen y destino
    const src       = req.file.path;
    const filename  = chunkIndex === '-1' ? 'location.txt' : `chunk_${chunkIndex}`;
    const destPath  = path.join(userDir, filename);
    console.log(`📝 Moviendo chunk: src=${src}, existsSrc=${fs.existsSync(src)}, dest=${destPath}`);

    // 4) Mover el fichero
    try {
      fs.renameSync(src, destPath);
      console.log('✅ renameSync OK');
    } catch (err) {
      console.error('❌ Error en renameSync:', err);
      return res.status(500).json({ error: 'No pude mover el chunk' });
    }

    // 5) Lógica según tipo de chunk
    if (chunkIndex === '-1') {
      // Ubicación encriptada
      const encryptedLoc = destPath;
      const decryptedLoc = path.join(userDir, 'location-decrypted.txt');
      try {
        await decryptFile(encryptedLoc, decryptedLoc);
        console.log('📍 Ubicación desencriptada:', decryptedLoc);

        await uploadVideoAndLocation(userId, fileId, decryptedLoc);
        console.log('📍 Ubicación subida a GCS');
      } catch (e) {
        console.error('❌ Error procesando ubicación:', e);
      }
    } else {
      // Vídeo por chunks
      const received = fs
        .readdirSync(userDir)
        .filter(name => name.startsWith('chunk_')).length;

      console.log(`▶︎ Llevamos ${received}/${totalChunks} chunks`);

      if (received === Number(totalChunks)) {
        assembleAndUpload(userId, fileId, Number(totalChunks))
          .then(url => console.log('🎉 Vídeo procesado y subido:', url))
          .catch(err => console.error('❌ Error assembleAndUpload:', err));
      }
    }

    return res.json({ message: 'Chunk recibido' });
  }
);

module.exports = router;
