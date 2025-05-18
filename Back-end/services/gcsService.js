// src/services/gcsService.js

const { Storage } = require('@google-cloud/storage');
const fs          = require('fs');
const path        = require('path');
const { GCS_BUCKET, GCS_KEYFILE } = require('../config');

const storage = new Storage({ keyFilename: GCS_KEYFILE });
const bucket  = storage.bucket(GCS_BUCKET);

async function uploadVideoAndLocation(userId, fileId, videoFilePath) {
  // 1) Subir el vídeo como antes
  const videoDestination = `${userId}/${fileId}.mp4`;
  await bucket.upload(videoFilePath, {
    destination: videoDestination,
    metadata:    { contentType: 'video/mp4' },
    resumable:   true
  });

  // 2) Ahora buscamos el archivo location-decrypted.txt en la misma carpeta
  const folder = path.dirname(videoFilePath);
  const txtLocalPath = path.join(folder, 'location-decrypted.txt');

  if (fs.existsSync(txtLocalPath)) {
    const txtDestination = `${userId}/${fileId}.txt`;
    await bucket.upload(txtLocalPath, {
      destination: txtDestination,
      metadata:    { contentType: 'text/plain' },
      resumable:   true
    });
    console.log('📍 location-decrypted.txt subido como', txtDestination);
  } else {
    console.log('⚠️ No se encontró location-decrypted.txt en', folder);
  }

  // 3) Devolvemos la URL del vídeo principal (puedes cambiarlo para devolver un objeto con ambas URLs)
  return `https://storage.googleapis.com/${GCS_BUCKET}/${videoDestination}`;
}

async function listUserFiles(userId) {
  const [files] = await bucket.getFiles({ prefix: `${userId}/` });
  return Promise.all(files.map(async file => {
    const [signedUrl] = await file.getSignedUrl({
      action: 'read',
      expires: Date.now() + 60 * 60 * 1000
    });
    return { name: file.name, url: signedUrl };
  }));
}

module.exports = {
  uploadVideoAndLocation,
  listUserFiles
};
