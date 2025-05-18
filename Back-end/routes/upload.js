// services/fileAssembler.js

const fs   = require('fs');
const path = require('path');
const { TMPDIR } = require('../config');
const { decryptFile }          = require('./decryptionService');
const { uploadVideoAndLocation } = require('./gcsService');

/**
 * Ensambla los chunks de vídeo, desencripta el fichero resultante
 * y lo sube a GCS. Devuelve la URL final.
 */
async function assembleAndUpload(userId, fileId, totalChunks) {
  // 1) Ruta donde Multer dejó los chunks
  const chunksDir = path.join(TMPDIR, userId, fileId);

  // 2) Verificar existencia
  if (!fs.existsSync(chunksDir)) {
    throw new Error(`No existe el directorio de chunks: ${chunksDir}`);
  }

  // 3) Listar y ordenar los archivos chunk_X
  const chunkFiles = fs.readdirSync(chunksDir)
    .filter(name => name.startsWith('chunk_'))
    .sort((a, b) => {
      const idxA = parseInt(a.split('_')[1], 10);
      const idxB = parseInt(b.split('_')[1], 10);
      return idxA - idxB;
    });

  if (chunkFiles.length !== totalChunks) {
    throw new Error(
      `Número de chunks encontrado (${chunkFiles.length}) ` +
      `no coincide con total esperado (${totalChunks})`
    );
  }

  // 4) Fichero ensamblado (aún encriptado)
  const encryptedPath = path.join(chunksDir, `${fileId}-encrypted.mp4`);
  const writeStream   = fs.createWriteStream(encryptedPath);

  for (const filename of chunkFiles) {
    const chunkPath = path.join(chunksDir, filename);
    const data      = fs.readFileSync(chunkPath);
    writeStream.write(data);
  }
  writeStream.end();
  await new Promise(resolve => writeStream.on('finish', resolve));

  // 5) Desencriptar el vídeo completo
  const decryptedPath = path.join(chunksDir, `${fileId}.mp4`);
  await decryptFile(encryptedPath, decryptedPath);

  // 6) Subir vídeo desencriptado a GCS
  const publicUrl = await uploadVideoAndLocation(userId, fileId, decryptedPath);

  return publicUrl;
}

module.exports = {
  assembleAndUpload
};
