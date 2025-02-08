require('dotenv').config({ path: './keys.env' });
const express = require('express');
const multer = require('multer');
const { v4: uuidv4 } = require('uuid');
const { Storage } = require('@google-cloud/storage');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const { Worker } = require('worker_threads');

const app = express();
app.use(cors());
app.use(express.json());

// Configurar TMPDIR: Asegúrate de que apunta a un disco con suficiente espacio.
process.env.TMPDIR = process.env.TMPDIR || '/mnt/uploads/tmp';
if (!fs.existsSync(process.env.TMPDIR)) {
  fs.mkdirSync(process.env.TMPDIR, { recursive: true });
}
console.log('TMPDIR:', process.env.TMPDIR);

// Configuración de Multer: Guardamos los archivos en TMPDIR
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, process.env.TMPDIR);
  },
  filename: function (req, file, cb) {
    cb(null, Date.now() + '-' + file.originalname);
  }
});
const upload = multer({ storage: storage });

const PORT = process.env.PORT || 3000;
const gcs = new Storage({ keyFilename: process.env.GOOGLE_APPLICATION_CREDENTIALS });
const bucketName = process.env.BUCKET_NAME;

// Parámetros para desencriptación (deben coincidir con el front)
const IV_SIZE = 12;            // 12 bytes para GCM
const TAG_SIZE = 16;           // 16 bytes para el tag
const SECRET_KEY = '1234567890123456';  // Debe coincidir con el front
const MAGIC = Buffer.from("CHNK");      // Magic header para archivos encriptados en modo chunked

/**
 * Ejecuta un Worker Thread para desencriptar un archivo.
 * Se le pasan dos rutas: inputFilePath (archivo encriptado) y outputFilePath (destino para el archivo desencriptado).
 */
function runDecryptionWorker(inputFilePath, outputFilePath) {
  return new Promise((resolve, reject) => {
    const worker = new Worker('./decryptWorker.js', {
      workerData: { inputFilePath, outputFilePath, SECRET_KEY, IV_SIZE, TAG_SIZE }
    });
    worker.on('message', (message) => {
      if (message.success) {
        resolve(outputFilePath);
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
  });
}

/**
 * Función que une los chunks recibidos en un único archivo encriptado, lo desencripta y luego lo sube a GCS.
 * Se espera que los chunks se hayan guardado en "uploads/<fileId>/chunk_0, chunk_1, …".
 */
function assembleFile(fileId, totalChunks) {
  const fileDir = path.join('uploads', fileId);
  // El archivo ensamblado (encriptado) se guardará con este nombre:
  const encryptedFilePath = path.join('uploads', `${fileId}-encrypted.mp4`);
  // Una vez desencriptado se guardará aquí:
  const decryptedFilePath = path.join('uploads', `${fileId}-decrypted.mp4`);
  const writeStream = fs.createWriteStream(encryptedFilePath);
  
  let currentChunk = 0;
  
  function appendNextChunk() {
    if (currentChunk >= totalChunks) {
      writeStream.end();
      console.log('Archivo ensamblado correctamente:', encryptedFilePath);
      
      // Ejecutar el Worker para desencriptar el archivo
      runDecryptionWorker(encryptedFilePath, decryptedFilePath)
        .then(() => {
          console.log('Archivo desencriptado correctamente:', decryptedFilePath);
          // Subir video y ubicación al bucket
          uploadVideoAndLocationToGCS(decryptedFilePath, fileId)
            .then((url) => {
              console.log('Video y ubicación subidos correctamente:', url);
              // (Opcional) Limpieza de archivos temporales
              cleanupFiles(fileDir, encryptedFilePath, decryptedFilePath);
            })
            .catch(err => console.error('Error subiendo video/ubicación a GCS:', err));
        })
        .catch(err => console.error('Error al desencriptar el archivo:', err));
      return;
    }
    
    const chunkPath = path.join(fileDir, `chunk_${currentChunk}`);
    const readStream = fs.createReadStream(chunkPath);
    
    readStream.pipe(writeStream, { end: false });
    readStream.on('end', () => {
      console.log(`Chunk ${currentChunk} agregado.`);
      currentChunk++;
      appendNextChunk();
    });
    readStream.on('error', (err) => {
      console.error('Error al leer chunk:', err);
      writeStream.close();
    });
  }
  
  appendNextChunk();
}

/**
 * Función para subir el video desencriptado a GCS.
 * Se sube dentro de una carpeta identificada con el fileId.
 * Ahora, si existe el archivo de ubicación, éste se desencripta antes de ser subido.
 */
function uploadVideoAndLocationToGCS(videoFilePath, fileId) {
  return new Promise((resolve, reject) => {
    const bucket = gcs.bucket(bucketName);
    const videoDestination = `${fileId}/video.mp4`;
    const locationDestination = `${fileId}/location.txt`; // Ubicación desencriptada se subirá con este nombre
    console.log(`Iniciando subida del video desencriptado: ${videoFilePath} a ${videoDestination}`);
    
    // Primero, subir el video
    fs.createReadStream(videoFilePath)
      .pipe(bucket.file(videoDestination).createWriteStream({
        metadata: { contentType: 'video/mp4' },
        resumable: true
      }))
      .on('finish', () => {
        console.log('Video subido correctamente.');
        // Luego, buscar el archivo de ubicación encriptado
        const encryptedLocationPath = path.join('uploads', fileId, 'location.txt');
        if (fs.existsSync(encryptedLocationPath)) {
          console.log(`Ubicación encriptada encontrada en ${encryptedLocationPath}. Se procederá a desencriptarla.`);
          // Definir ruta para la ubicación desencriptada
          const decryptedLocationPath = path.join('uploads', `${fileId}-decrypted-location.txt`);
          // Ejecutar el worker para desencriptar la ubicación
          runDecryptionWorker(encryptedLocationPath, decryptedLocationPath)
            .then(() => {
              console.log(`Ubicación desencriptada correctamente: ${decryptedLocationPath}`);
              // Subir el archivo desencriptado de ubicación a GCS
              fs.createReadStream(decryptedLocationPath)
                .pipe(bucket.file(locationDestination).createWriteStream({
                  metadata: { contentType: 'text/plain' },
                  resumable: true
                }))
                .on('finish', () => {
                  const baseUrl = `https://storage.googleapis.com/${bucketName}/${fileId}/`;
                  console.log('Video y ubicación subidos correctamente:', baseUrl);
                  resolve(baseUrl);
                })
                .on('error', reject);
            })
            .catch(err => {
              console.error('Error desencriptando la ubicación:', err);
              reject(err);
            });
        } else {
          console.log('No se encontró archivo de ubicación. Se subirá solo el video.');
          const videoUrl = `https://storage.googleapis.com/${bucketName}/${videoDestination}`;
          resolve(videoUrl);
        }
      })
      .on('error', reject);
  });
}

/**
 * Función para limpiar archivos temporales.
 */
function cleanupFiles(chunksDir, encryptedFilePath, decryptedFilePath) {
  // Eliminar la carpeta de chunks
  if (fs.existsSync(chunksDir)) {
    fs.readdirSync(chunksDir).forEach(file => {
      fs.unlinkSync(path.join(chunksDir, file));
    });
    fs.rmdirSync(chunksDir);
    console.log('Carpeta de chunks eliminada:', chunksDir);
  }
  // Eliminar el archivo encriptado ensamblado
  if (fs.existsSync(encryptedFilePath)) {
    fs.unlinkSync(encryptedFilePath);
    console.log('Archivo encriptado temporal eliminado:', encryptedFilePath);
  }
  // Eliminar el archivo desencriptado (opcional, si ya fue subido)
  if (fs.existsSync(decryptedFilePath)) {
    fs.unlinkSync(decryptedFilePath);
    console.log('Archivo desencriptado temporal eliminado:', decryptedFilePath);
  }
}

/**
 * Endpoint para recibir cada chunk.
 * Se espera que el cliente envíe en el body: fileId, chunkIndex, totalChunks,
 * y en el campo 'chunkData' el archivo correspondiente.
 */
app.post('/upload-chunk', upload.single('chunkData'), (req, res) => {
  try {
    // Obtener metadatos enviados en el body
    const fileId = req.body.fileId;
    const chunkIndexStr = req.body.chunkIndex; // se recibe como String
    const totalChunks = req.body.totalChunks;   // para los video chunks, este valor es el total esperado

    // Crear carpeta específica para los chunks de este archivo
    const fileDir = path.join('uploads', fileId);
    if (!fs.existsSync(fileDir)) {
      fs.mkdirSync(fileDir, { recursive: true });
    }
    
    if (chunkIndexStr === "-1") { // comparamos correctamente
      // Caso especial: se trata de la ubicación
      const locationFilename = path.join(fileDir, "location.txt");
      fs.renameSync(req.file.path, locationFilename);
      console.log(`Ubicación recibida para fileId ${fileId}. Guardada en ${locationFilename}`);
    } else {
      // Caso normal: se trata de un chunk de video
      const chunkFilename = path.join(fileDir, `chunk_${chunkIndexStr}`);
      fs.renameSync(req.file.path, chunkFilename);
      console.log(`Chunk ${chunkIndexStr} del archivo ${fileId} recibido.`);
    }
    
    // Verificar si ya se han recibido todos los chunks del video (ignoramos la ubicación)
    // Solo se cuentan los archivos que empiecen con "chunk_"
    const receivedChunks = fs.readdirSync(fileDir).filter(name => name.startsWith('chunk_')).length;
    if (parseInt(totalChunks) === receivedChunks) {
      console.log('Todos los chunks del video recibidos. Iniciando el ensamblado.');
      assembleFile(fileId, totalChunks);
    }
    
    res.status(200).send({ message: 'Chunk recibido' });
  } catch (error) {
    console.error('Error en /upload-chunk:', error);
    res.status(500).send({ error: error.message });
  }
});

app.listen(PORT, () => {
  console.log(`🚀 Servidor corriendo en el puerto ${PORT}`);
});
