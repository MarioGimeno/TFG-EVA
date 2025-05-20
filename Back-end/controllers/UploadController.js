// src/controllers/UploadController.js
const uploadService = require('../services/UploadService');

class UploadController {
  async uploadChunk(req, res, next) {
    try {
      const userId      = String(req.userId);
      const { fileId, chunkIndex, totalChunks } = req.body;
      if (!fileId || chunkIndex == null || !totalChunks) {
        const err = new Error('Faltan fileId, chunkIndex o totalChunks');
        err.status = 400;
        throw err;
      }
      if (!req.file) {
        const err = new Error('No se envió ningún chunk');
        err.status = 400;
        throw err;
      }

      const result = await uploadService.processChunk(
        userId,
        fileId,
        chunkIndex,
        totalChunks,
        req.file.path
      );

      res.json({ message: 'Chunk procesado', ...result });
    } catch (err) {
      console.error('Error en uploadChunk:', err);
      next(err);
    }
  }
}

module.exports = new UploadController();
