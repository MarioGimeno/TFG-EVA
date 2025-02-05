const { parentPort, workerData } = require('worker_threads');
const fs = require('fs');
const crypto = require('crypto');
const { pipeline } = require('stream');
const { promisify } = require('util');
const pipelineAsync = promisify(pipeline);

const IV_SIZE = 12;             // 12 bytes para GCM
const TAG_SIZE = 16;            // 16 bytes para el Auth Tag
const SECRET_KEY = '1234567890123456'; // Debe coincidir con el front
const MAGIC = "CHNK";           // Magic header para archivos chunked

/**
 * Función principal que detecta el formato del archivo y llama a la función de desencriptación adecuada.
 * Si el archivo inicia con el magic header "CHNK", se asume formato chunked; de lo contrario, se asume formato streaming.
 */
async function decryptFile(inputFilePath, outputFilePath) {
  // Obtener tamaño total del archivo
  const stats = fs.statSync(inputFilePath);
  const fileSize = stats.size;
  if (fileSize < IV_SIZE + TAG_SIZE) {
    throw new Error("Archivo demasiado corto para desencriptación");
  }

  // Leer los primeros 4 bytes para detectar el formato
  const fd = fs.openSync(inputFilePath, 'r');
  const header = Buffer.alloc(4);
  fs.readSync(fd, header, 0, 4, 0);
  fs.closeSync(fd);

  if (header.toString('utf8') === MAGIC) {
    console.log("Formato chunked detectado.");
    await decryptChunkedFile(inputFilePath, outputFilePath);
  } else {
    console.log("Formato streaming detectado.");
    await decryptFileStreaming(inputFilePath, outputFilePath);
  }
}

/**
 * Desencripta un archivo en modo streaming.
 * Se espera el formato: [ IV (12 bytes) | Ciphertext ... | Auth Tag (16 bytes) ]
 */
async function decryptFileStreaming(inputFilePath, outputFilePath) {
  const stats = fs.statSync(inputFilePath);
  const fileSize = stats.size;
  if (fileSize < IV_SIZE + TAG_SIZE) {
    throw new Error("Archivo demasiado corto para desencriptación streaming");
  }

  // Leer el IV (primeros 12 bytes) y el Auth Tag (últimos 16 bytes) sin cargar todo el archivo en memoria
  const fd = fs.openSync(inputFilePath, 'r');
  const iv = Buffer.alloc(IV_SIZE);
  fs.readSync(fd, iv, 0, IV_SIZE, 0);
  const authTag = Buffer.alloc(TAG_SIZE);
  fs.readSync(fd, authTag, 0, TAG_SIZE, fileSize - TAG_SIZE);
  fs.closeSync(fd);

  // Crear el objeto decipher para AES-128-GCM
  const decipher = crypto.createDecipheriv('aes-128-gcm', Buffer.from(SECRET_KEY, 'utf8'), iv);
  decipher.setAuthTag(authTag);

  // Crear un stream de lectura que omita el IV y el Auth Tag
  const readStream = fs.createReadStream(inputFilePath, {
    start: IV_SIZE,
    end: fileSize - TAG_SIZE - 1  // 'end' es inclusivo
  });
  const writeStream = fs.createWriteStream(outputFilePath);

  // Conectar los streams con pipeline
  await pipelineAsync(readStream, decipher, writeStream);
}

/**
 * Desencripta un archivo en modo chunked.
 * Se asume el formato:
 *   [ MAGIC (4 bytes) | chunkSize (4 bytes) | (para cada chunk: [ ivLength (4 bytes) | IV | encryptedChunkLength (4 bytes) | encryptedChunk (ciphertext + Auth Tag) ] )
 *
 * Se separa el Auth Tag de cada chunk (se asume que es de 16 bytes y se encuentra al final del chunk encriptado).
 */
async function decryptChunkedFile(inputFilePath, outputFilePath) {
  const fileBuffer = fs.readFileSync(inputFilePath);
  let offset = 0;

  // Leer el magic header (4 bytes)
  const magicHeader = fileBuffer.slice(offset, offset + 4).toString('utf8');
  if (magicHeader !== MAGIC) {
    throw new Error("Archivo no tiene el magic header esperado.");
  }
  offset += 4;

  // Leer el tamaño de chunk (4 bytes, entero Big Endian)
  const chunkSize = fileBuffer.readInt32BE(offset);
  offset += 4;
  console.log("Tamaño de chunk declarado:", chunkSize);

  const outputBuffers = [];

  // Procesar cada chunk
  while (offset < fileBuffer.length) {
    // Leer la longitud del IV (4 bytes)
    const ivLength = fileBuffer.readInt32BE(offset);
    offset += 4;
    // Leer el IV
    const iv = fileBuffer.slice(offset, offset + ivLength);
    offset += ivLength;

    // Leer la longitud del chunk encriptado (4 bytes)
    const encryptedChunkLength = fileBuffer.readInt32BE(offset);
    offset += 4;
    // Leer el chunk encriptado (contiene ciphertext + Auth Tag)
    const encryptedChunk = fileBuffer.slice(offset, offset + encryptedChunkLength);
    offset += encryptedChunkLength;

    if (encryptedChunk.length < TAG_SIZE) {
      throw new Error("Chunk encriptado demasiado corto para tener Auth Tag");
    }
    // Separar el Auth Tag del ciphertext
    const ciphertext = encryptedChunk.slice(0, encryptedChunk.length - TAG_SIZE);
    const authTag = encryptedChunk.slice(encryptedChunk.length - TAG_SIZE);

    // Crear el objeto decipher para este chunk
    const decipher = crypto.createDecipheriv('aes-128-gcm', Buffer.from(SECRET_KEY, 'utf8'), iv);
    decipher.setAuthTag(authTag);
    const decryptedChunk = Buffer.concat([decipher.update(ciphertext), decipher.final()]);
    outputBuffers.push(decryptedChunk);
  }

  // Escribir el resultado final
  fs.writeFileSync(outputFilePath, Buffer.concat(outputBuffers));
}

(async () => {
  try {
    const { inputFilePath, outputFilePath } = workerData;
    console.log("Worker: Iniciando desencriptación.");
    await decryptFile(inputFilePath, outputFilePath);
    console.log("Worker: Desencriptación completada. Archivo desencriptado:", outputFilePath);
    parentPort.postMessage({ success: true, outputFilePath });
  } catch (err) {
    console.error("Worker: Error al desencriptar el archivo:", err);
    parentPort.postMessage({ success: false, error: err.message });
  }
})();
