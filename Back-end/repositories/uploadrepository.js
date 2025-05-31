// src/repositories/UploadRepository.js
const fs   = require('fs');
const path = require('path');
const { TMPDIR } = require('../config/Pool');
const { pool } = require('../config/Pool');

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
  async  insertSubida(userId) {
  const query = `
    INSERT INTO subida (id_usuario)
    VALUES ($1)
  `;
  await pool.query(query, [userId]);
}
}

module.exports = new UploadRepository();
