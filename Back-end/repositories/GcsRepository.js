// src/repositories/GcsRepository.js
const { Storage } = require('@google-cloud/storage');
const path        = require('path');
const fs          = require('fs');
const { GCS_BUCKET, GCS_KEYFILE } = require('../config/Pool');

class GcsRepository {
  constructor() {
    this.storage = new Storage({ keyFilename: GCS_KEYFILE });
    this.bucket  = this.storage.bucket(GCS_BUCKET);
  }

  /**
   * Sube un fichero local a GCS.
   * @param {string} localPath – Ruta en disco.
   * @param {string} destination – Ruta en el bucket (userId/fileId.ext).
   * @param {string} contentType – MIME type.
   * @param {boolean} resumable
   */
  async uploadFile(localPath, destination, contentType, resumable = true) {
    await this.bucket.upload(localPath, {
      destination,
      metadata:    { contentType },
      resumable
    });
    return destination;
  }

  /**
   * Lista todos los objetos de GCS con un prefijo dado.
   * @param {string} prefix – p.ej. "userId/".
   * @returns {File[]} array de objetos File
   */
  async listFiles(prefix) {
    const [files] = await this.bucket.getFiles({ prefix });
    return files;
  }

  /**
   * Genera una Signed URL de lectura.
   * @param {string} filename
   * @param {number} expiresMillis – timestamp de expiración
   */
  async getSignedUrl(filename, expiresMillis) {
    const file = this.bucket.file(filename);
    const [url] = await file.getSignedUrl({
      action:  'read',
      expires: expiresMillis
    });
    return url;
  }
}

module.exports = new GcsRepository();
