const { parentPort, workerData } = require('worker_threads');
const fs = require('fs');
const crypto = require('crypto');
const { pipeline } = require('stream');
const { promisify } = require('util');
const pipelineAsync = promisify(pipeline);

const IV_SIZE = 12;             // 12 bytes para GCM
const TAG_SIZE = 16;            // 16 bytes para el Auth Tag
const SECRET_KEY = '1234567890123456'; // Debe coincidir con el front (AES-128-GCM requiere 16 bytes)
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
 * Desencripta un archivo en modo chunked sin cargar todo el archivo en memoria.
 * Se asume el formato:
 *   [ MAGIC (4 bytes) | chunkSize (4 bytes) | (para cada chunk: [ ivLength (4 bytes) | IV | encryptedChunkLength (4 bytes) | encryptedChunk (ciphertext + Auth Tag) ] )
 *
 * En este refactor se procesa el archivo de forma secuencial leyendo cada parte en memoria y escribiendo el resultado en un stream de salida.
 */
async function decryptChunkedFile(inputFilePath, outputFilePath) {
  // Abrir el archivo en modo lectura utilizando promesas
  const fd = await fs.promises.open(inputFilePath, 'r');
  let offset = 0;

  // Leer el header: MAGIC (4 bytes) y chunkSize (4 bytes)
  const headerBuffer = Buffer.alloc(8);
  await fd.read(headerBuffer, 0, 8, offset);
  offset += 8;

  const magicHeader = headerBuffer.slice(0, 4).toString('utf8');
  if (magicHeader !== MAGIC) {
    await fd.close();
    throw new Error("Archivo no tiene el magic header esperado.");
  }
  const declaredChunkSize = headerBuffer.readInt32BE(4);
  console.log("Tamaño de chunk declarado:", declaredChunkSize);

  // Crear un stream de escritura para el archivo desencriptado
  const writeStream = fs.createWriteStream(outputFilePath);

  // Procesar cada chunk secuencialmente
  while (true) {
    // Leer la longitud del IV (4 bytes)
    const ivLengthBuffer = Buffer.alloc(4);
    const { bytesRead: br1 } = await fd.read(ivLengthBuffer, 0, 4, offset);
    if (br1 < 4) break; // Fin del archivo
    offset += 4;
    const ivLength = ivLengthBuffer.readInt32BE(0);

    // Leer el IV
    const iv = Buffer.alloc(ivLength);
    await fd.read(iv, 0, ivLength, offset);
    offset += ivLength;

    // Leer la longitud del chunk encriptado (4 bytes)
    const encChunkLengthBuffer = Buffer.alloc(4);
    await fd.read(encChunkLengthBuffer, 0, 4, offset);
    offset += 4;
    const encryptedChunkLength = encChunkLengthBuffer.readInt32BE(0);

    // Leer el chunk encriptado (ciphertext + Auth Tag)
    const encryptedChunk = Buffer.alloc(encryptedChunkLength);
    await fd.read(encryptedChunk, 0, encryptedChunkLength, offset);
    offset += encryptedChunkLength;

    if (encryptedChunk.length < TAG_SIZE) {
      await fd.close();
      throw new Error("Chunk encriptado demasiado corto para tener Auth Tag");
    }
    // Separar el Auth Tag del ciphertext
    const ciphertext = encryptedChunk.slice(0, encryptedChunk.length - TAG_SIZE);
    const authTag = encryptedChunk.slice(encryptedChunk.length - TAG_SIZE);

    // Crear el objeto decipher para este chunk
    const decipher = crypto.createDecipheriv('aes-128-gcm', Buffer.from(SECRET_KEY, 'utf8'), iv);
    decipher.setAuthTag(authTag);

    // Desencriptar el chunk
    const decryptedChunk = Buffer.concat([decipher.update(ciphertext), decipher.final()]);

    // Escribir el chunk desencriptado en el stream de salida
    writeStream.write(decryptedChunk);
  }

  await fd.close();
  writeStream.end();
  console.log(`Archivo ensamblado y desencriptado correctamente: ${outputFilePath}`);
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
