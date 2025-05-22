// src/services/FilesService.js
const repo = require('../repositories/filesrepository');

class FilesService {
  async listFiles(userId) {
    return repo.listFiles(userId);
  }

  async uploadFile(userId, file) {
    if (!file) {
      const err = new Error('No se envió ningún archivo.');
      err.status = 400;
      throw err;
    }
    return repo.uploadFile(userId, file);
  }
}

module.exports = new FilesService();
