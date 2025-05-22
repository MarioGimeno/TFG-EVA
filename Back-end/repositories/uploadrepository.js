// src/repositories/UploadRepository.js
const fs   = require('fs');
const path = require('path');
const { TMPDIR } = require('../config/Pool');

class UploadRepository {
  getChunkDir(userId, fileId) {
    const dir = path.join(TMPDIR, String(userId), fileId);
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
    }
    return dir;
  }

  saveChunk(userId, fileId, chunkIndex, tmpFilePath) {
    const dir = this.getChunkDir(userId, fileId);
    const filename = chunkIndex === '-1'
      ? 'location.txt'
      : `chunk_${chunkIndex}`;
    const dest = path.join(dir, filename);
    fs.renameSync(tmpFilePath, dest);
    return dest;
  }

  async countVideoChunks(userId, fileId) {
    const dir = this.getChunkDir(userId, fileId);
    return fs.readdirSync(dir)
      .filter(name => name.startsWith('chunk_'))
      .length;
  }
}

module.exports = new UploadRepository();
