const fs = require('fs');
const path = require('path');
const { decryptFile } = require('./decryptionService');
const { uploadVideoAndLocation } = require('./gcsService');
const { cleanupFiles } = require('./cleanupService.js');

async function assembleAndUpload(fileId, totalChunks) {
  const baseDir    = path.join('uploads', fileId);
  const encPath    = path.join(baseDir, `${fileId}-encrypted.mp4`);
  const decPath    = path.join(baseDir, `${fileId}-decrypted.mp4`);
  const writeStream = fs.createWriteStream(encPath);
  
  // unir chunks
  for (let i = 0; i < totalChunks; i++) {
    await new Promise((res, rej) => {
      const rs = fs.createReadStream(path.join(baseDir, `chunk_${i}`));
      rs.pipe(writeStream, { end: false })
        .on('end', res)
        .on('error', rej);
    });
  }
  writeStream.end();
  
  // desencriptar y subir
  await decryptFile(encPath, decPath);
  const url = await uploadVideoAndLocation(decPath, fileId);
  await cleanupFiles(baseDir, encPath, decPath);
  return url;
}

module.exports = { assembleAndUpload };
