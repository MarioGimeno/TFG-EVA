// Importa los objetos necesarios del módulo worker_threads para comunicarse con el thread padre
const { parentPort, workerData } = require('worker_threads');
// Importa el módulo para manejo de archivos
const fs = require('fs');
// Importa el módulo de criptografía
const crypto = require('crypto');
// Importa la función pipeline del módulo stream
const { pipeline } = require('stream');
// Importa la función promisify para convertir funciones basadas en callbacks en promesas
const { promisify } = require('util');
// Crea una versión asíncrona de pipeline para trabajar con promesas
const pipelineAsync = promisify(pipeline);
// Cargar las variables de entorno desde el archivo keys.env
require('dotenv').config({ path: './keys.env' });
// Configuración de parámetros de desencriptación obtenidos desde las variables de entorno
const IV_SIZE = parseInt(process.env.IV_SIZE, 10);           // Tamaño del vector de inicialización (IV)
const TAG_SIZE = parseInt(process.env.TAG_SIZE, 10);         // Tamaño del Auth Tag
const SECRET_KEY = process.env.SECRET_KEY;                   // Clave secreta para la encriptación/desencriptación
const MAGIC = Buffer.from(process.env.MAGIC);                // Encabezado mágico que identifica el formato chunked

/**
 * Función principal que detecta el formato del archivo y llama a la función de desencriptación adecuada.
 * Si el archivo inicia con el magic header (4 bytes) igual a MAGIC, se asume que es un archivo chunked;
 * de lo contrario, se asume que es un archivo en modo streaming.
 *
 * @param {string} inputFilePath  Ruta del archivo encriptado de entrada.
 * @param {string} outputFilePath Ruta donde se escribirá el archivo desencriptado.
 */
async function decryptFile(inputFilePath, outputFilePath) {
  // Obtener estadísticas del archivo para conocer su tamaño
  const stats = fs.statSync(inputFilePath);
  const fileSize = stats.size;
  // Si el archivo es demasiado pequeño para contener IV y Auth Tag, se lanza un error
  if (fileSize < IV_SIZE + TAG_SIZE) {
    throw new Error("Archivo demasiado corto para desencriptación");
  }

  // Se abre el archivo de forma sincrónica para leer los primeros 4 bytes y detectar el formato
  const fd = fs.openSync(inputFilePath, 'r');
  const header = Buffer.alloc(4);
  fs.readSync(fd, header, 0, 4, 0); // Lee 4 bytes desde el inicio
  fs.closeSync(fd);

  // Compara el header leído con el MAGIC; si coinciden, se asume formato chunked
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
 * Se espera el formato:
 *   [ IV (12 bytes) | Ciphertext ... | Auth Tag (16 bytes) ]
 *
 * Este método no carga todo el archivo en memoria, sino que utiliza streams.
 *
 * @param {string} inputFilePath  Ruta del archivo encriptado.
 * @param {string} outputFilePath Ruta del archivo desencriptado.
 */
async function decryptFileStreaming(inputFilePath, outputFilePath) {
  // Obtiene el tamaño del archivo
  const stats = fs.statSync(inputFilePath);
  const fileSize = stats.size;
  if (fileSize < IV_SIZE + TAG_SIZE) {
    throw new Error("Archivo demasiado corto para desencriptación streaming");
  }

  // Abre el archivo y lee el IV (primeros IV_SIZE bytes) y el Auth Tag (últimos TAG_SIZE bytes)
  const fd = fs.openSync(inputFilePath, 'r');
  const iv = Buffer.alloc(IV_SIZE);
  fs.readSync(fd, iv, 0, IV_SIZE, 0);
  const authTag = Buffer.alloc(TAG_SIZE);
  fs.readSync(fd, authTag, 0, TAG_SIZE, fileSize - TAG_SIZE);
  fs.closeSync(fd);

  // Crea el objeto "decipher" utilizando AES-128-GCM con la clave, IV y luego configura el Auth Tag
  const decipher = crypto.createDecipheriv('aes-128-gcm', Buffer.from(SECRET_KEY, 'utf8'), iv);
  decipher.setAuthTag(authTag);

  // Crea un stream de lectura que omite el IV y el Auth Tag (se lee desde la posición IV_SIZE hasta fileSize - TAG_SIZE)
  const readStream = fs.createReadStream(inputFilePath, {
    start: IV_SIZE,
    end: fileSize - TAG_SIZE - 1  // 'end' es inclusivo
  });
  // Crea un stream de escritura para el archivo desencriptado
  const writeStream = fs.createWriteStream(outputFilePath);

  // Conecta el stream de lectura, el objeto decipher y el stream de escritura usando pipelineAsync
  await pipelineAsync(readStream, decipher, writeStream);
}

/**
 * Desencripta un archivo en modo chunked sin cargar todo el archivo en memoria.
 *
 * Se asume el siguiente formato:
 *   [ MAGIC (4 bytes) | chunkSize (4 bytes) |
 *     (Para cada chunk: [ ivLength (4 bytes) | IV | encryptedChunkLength (4 bytes) |
 *                          encryptedChunk (ciphertext + Auth Tag) ]) ]
 *
 * Se procesa el archivo secuencialmente, leyendo cada parte en memoria y escribiendo el resultado
 * en un stream de salida.
 *
 * @param {string} inputFilePath  Ruta del archivo encriptado en formato chunked.
 * @param {string} outputFilePath Ruta donde se escribirá el archivo desencriptado.
 */
async function decryptChunkedFile(inputFilePath, outputFilePath) {
  // Abre el archivo de entrada en modo lectura utilizando promesas
  const fd = await fs.promises.open(inputFilePath, 'r');
  let offset = 0;

  // Lee el header que contiene el MAGIC (4 bytes) y el tamaño del chunk (4 bytes)
  const headerBuffer = Buffer.alloc(8);
  await fd.read(headerBuffer, 0, 8, offset);
  offset += 8;

  // Verifica que el magic header coincida con el esperado
  const magicHeader = headerBuffer.slice(0, 4).toString('utf8');
  if (magicHeader !== MAGIC) {
    await fd.close();
    throw new Error("Archivo no tiene el magic header esperado.");
  }
  // Lee el tamaño de chunk declarado en el header (4 bytes en big-endian)
  const declaredChunkSize = headerBuffer.readInt32BE(4);
  console.log("Tamaño de chunk declarado:", declaredChunkSize);

  // Crea un stream de escritura para generar el archivo desencriptado
  const writeStream = fs.createWriteStream(outputFilePath);

  // Procesa cada chunk de forma secuencial
  while (true) {
    // Lee 4 bytes que indican la longitud del IV para el siguiente chunk
    const ivLengthBuffer = Buffer.alloc(4);
    const { bytesRead: br1 } = await fd.read(ivLengthBuffer, 0, 4, offset);
    if (br1 < 4) break; // Si no se leen 4 bytes, se asume fin de archivo
    offset += 4;
    const ivLength = ivLengthBuffer.readInt32BE(0);

    // Lee el IV, según la longitud leída
    const iv = Buffer.alloc(ivLength);
    await fd.read(iv, 0, ivLength, offset);
    offset += ivLength;

    // Lee 4 bytes que indican la longitud del chunk encriptado (ciphertext + Auth Tag)
    const encChunkLengthBuffer = Buffer.alloc(4);
    await fd.read(encChunkLengthBuffer, 0, 4, offset);
    offset += 4;
    const encryptedChunkLength = encChunkLengthBuffer.readInt32BE(0);

    // Lee el chunk encriptado completo
    const encryptedChunk = Buffer.alloc(encryptedChunkLength);
    await fd.read(encryptedChunk, 0, encryptedChunkLength, offset);
    offset += encryptedChunkLength;

    // Verifica que el chunk tenga al menos TAG_SIZE bytes para el Auth Tag
    if (encryptedChunk.length < TAG_SIZE) {
      await fd.close();
      throw new Error("Chunk encriptado demasiado corto para tener Auth Tag");
    }
    // Separa el contenido en dos partes: el ciphertext y el Auth Tag (últimos TAG_SIZE bytes)
    const ciphertext = encryptedChunk.slice(0, encryptedChunk.length - TAG_SIZE);
    const authTag = encryptedChunk.slice(encryptedChunk.length - TAG_SIZE);

    // Crea un objeto decipher para desencriptar este chunk usando AES-128-GCM
    const decipher = crypto.createDecipheriv('aes-128-gcm', Buffer.from(SECRET_KEY, 'utf8'), iv);
    decipher.setAuthTag(authTag);

    // Desencripta el chunk, concatenando el resultado de decipher.update y decipher.final()
    const decryptedChunk = Buffer.concat([decipher.update(ciphertext), decipher.final()]);

    // Escribe el chunk desencriptado en el stream de salida
    writeStream.write(decryptedChunk);
  }

  // Cierra el descriptor de archivo y finaliza el stream de escritura
  await fd.close();
  writeStream.end();
  console.log(`Archivo ensamblado y desencriptado correctamente: ${outputFilePath}`);
}

// Función autoejecutable para iniciar el proceso de desencriptación en el Worker
(async () => {
  try {
    // Extrae las rutas de entrada y salida desde workerData, que se pasan al iniciar el Worker
    const { inputFilePath, outputFilePath } = workerData;
    console.log("Worker: Iniciando desencriptación.");
    // Llama a la función principal de desencriptación, que selecciona el método adecuado según el formato
    await decryptFile(inputFilePath, outputFilePath);
    console.log("Worker: Desencriptación completada. Archivo desencriptado:", outputFilePath);
    // Envía un mensaje al thread padre indicando éxito y la ruta del archivo desencriptado
    parentPort.postMessage({ success: true, outputFilePath });
  } catch (err) {
    console.error("Worker: Error al desencriptar el archivo:", err);
    // Envía un mensaje al thread padre indicando error, con el mensaje correspondiente
    parentPort.postMessage({ success: false, error: err.message });
  }
})();
