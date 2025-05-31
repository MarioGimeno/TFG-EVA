// src/services/UploadService.js
const uploadRepo             = require('../repositories/uploadrepository');
const { decryptFile }        = require('../utils/DecryptionUtils');
const { uploadVideoAndLocation } = require('../services/gcsservice');
const { assembleAndUpload }  = require('../utils/FileAssemblerUtils');

class UploadService {
  /**
   * Procesa un chunk (o la ubicación) y, si es el último,
   * ensambla y sube el vídeo completo.
   */
  async processChunk(userId, fileId, chunkIndex, totalChunks, tmpFilePath) {
    // 1) Guardar chunk o ubicación en disco
    const savedPath = uploadRepo.saveChunk(userId, fileId, chunkIndex, tmpFilePath);

    // 2) Si es la ubicación (-1) → desencriptar y subir
    if (chunkIndex === '-1') {
      const decryptedLoc = savedPath.replace('.txt', '-decrypted.txt');
      await decryptFile(savedPath, decryptedLoc);
      await uploadVideoAndLocation(userId, fileId, decryptedLoc);
      return { type: 'location' };
    }

    // 3) Si no, es un chunk de vídeo → comprobar si recibimos todos
    const received = await uploadRepo.countVideoChunks(userId, fileId);
    if (received === Number(totalChunks)) {
      const url = await assembleAndUpload(userId, fileId, Number(totalChunks));
      await uploadRepo.insertSubida(userId);
      return { type: 'video', url };
    }

    // 4) Si faltan más chunks, sólo confirmamos recepción
    return { type: 'chunk', received };
  }
}

module.exports = new UploadService();
