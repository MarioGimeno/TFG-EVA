const express = require('express');
const fs      = require('fs');
const path    = require('path');
const upload  = require('../middleware/multerConfig');
const { assembleAndUpload } = require('../services/fileAssembler');
const auth    = require('../middleware/authMiddleware');
const router  = express.Router();

router.post('/upload-chunk',
  auth,
  upload.single('chunkData'),
  async (req, res) => {
    const userId     = req.userId;
    const { fileId, chunkIndex, totalChunks } = req.body;
    const userDir    = path.join('uploads', userId, fileId);

    if (!fs.existsSync(userDir)) fs.mkdirSync(userDir, { recursive:true });

    // renombra según si es chunk o location
    const filename = chunkIndex === '-1'
      ? 'location.txt'
      : `chunk_${chunkIndex}`;
    fs.renameSync(req.file.path, path.join(userDir, filename));

    const received = fs.readdirSync(userDir).filter(n=>n.startsWith('chunk_')).length;
    if (received === +totalChunks) {
      // pasamos userId a la lógica para que suba a GCS en carpeta userId/fileId/…
      assembleAndUpload(userId, fileId, +totalChunks)
        .then(url=> console.log('Upload éxito:', url))
        .catch(err=> console.error(err));
    }

    res.json({ message:'Chunk recibido' });
  }
);

module.exports = router;
