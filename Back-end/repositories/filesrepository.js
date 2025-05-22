// src/repositories/FilesRepository.js
const path         = require('path');
const { v4: uuidv4 } = require('uuid');
const { Storage }  = require('@google-cloud/storage');
const { GCS_BUCKET, GCS_KEYFILE } = require('../config/Pool');

const storage = new Storage({ keyFilename: GCS_KEYFILE });
const bucket  = storage.bucket(GCS_BUCKET);

class FilesRepository {
  /**
   * Lista todos los archivos de un usuario en GCS,
   * genera Signed URLs y devuelve metadata.
   */
  async listFiles(userId) {
    const prefix = `${userId}/`;
    const [files] = await bucket.getFiles({ prefix });
    return Promise.all(files.map(async file => {
      const expires = Date.now() + 60 * 60 * 1000; // 1h
      const [url]    = await file.getSignedUrl({ action: 'read', expires });
      const [meta]   = await file.getMetadata();
      return {
        name:    path.basename(file.name),
        url,
        created: meta.timeCreated,
        size:    parseInt(meta.size, 10)
      };
    }));
  }

  /**
   * Sube un archivo a GCS y devuelve name + Signed URL.
   */
  async uploadFile(userId, file) {
    const ext     = path.extname(file.originalname);
    const fileId  = uuidv4();
    const gcsPath = `${userId}/${fileId}${ext}`;
    const gcsFile = bucket.file(gcsPath);

    await gcsFile.save(file.buffer, {
      metadata:    { contentType: file.mimetype },
      resumable:   false
    });

    const expires = Date.now() + 60 * 60 * 1000;
    const [url]   = await gcsFile.getSignedUrl({ action: 'read', expires });

    return {
      name: `${fileId}${ext}`,
      url
    };
  }
}

module.exports = new FilesRepository();
