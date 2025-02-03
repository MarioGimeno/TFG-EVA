const { parentPort, workerData } = require('worker_threads');
const fs = require('fs');
const crypto = require('crypto');
const { pipeline } = require('stream');
const { promisify } = require('util');
const pipelineAsync = promisify(pipeline);

const IV_SIZE = 12;    // 12 bytes para GCM
const TAG_SIZE = 16;   // 16 bytes para el tag
const SECRET_KEY = '1234567890123456'; // Debe coincidir con el front
const MAGIC = Buffer.from("CHNK");

/**
 * Función para desencriptar un archivo en modo streaming.
 * Se asume que el archivo tiene el formato: [ IV (12 bytes) | Ciphertext ... | Auth Tag (16 bytes) ]
 */
async function decryptFileStreaming(inputFilePath, outputFilePath) {
  const stats = fs.statSync(inputFilePath);
  const fileSize = stats.size;
  if (fileSize < IV_SIZE + TAG_SIZE) {
    throw new Error("Archivo demasiado corto para desencriptación streaming");
  }
  // Leer IV y Auth Tag
  const fd = fs.openSync(inputFilePath, 'r');
  const iv = Buffer.alloc(IV_SIZE);
  fs.readSync(fd, iv, 0, IV_SIZE, 0);
  const tag = Buffer.alloc(TAG_SIZE);
  fs.readSync(fd, tag, 0, TAG_SIZE, fileSize - TAG_SIZE);
  fs.closeSync(fd);

  const decipher = crypto.createDecipheriv('aes-128-gcm', Buffer.from(SECRET_KEY, 'utf8'), iv);
  decipher.setAuthTag(tag);

  // Crear stream de lectura: omite los primeros IV_SIZE bytes y no incluye el Auth Tag final
  const readStream = fs.createReadStream(inputFilePath, { start: IV_SIZE, end: fileSize - TAG_SIZE - 1 });
  const writeStream = fs.createWriteStream(outputFilePath);

  await pipelineAsync(readStream, decipher, writeStream);
}

/**
 * Función para desencriptar un archivo en modo chunked.
 * Se espera el formato: [ MAGIC (4 bytes) | chunkSize (int32) | ... ]
 * Se procesa en memoria (nota: para archivos muy grandes, idealmente se debería usar un enfoque basado en streams).
 */
function decryptFileChunked(inputFilePath, outputFilePath) {
  const data = fs.readFileSync(inputFilePath);
  let offset = 4; // Saltar MAGIC
  const chunkSize = data.readInt32BE(offset);
  offset += 4;
  const decryptedChunks = [];
  while (offset < data.length) {
    const ivLength = data.readInt32BE(offset);
    offset += 4;
    const iv = data.slice(offset, offset + ivLength);
    offset += ivLength;
    const encChunkLength = data.readInt32BE(offset);
    offset += 4;
    const encryptedChunk = data.slice(offset, offset + encChunkLength);
    offset += encChunkLength;
    if (encryptedChunk.length < TAG_SIZE) {
      throw new Error("Encrypted chunk too short to contain auth tag");
    }
    const authTag = encryptedChunk.slice(encryptedChunk.length - TAG_SIZE);
    const ciphertext = encryptedChunk.slice(0, encryptedChunk.length - TAG_SIZE);
    const decipher = crypto.createDecipheriv('aes-128-gcm', Buffer.from(SECRET_KEY, 'utf8'), iv);
    decipher.setAuthTag(authTag);
    const decryptedChunk = Buffer.concat([decipher.update(ciphertext), decipher.final()]);
    decryptedChunks.push(decryptedChunk);
  }
  const decryptedBuffer = Buffer.concat(decryptedChunks);
  fs.writeFileSync(outputFilePath, decryptedBuffer);
}

(async () => {
  try {
    const { inputFilePath, outputFilePath } = workerData;
    // Verificar el header para determinar el modo de encriptación
    const fd = fs.openSync(inputFilePath, 'r');
    const header = Buffer.alloc(4);
    fs.readSync(fd, header, 0, 4, 0);
    fs.closeSync(fd);

    if (header.equals(MAGIC)) {
      console.log("Worker: Formato chunked detectado.");
      decryptFileChunked(inputFilePath, outputFilePath);
    } else {
      console.log("Worker: Formato streaming detectado.");
      await decryptFileStreaming(inputFilePath, outputFilePath);
    }
    parentPort.postMessage({ success: true, outputFilePath });
  } catch (err) {
    parentPort.postMessage({ success: false, error: err.message });
  }
})();
