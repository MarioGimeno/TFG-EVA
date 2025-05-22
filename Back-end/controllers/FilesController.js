// src/controllers/FilesController.js
const service = require('../services/filesservice');

class FilesController {
  async list(req, res, next) {
    try {
      const files = await service.listFiles(req.userId);
      res.json({ files });
    } catch (err) {
      next(err);
    }
  }

  async upload(req, res, next) {
    try {
      const result = await service.uploadFile(req.userId, req.file);
      res.json(result);
    } catch (err) {
      next(err);
    }
  }
}

module.exports = new FilesController();
