const fs = require('fs');
const path = require('path');

async function cleanupFiles(chunksDir, encPath, decPath) {
  if (fs.existsSync(chunksDir)) {
    fs.readdirSync(chunksDir).forEach(f => fs.unlinkSync(path.join(chunksDir, f)));
    fs.rmdirSync(chunksDir);
  }
  if (fs.existsSync(encPath)) fs.unlinkSync(encPath);
  if (fs.existsSync(decPath)) fs.unlinkSync(decPath);
}

module.exports = { cleanupFiles };
