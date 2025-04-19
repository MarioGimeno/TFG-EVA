// src/services/gcsService.js

const { Storage } = require('@google-cloud/storage');
const fs = require('fs');
const path = require('path');
const { GCS_BUCKET, GCS_KEYFILE } = require('../config');

// Instancia del cliente de Google Cloud Storage
const storage = new Storage({ keyFilename: GCS_KEYFILE });
const bucket = storage.bucket(GCS_BUCKET);

/**
 * Sube el video desencriptado y, si existe, el archivo de ubicación desencriptado
 * a GCS bajo el prefijo `${userId}/${fileId}/...`.
 * @param {string} userId  Identificador del usuario
 * @param {string} fileId  Identificador único del archivo
 * @param {string} videoFilePath Ruta local del archivo de video desencriptado
 * @returns {Promise<string>} URL base donde están los archivos subidos
 */
// …
async function uploadVideoAndLocation(userId, fileId, videoFilePath) {
  // antes tenías:
  // const videoDestination   = `${userId}/${fileId}/video.mp4`;
  // const locationDestination= `${userId}/${fileId}/location.txt`;

  const videoDestination    = `${userId}/${fileId}.mp4`;
  const locationDestination = `${userId}/${fileId}.txt`;

  await bucket.upload(videoFilePath, {
    destination: videoDestination,
    metadata: { contentType: 'video/mp4' },
    resumable: true
  });

  const locationDecPath = path.join(
    path.dirname(videoFilePath),
    `${fileId}.txt`
  );
  if (fs.existsSync(locationDecPath)) {
    await bucket.upload(locationDecPath, {
      destination: locationDestination,
      metadata: { contentType: 'text/plain' },
      resumable: true
    });
  }

  return `https://storage.googleapis.com/${GCS_BUCKET}/${videoDestination}`;
}



/**
 * Lista todos los ficheros de un usuario en el bucket
 * y devuelve Signed URLs de lectura válidas por 1 hora.
 */
async function listUserFiles(userId) {
  const [files] = await bucket.getFiles({ prefix: `${userId}/` });

  // Para cada fichero, pide un signed URL que expira en 1 hora
  const oneHourFromNow = Date.now() + 60 * 60 * 1000;
  const signedUrls = await Promise.all(files.map(file =>
    file.getSignedUrl({
      action: 'read',
      expires: oneHourFromNow
    }).then(urls => urls[0])
  ));
  return signedUrls;
}
module.exports = {
  uploadVideoAndLocation,
  listUserFiles
};
