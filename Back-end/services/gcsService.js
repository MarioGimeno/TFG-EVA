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
async function uploadVideoAndLocation(userId, fileId, videoFilePath) {
  // Definimos destinos dentro del bucket con template literals
  const videoDestination = `${userId}/${fileId}/video.mp4`;
  const locationDestination = `${userId}/${fileId}/location.txt`;
  
  // Subida del vídeo
  await bucket.upload(videoFilePath, {
    destination: videoDestination,
    metadata: { contentType: 'video/mp4' },
    resumable: true
  });

  // Comprobamos si existe un archivo de ubicación desencriptado
  const locationDecPath = path.join(
    path.dirname(videoFilePath),
    `${fileId}-decrypted-location.txt`
  );
  if (fs.existsSync(locationDecPath)) {
    // Subida de la ubicación
    await bucket.upload(locationDecPath, {
      destination: locationDestination,
      metadata: { contentType: 'text/plain' },
      resumable: true
    });
  }

  // Construimos la URL pública base
  const baseUrl = `https://storage.googleapis.com/${GCS_BUCKET}/${userId}/${fileId}/`;
  return baseUrl;
}

/**
 * Lista todos los ficheros de un usuario en el bucket y devuelve sus URLs públicas
 * @param {string} userId Identificador del usuario
 * @returns {Promise<string[]>} Lista de URLs de los archivos
 */
async function listUserFiles(userId) {
  const [files] = await bucket.getFiles({ prefix: `${userId}/` });
  return files.map(file => `https://storage.googleapis.com/${GCS_BUCKET}/${file.name}`);
}

module.exports = {
  uploadVideoAndLocation,
  listUserFiles
};
