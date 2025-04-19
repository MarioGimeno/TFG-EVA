// services/fileAssembler.js

const fs = require('fs');
const path = require('path');
const { decryptFile } = require('./decryptionService');
const { uploadVideoAndLocation } = require('./gcsService');

/**
 * Ensambla los chunks desde uploads/userId/fileId,
 * desencripta los archivos y sube vídeo y ubicación a GCS.
 *
 * @param {string} userId      ID del usuario.
 * @param {string} fileId      ID único para el archivo.
 * @param {number} totalChunks Número de trozos de vídeo.
 * @returns {Promise<string>}  URL pública del vídeo en GCS.
 */
async function assembleAndUpload(userId, fileId, totalChunks) {
  // **1) Directorio donde Multer puso los chunks**
  const fileDir = path.join('uploads', userId, fileId);
  if (!fs.existsSync(fileDir)) {
    throw new Error(`No existe el directorio de chunks: ${fileDir}`);
  }

  // **2) Concatenar chunks en un único .mp4 encriptado**
  const encVideoPath = path.join(fileDir, `${fileId}-encrypted.mp4`);
  const writeStream = fs.createWriteStream(encVideoPath);
  for (let i = 0; i < totalChunks; i++) {
    const chunkPath = path.join(fileDir, `chunk_${i}`);
    if (!fs.existsSync(chunkPath)) {
      writeStream.close();
      throw new Error(`Falta chunk_${i} en ${fileDir}`);
    }
    writeStream.write(fs.readFileSync(chunkPath));
  }
  writeStream.end();

  // **3) Desencriptar vídeo**
  const decVideoPath = path.join(fileDir, `${fileId}-decrypted.mp4`);
  await decryptFile(encVideoPath, decVideoPath);

  // **4) Renombrar la ubicación si existe**
  const tmpLoc = path.join(fileDir, 'location-decrypted.txt');
  if (fs.existsSync(tmpLoc)) {
    fs.renameSync(tmpLoc, path.join(fileDir, `${fileId}.txt`));
  }

  // **5) Subir vídeo y ubicación a GCS**
  const publicUrl = await uploadVideoAndLocation(userId, fileId, decVideoPath);

  // **6) Limpiar carpeta de este fileId**
  fs.rmSync(fileDir, { recursive: true, force: true });

  return publicUrl;
}

module.exports = { assembleAndUpload };
