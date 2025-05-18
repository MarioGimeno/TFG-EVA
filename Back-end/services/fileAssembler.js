// services/fileAssembler.js

const fs   = require('fs');
const path = require('path');
const { TMPDIR } = require('../config');
const { decryptFile }            = require('./decryptionService');
const { uploadVideoAndLocation } = require('./gcsService');

/**
 * Ensambla los chunks de vídeo, desencripta el fichero resultante
 * y lo sube a GCS. Devuelve la URL pública.
 */
async function assembleAndUpload(userId, fileId, totalChunks) {
  // 1) Directorio de los chunks dentro de TMPDIR
  const chunksDir = path.join(TMPDIR, userId, fileId);
  
  // 2) Verificar que exista
  if (!fs.existsSync(chunksDir)) {
    throw new Error(`No existe el directorio de chunks: ${chunksDir}`);
  }

  // 3) Listar y ordenar los archivos de chunk
  const chunkFiles = fs.readdirSync(chunksDir)
    .filter(name => name.startsWith('chunk_'))
    .sort((a, b) => {
      const ia = parseInt(a.split('_')[1], 10);
      const ib = parseInt(b.split('_')[1], 10);
      return ia - ib;
    });

  if (chunkFiles.length !== totalChunks) {
    throw new Error(
      `Chunks encontrados (${chunkFiles.length}) ` +
      `no coinciden con total esperado (${totalChunks})`
    );
  }

  // 4) Ensamblar en un único archivo encriptado
  const encryptedPath = path.join(chunksDir, `${fileId}-encrypted.mp4`);
  const writeStream   = fs.createWriteStream(encryptedPath);

  for (const filename of chunkFiles) {
    const chunkPath = path.join(chunksDir, filename);
    const data      = fs.readFileSync(chunkPath);
    writeStream.write(data);
  }
  writeStream.end();
  await new Promise(resolve => writeStream.on('finish', resolve));

  // 5) Desencriptar el vídeo ensamblado
  const decryptedPath = path.join(chunksDir, `${fileId}.mp4`);
  await decryptFile(encryptedPath, decryptedPath);

  // 6) Subir el vídeo desencriptado a GCS
  const publicUrl = await uploadVideoAndLocation(userId, fileId, decryptedPath);

  return publicUrl;
}

module.exports = { assembleAndUpload };
