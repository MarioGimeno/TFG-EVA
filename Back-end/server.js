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

// Configurar TMPDIR: Asegúrate de que apunta a un disco con suficiente espacio, por ejemplo, /mnt/uploads/tmp.
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

// Parámetros para desencriptación
const IV_SIZE = 12;            // 12 bytes para GCM
const TAG_SIZE = 16;           // 16 bytes para el tag
const SECRET_KEY = '1234567890123456';  // Debe coincidir con el front
const MAGIC = Buffer.from("CHNK");      // Magic header para archivos encriptados en modo chunked

/**
 * Función para determinar si un archivo encriptado es de modo chunked.
 * Lee los primeros 4 bytes y los compara con MAGIC.
 */
function isChunked(filePath) {
  const fd = fs.openSync(filePath, 'r');
  const header = Buffer.alloc(4);
  fs.readSync(fd, header, 0, 4, 0);
  fs.closeSync(fd);
  return header.equals(MAGIC);
}

/**
 * Ejecuta un Worker Thread para desencriptar un archivo.
 * Se le pasan dos rutas: inputFilePath (archivo encriptado) y outputFilePath (destino para el archivo desencriptado).
 */
function runDecryptionWorker(inputFilePath, outputFilePath) {
  return new Promise((resolve, reject) => {
    const worker = new Worker('./decryptWorker.js', {
      workerData: { inputFilePath, outputFilePath }
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
 * Endpoint para subir archivos de video y ubicación encriptados.
 * Se permiten múltiples archivos.
 * Los archivos se procesan con Worker Threads para desencriptarlos y se suben a GCS usando streams y subidas reanudables.
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
      
      // Procesar el archivo de ubicación (usar el primer archivo recibido)
      const locationFilePath = locationFiles[0].path;
      const decryptedLocationPath = path.join(process.env.TMPDIR, `decrypted-${uuidv4()}.txt`);
      await runDecryptionWorker(locationFilePath, decryptedLocationPath);
      console.log(`✅ Ubicación desencriptada y guardada en: ${decryptedLocationPath}`);
      
      // Procesar cada video
      const uploadTasks = await Promise.all(videoFiles.map(async (file) => {
        const decryptedVideoPath = path.join(process.env.TMPDIR, `decrypted-${uuidv4()}.mp4`);
        await runDecryptionWorker(file.path, decryptedVideoPath);
        console.log(`✅ Video desencriptado y guardado en: ${decryptedVideoPath}`);
  
        const folderName = uuidv4();
        console.log(`📂 Creando carpeta en GCS: ${folderName}`);
        const url = await uploadFilesToGCS(decryptedVideoPath, decryptedLocationPath, folderName);
        console.log(`🎉 Subida completa: ${url}`);
  
        // Eliminar archivos temporales (original encriptado y desencriptado)
        [file.path, decryptedVideoPath].forEach(fp => {
          if (fs.existsSync(fp)) {
            fs.unlinkSync(fp);
            console.log(`🗑️ Archivo eliminado: ${fp}`);
          }
        });
  
        return { folderUrl: url, folderName };
      }));
  
      // Eliminar el archivo de ubicación desencriptado (usado para todos)
      if (fs.existsSync(decryptedLocationPath)) {
        fs.unlinkSync(decryptedLocationPath);
        console.log(`🗑️ Archivo de ubicación temporal eliminado: ${decryptedLocationPath}`);
      }
  
      res.send({ message: 'Files uploaded successfully', uploads: uploadTasks });
    } catch (error) {
      console.error('❌ Error en la subida:', error);
      res.status(500).send({ error: error.message });
    }
  }
);

/**
 * Función para subir archivos a Google Cloud Storage usando streams y subidas reanudables.
 */
async function uploadFilesToGCS(videoFilePath, textFilePath, folderName) {
  const bucket = gcs.bucket(bucketName);
  console.log('🚀 Iniciando subida de archivos a GCS...');
  
  await Promise.all([
    new Promise((resolve, reject) => {
      console.log(`📤 Subiendo video: ${videoFilePath} a ${folderName}/video.mp4`);
      fs.createReadStream(videoFilePath)
        .pipe(bucket.file(`${folderName}/video.mp4`).createWriteStream({
          metadata: { contentType: 'video/mp4' },
          resumable: true
        }))
        .on('finish', () => {
          console.log('✅ Video subido correctamente.');
          resolve();
        })
        .on('error', reject);
    }),
    new Promise((resolve, reject) => {
      console.log(`📤 Subiendo ubicación: ${textFilePath} a ${folderName}/location.txt`);
      fs.createReadStream(textFilePath)
        .pipe(bucket.file(`${folderName}/location.txt`).createWriteStream({
          metadata: { contentType: 'text/plain' },
          resumable: true
        }))
        .on('finish', () => {
          console.log('✅ Ubicación subida correctamente.');
          resolve();
        })
        .on('error', reject);
    })
  ]);
  
  console.log('🎯 Todos los archivos han sido subidos.');
  return `https://storage.googleapis.com/${bucketName}/${folderName}/`;
}

app.listen(PORT, () => {
  console.log(`🚀 Servidor corriendo en el puerto ${PORT}`);
});
