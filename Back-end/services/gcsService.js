// src/services/GcsService.js
const path          = require('path');
const fs            = require('fs');
const gcsRepo       = require('../repositories/gcsRepository');

class GcsService {
  /**
   * Sube el vídeo (.mp4) y, si existe, el fichero de ubicación desencriptado.
   * Devuelve la URL firmada del vídeo.
   */
  async uploadVideoAndLocation(userId, fileId, videoFilePath) {
    const videoDest = `${userId}/${fileId}.mp4`;
    await gcsRepo.uploadFile(videoFilePath, videoDest, 'video/mp4', true);

    const folder       = path.dirname(videoFilePath);
    const locPath      = path.join(folder, 'location-decrypted.txt');
    if (fs.existsSync(locPath)) {
      const locDest = `${userId}/${fileId}.txt`;
      await gcsRepo.uploadFile(locPath, locDest, 'text/plain', true);
    }

    // URL para la descarga del vídeo
    return await gcsRepo.getSignedUrl(
      videoDest,
      Date.now() + 60 * 60 * 1000
    );
  }

  /**
   * Lista todos los archivos de un usuario y devuelve name+url.
   */
  async listUserFiles(userId) {
    const prefix = `${userId}/`;
    const files  = await gcsRepo.listFiles(prefix);

    return Promise.all(files.map(async file => ({
      name: path.basename(file.name),
      url:  await gcsRepo.getSignedUrl(
              file.name,
              Date.now() + 60 * 60 * 1000
            )
    })));
  }
}

module.exports = new GcsService();
