require('dotenv').config({ path: './keys.env' });
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

const app = express();
app.use(cors());
app.use(express.json());

// Configuración de Multer usando diskStorage para evitar uso excesivo de RAM
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    // Guarda los archivos en el directorio "uploads" dentro del directorio actual
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
const MAGIC = Buffer.from("CHNK");      // Magic header para archivos encriptados en modo chunked

/**
 * Ejecuta un Worker Thread para desencriptar un buffer encriptado.
 * Se espera que el Worker (en decryptWorker.js) procese el buffer y devuelva el ArrayBuffer desencriptado.
 */
function runDecryptionWorker(encryptedBuffer) {
  return new Promise((resolve, reject) => {
    const worker = new Worker('./decryptWorker.js'); // Asegúrate de que la ruta es correcta
    worker.on('message', (message) => {
      if (message.success) {
        // Convertir el ArrayBuffer recibido en un Buffer de Node.js
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
    // Enviar el buffer encriptado directamente
    worker.postMessage(encryptedBuffer);
  });
}

/**
 * Endpoint para subir archivos de video y ubicación encriptados.
 * Se permite la recepción de múltiples archivos, que se procesan usando Worker Threads.
 * Los archivos se leen desde disco (por multer.diskStorage()) y luego se desencriptan.
 * Los archivos temporales (desencriptados) se crean en /mnt/uploads para evitar problemas de espacio.
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
  
      // Usamos el primer archivo de ubicación para todos los videos
      const locationFilePath = locationFiles[0].path;
      const locationBuffer = fs.readFileSync(locationFilePath);
  
      const uploadTasks = await Promise.all(videoFiles.map(async (file) => {
        // Leer el archivo encriptado desde disco
        const encryptedVideoBuffer = fs.readFileSync(file.path);
        // Desencriptar el video usando un Worker Thread
        const decryptedVideoBuffer = await runDecryptionWorker(encryptedVideoBuffer);
        console.log('✅ Video desencriptado correctamente.');
  
        // Guardar el video desencriptado en un archivo temporal en /mnt/uploads
        const videoTemp = tmp.fileSync({ postfix: '.mp4', dir: '/mnt/uploads' });
        fs.writeFileSync(videoTemp.name, decryptedVideoBuffer);
        console.log(`✅ Video guardado temporalmente en: ${videoTemp.name}`);
  
        // Desencriptar la ubicación (para este ejemplo, se utiliza el mismo archivo para todos)
        const decryptedLocationBuffer = await runDecryptionWorker(locationBuffer);
        console.log('✅ Ubicación desencriptada correctamente.');
        const locationTemp = tmp.fileSync({ postfix: '.txt', dir: '/mnt/uploads' });
        fs.writeFileSync(locationTemp.name, decryptedLocationBuffer);
        console.log(`✅ Ubicación guardada temporalmente en: ${locationTemp.name}`);
  
        const folderName = uuidv4();
        console.log(`📂 Creando carpeta en GCS: ${folderName}`);
  
        const url = await uploadFilesToGCS(videoTemp.name, locationTemp.name, folderName);
        console.log(`🎉 Subida completa: ${url}`);
  
        // Limpiar archivos temporales y originales
        if (fs.existsSync(videoTemp.name)) {
          fs.unlinkSync(videoTemp.name);
          console.log('🗑️ Archivo de video temporal eliminado.');
        }
        if (fs.existsSync(locationTemp.name)) {
          fs.unlinkSync(locationTemp.name);
          console.log('🗑️ Archivo de ubicación temporal eliminado.');
        }
        if (fs.existsSync(file.path)) {
          fs.unlinkSync(file.path);
          console.log(`🗑️ Archivo de video encriptado ${file.path} eliminado.`);
        }
  
        return { folderUrl: url, folderName };
      }));
  
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
