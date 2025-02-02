require('dotenv').config({ path: './keys.env' });

// Forzar que TMPDIR esté definido (si no lo está en el entorno)
process.env.TMPDIR = process.env.TMPDIR || '/mnt/uploads/tmp';
console.log('TMPDIR:', process.env.TMPDIR);

const express = require('express');
const multer = require('multer');
const { v4: uuidv4 } = require('uuid');
const { Storage } = require('@google-cloud/storage');
const cors = require('cors');
const tmp = require('tmp');
const fs = require('fs');
const crypto = require('crypto');
const { Worker } = require('worker_threads');
const path = require('path');
const { pipeline } = require('stream');
const { promisify } = require('util');
const pipelineAsync = promisify(pipeline);

const app = express();
app.use(cors());
app.use(express.json());

// Configuración de Multer usando diskStorage para no usar RAM excesiva
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    const uploadDir = path.join(__dirname, 'uploads');
    if (!fs.existsSync(uploadDir)) {
      fs.mkdirSync(uploadDir, { recursive: true });
    }
    cb(null, uploadDir);
  },
  filename: function (req, file, cb) {
    cb(null, Date.now() + '-' + file.originalname);
  }
});
const upload = multer({ storage: storage });

const PORT = process.env.PORT || 3000;
const gcs = new Storage({
  keyFilename: process.env.GOOGLE_APPLICATION_CREDENTIALS
});
const bucketName = process.env.BUCKET_NAME;

// Parámetros para desencriptación
const IV_SIZE = 12;            // 12 bytes para GCM
const TAG_SIZE = 16;           // 16 bytes para el tag
const SECRET_KEY = '1234567890123456';  // Clave compartida (debe coincidir con el front)
const MAGIC = Buffer.from("CHNK");      // Magic header para archivos chunked

// Umbral (en bytes) que define el modo streaming en el front
const THRESHOLD = 16 * 1024 * 1024;

/**
 * Función auxiliar para determinar si un archivo está en formato chunked.
 * Lee los primeros 4 bytes y los compara con MAGIC.
 */
function isChunked(encryptedFilePath) {
  const fd = fs.openSync(encryptedFilePath, 'r');
  const header = Buffer.alloc(4);
  fs.readSync(fd, header, 0, 4, 0);
  fs.closeSync(fd);
  return header.equals(MAGIC);
}

/**
 * Función para desencriptar en modo streaming (para archivos que NO usan chunked).
 * Supone que el archivo tiene el siguiente formato:
 * [ IV (12 bytes) | Ciphertext ... | Auth Tag (16 bytes) ]
 * Procesa el archivo sin cargarlo completamente en memoria.
 */
function decryptFileStreaming(encryptedFilePath, decryptedFilePath) {
  return new Promise((resolve, reject) => {
    // Obtener el tamaño del archivo
    const stats = fs.statSync(encryptedFilePath);
    const fileSize = stats.size;
    if (fileSize < IV_SIZE + TAG_SIZE) {
      return reject(new Error("Archivo demasiado corto para encriptación streaming"));
    }
    // Leer el IV (primeros 12 bytes)
    const fd = fs.openSync(encryptedFilePath, 'r');
    const ivBuffer = Buffer.alloc(IV_SIZE);
    fs.readSync(fd, ivBuffer, 0, IV_SIZE, 0);
    fs.closeSync(fd);
    
    // Leer el Auth Tag (últimos 16 bytes)
    const fd2 = fs.openSync(encryptedFilePath, 'r');
    const tagBuffer = Buffer.alloc(TAG_SIZE);
    fs.readSync(fd2, tagBuffer, 0, TAG_SIZE, fileSize - TAG_SIZE);
    fs.closeSync(fd2);
    
    // Crear un stream de lectura que omita el IV y el tag
    const readStream = fs.createReadStream(encryptedFilePath, { start: IV_SIZE, end: fileSize - TAG_SIZE - 1 });
    // Crear el decipher stream
    const decipher = crypto.createDecipheriv('aes-128-gcm', Buffer.from(SECRET_KEY, 'utf8'), ivBuffer);
    decipher.setAuthTag(tagBuffer);
    const writeStream = fs.createWriteStream(decryptedFilePath);
    
    pipelineAsync(readStream, decipher, writeStream)
      .then(() => resolve(decryptedFilePath))
      .catch(err => reject(err));
  });
}

/**
 * Ejecuta un Worker Thread para desencriptar un buffer encriptado (usado para archivos chunked).
 */
function runDecryptionWorker(encryptedBuffer) {
  return new Promise((resolve, reject) => {
    const worker = new Worker('./decryptWorker.js');
    worker.on('message', (message) => {
      if (message.success) {
        resolve(Buffer.from(message.decryptedBuffer));
      } else {
        reject(new Error(message.error));
      }
    });
    worker.on('error', reject);
    worker.on('exit', (code) => {
      if (code !== 0) {
        reject(new Error(`Worker stopped with exit code ${code}`));
      }
    });
    worker.postMessage(encryptedBuffer);
  });
}

/**
 * Endpoint para subir archivos de video y ubicación encriptados.
 * Permite múltiples archivos. Se usa streaming para archivos en formato streaming;
 * y para archivos en chunked se usa el Worker Thread.
 */
app.post(
  '/upload-video-location',
  upload.fields([
    { name: 'video', maxCount: 1000 },
    { name: 'location', maxCount: 1000 }
  ]),
  async (req, res) => {
    try {
      console.log('📥 Recibiendo archivos...');
      const videoFiles = req.files.video;
      const locationFiles = req.files.location;
      
      if (!videoFiles || videoFiles.length === 0) {
        return res.status(400).send({ error: 'No video files received' });
      }
      if (!locationFiles || locationFiles.length === 0) {
        return res.status(400).send({ error: 'No location file received' });
      }
  
      // Procesar la ubicación (usamos el primer archivo de ubicación para todos)
      const locationFilePath = locationFiles[0].path;
      let decryptedLocationPath;
      // Determinar el modo de la ubicación
      if (isChunked(locationFilePath)) {
        // Si está en modo chunked, leemos el archivo completo y usamos el worker
        const encryptedLocationBuffer = fs.readFileSync(locationFilePath);
        const decryptedLocationBuffer = await runDecryptionWorker(encryptedLocationBuffer);
        decryptedLocationPath = path.join(process.env.TMPDIR, Date.now() + '-location.txt');
        fs.writeFileSync(decryptedLocationPath, decryptedLocationBuffer);
        console.log(`✅ Ubicación desencriptada y guardada en: ${decryptedLocationPath}`);
      } else {
        // Si está en streaming, usa la función de streaming
        decryptedLocationPath = path.join(process.env.TMPDIR, Date.now() + '-location.txt');
        await decryptFileStreaming(locationFilePath, decryptedLocationPath);
        console.log(`✅ Ubicación desencriptada (streaming) y guardada en: ${decryptedLocationPath}`);
      }
  
      const uploadTasks = await Promise.all(videoFiles.map(async (file) => {
        let decryptedVideoPath;
        // Determinar el modo del video
        if (isChunked(file.path)) {
          // Modo chunked: usar worker
          const encryptedVideoBuffer = fs.readFileSync(file.path);
          const decryptedVideoBuffer = await runDecryptionWorker(encryptedVideoBuffer);
          decryptedVideoPath = path.join(process.env.TMPDIR, Date.now() + '-video.mp4');
          fs.writeFileSync(decryptedVideoPath, decryptedVideoBuffer);
          console.log(`✅ Video desencriptado (chunked) y guardado en: ${decryptedVideoPath}`);
        } else {
          // Modo streaming: usar función de streaming
          decryptedVideoPath = path.join(process.env.TMPDIR, Date.now() + '-video.mp4');
          await decryptFileStreaming(file.path, decryptedVideoPath);
          console.log(`✅ Video desencriptado (streaming) y guardado en: ${decryptedVideoPath}`);
        }
  
        const folderName = uuidv4();
        console.log(`📂 Creando carpeta en GCS: ${folderName}`);
  
        const url = await uploadFilesToGCS(decryptedVideoPath, decryptedLocationPath, folderName);
        console.log(`🎉 Subida completa: ${url}`);
  
        // Limpiar archivos temporales y originales
        if (fs.existsSync(decryptedVideoPath)) {
          fs.unlinkSync(decryptedVideoPath);
          console.log('🗑️ Archivo de video temporal eliminado.');
        }
        if (fs.existsSync(file.path)) {
          fs.unlinkSync(file.path);
          console.log(`🗑️ Archivo de video encriptado ${file.path} eliminado.`);
        }
  
        return { folderUrl: url, folderName };
      }));
  
      // Limpiar el archivo temporal de ubicación (usado para todos)
      if (fs.existsSync(decryptedLocationPath)) {
        fs.unlinkSync(decryptedLocationPath);
        console.log('🗑️ Archivo de ubicación temporal eliminado.');
      }
  
      res.send({
        message: 'Files uploaded successfully',
        uploads: uploadTasks
      });
    } catch (error) {
      console.error('❌ Error en la subida:', error);
      res.status(500).send({ error: error.message });
    }
  }
);

/**
 * Función para subir archivos a Google Cloud Storage utilizando streams.
 */
async function uploadFilesToGCS(videoFilePath, textFilePath, folderName) {
  const bucket = gcs.bucket(bucketName);
  console.log('🚀 Iniciando subida de archivos a GCS...');
  
  await Promise.all([
    new Promise((resolve, reject) => {
      console.log(`📤 Subiendo video: ${videoFilePath} a ${folderName}/video.mp4`);
      fs.createReadStream(videoFilePath)
        .pipe(
          bucket
            .file(`${folderName}/video.mp4`)
            .createWriteStream({
              metadata: { contentType: 'video/mp4' }
            })
        )
        .on('finish', () => {
          console.log('✅ Video subido correctamente.');
          resolve();
        })
        .on('error', (err) => {
          console.error('❌ Error subiendo el video:', err);
          reject(err);
        });
    }),
    new Promise((resolve, reject) => {
      console.log(`📤 Subiendo ubicación: ${textFilePath} a ${folderName}/location.txt`);
      fs.createReadStream(textFilePath)
        .pipe(
          bucket
            .file(`${folderName}/location.txt`)
            .createWriteStream({
              metadata: { contentType: 'text/plain' }
            })
        )
        .on('finish', () => {
          console.log('✅ Ubicación subida correctamente.');
          resolve();
        })
        .on('error', (err) => {
          console.error('❌ Error subiendo la ubicación:', err);
          reject(err);
        });
    })
  ]);
  
  console.log('🎯 Todos los archivos han sido subidos.');
  return `https://storage.googleapis.com/${bucketName}/${folderName}/`;
}

app.listen(PORT, () => {
  console.log(`🚀 Servidor corriendo en el puerto ${PORT}`);
});
