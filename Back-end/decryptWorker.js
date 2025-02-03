const { parentPort, workerData } = require('worker_threads');
const fs = require('fs');
const crypto = require('crypto');
const { pipeline } = require('stream');
const { promisify } = require('util');
const pipelineAsync = promisify(pipeline);

const IV_SIZE = 12;    // 12 bytes para GCM
const TAG_SIZE = 16;   // 16 bytes para el Auth Tag
const SECRET_KEY = '1234567890123456'; // Debe coincidir con el front

/**
 * Función para desencriptar un archivo en modo streaming.
 * Se asume el formato: [ IV (12 bytes) | Ciphertext ... | Auth Tag (16 bytes) ]
 */
async function decryptFileStreaming(inputFilePath, outputFilePath) {
  // Obtener el tamaño del archivo
  const stats = fs.statSync(inputFilePath);
  const fileSize = stats.size;
  if (fileSize < IV_SIZE + TAG_SIZE) {
    throw new Error("Archivo demasiado corto para desencriptación streaming");
  }

  // Abrir el archivo y leer el IV y el Auth Tag sin cargar todo en memoria
  const fd = fs.openSync(inputFilePath, 'r');
  const iv = Buffer.alloc(IV_SIZE);
  fs.readSync(fd, iv, 0, IV_SIZE, 0);
  const authTag = Buffer.alloc(TAG_SIZE);
  fs.readSync(fd, authTag, 0, TAG_SIZE, fileSize - TAG_SIZE);
  fs.closeSync(fd);

  // Crear el decifrador con el algoritmo AES-128-GCM
  const decipher = crypto.createDecipheriv('aes-128-gcm', Buffer.from(SECRET_KEY, 'utf8'), iv);
  decipher.setAuthTag(authTag);

  // Crear un stream de lectura que omita el IV (primeros IV_SIZE bytes) y el Auth Tag (últimos TAG_SIZE bytes)
  const readStream = fs.createReadStream(inputFilePath, {
    start: IV_SIZE,
    end: fileSize - TAG_SIZE - 1  // 'end' es inclusivo, por ello se resta 1
  });
  const writeStream = fs.createWriteStream(outputFilePath);

  // Conectar los streams usando pipeline para procesar el archivo en bloques pequeños
  await pipelineAsync(readStream, decipher, writeStream);
}

(async () => {
  try {
    const { inputFilePath, outputFilePath } = workerData;
    console.log("Worker: Iniciando desencriptación en modo streaming.");
    await decryptFileStreaming(inputFilePath, outputFilePath);
    console.log("Worker: Desencriptación completada. Archivo desencriptado:", outputFilePath);
    parentPort.postMessage({ success: true, outputFilePath });
  } catch (err) {
    console.error("Worker: Error al desencriptar el archivo:", err);
    parentPort.postMessage({ success: false, error: err.message });
  }
})();
