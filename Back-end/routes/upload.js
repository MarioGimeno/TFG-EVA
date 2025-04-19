// routes/upload.js
const express = require('express');
const fs      = require('fs');
const path    = require('path');
const upload  = require('../middleware/multerConfig');
const { assembleAndUpload } = require('../services/fileAssembler');
const auth    = require('../middleware/authMiddleware');
const router  = express.Router();
const { decryptFile }         = require('../services/decryptionService');
const { uploadVideoAndLocation } = require('../services/gcsService');
router.post(
    '/upload-chunk',
    auth,
    upload.single('chunkData'),
    async (req, res) => {
      const userId  = String(req.userId);
      const { fileId, chunkIndex, totalChunks } = req.body;
      const userDir    = path.join('uploads', userId, fileId);      if (!fs.existsSync(userDir)) fs.mkdirSync(userDir, { recursive: true });
  
      // Nombre de fichero según tipo
      const filename = chunkIndex === '-1'
        ? 'location.txt'
        : `chunk_${chunkIndex}`;
      const destPath = path.join(userDir, filename);
      fs.renameSync(req.file.path, destPath);
  
      console.log(`▶︎ Recibido ${filename} -> ${destPath}`);
  
      if (chunkIndex === '-1') {
        // *** SOLO LOCATION ***
        // Desencripta y sube la ubicación sin tocar el vídeo
        const encryptedLoc = destPath;
        const decryptedLoc = path.join(userDir, 'location-decrypted.txt');
        try {
          await decryptFile(encryptedLoc, decryptedLoc);
          // Aquí tu función que sube a GCS: uploadLocationToGCS(userId, fileId, decryptedLoc)
          console.log('📍 Ubicación desencriptada y subida:', decryptedLoc);
                  // Si quieres subirla junto al vídeo:
        await uploadVideoAndLocation(userId, fileId, decryptedLoc);
        console.log('📍 Ubicación subida a GCS.');
        } catch (e) {
          console.error('❌ Error procesando ubicación:', e);
        }
      } else {
        // *** SOLO CHUNKS DE VÍDEO ***
        const received = fs
          .readdirSync(userDir)
          .filter(n => n.startsWith('chunk_')).length;
  
        console.log(`▶︎ Llevamos ${received}/${totalChunks} chunks de vídeo`);
  
        if (received === Number(totalChunks)) {
          // Cuando ya están todos los chunks, ensamblar + desencriptar + subir
          assembleAndUpload(userId, fileId, Number(totalChunks))
            .then(url => console.log('🎉 Vídeo procesado y subido:', url))
            .catch(err => console.error('❌ Error assembleAndUpload:', err));
        }
      }
  
      res.json({ message: 'Chunk recibido' });
    }
  );
  

module.exports = router;
