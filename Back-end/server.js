require('dotenv').config({ path: './keys.env' });
const express = require('express');
const multer = require('multer');
const { v4: uuidv4 } = require('uuid');
const { Storage } = require('@google-cloud/storage');
const cors = require('cors');
const tmp = require('tmp');
const fs = require('fs');
const { Worker } = require('worker_threads');

const app = express();
app.use(cors());
app.use(express.json());

// Configuración de Multer para almacenamiento en memoria
const storage = multer.memoryStorage();
const upload = multer({ storage: storage });

const PORT = process.env.PORT || 3000;
const gcs = new Storage({
    keyFilename: process.env.GOOGLE_APPLICATION_CREDENTIALS
});
const bucketName = process.env.BUCKET_NAME;

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
 * Endpoint para subir archivos de video y ubicación sin encriptación.
 * Permite múltiples archivos (hasta 1000, ajustable) y los procesa de forma concurrente.
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
        const locationBuffer = locationFiles[0].buffer;
    
        const uploadTasks = videoFiles.map(file => {
          return runDecryptionWorker(file.buffer).then(decryptedVideoBuffer => {
            console.log('✅ Video desencriptado correctamente.');
            const videoTemp = tmp.fileSync({ postfix: '.mp4' });
            fs.writeFileSync(videoTemp.name, decryptedVideoBuffer);
            console.log(`✅ Video guardado temporalmente en: ${videoTemp.name}`);
    
            return runDecryptionWorker(locationBuffer).then(decryptedLocationBuffer => {
              console.log('✅ Ubicación desencriptada correctamente.');
              const locationTemp = tmp.fileSync({ postfix: '.txt' });
              fs.writeFileSync(locationTemp.name, decryptedLocationBuffer);
              console.log(`✅ Ubicación guardada temporalmente en: ${locationTemp.name}`);
    
              const folderName = uuidv4();
              console.log(`📂 Creando carpeta en GCS: ${folderName}`);
    
              return uploadFilesToGCS(videoTemp.name, locationTemp.name, folderName).then(url => {
                console.log(`🎉 Subida completa: ${url}`);
    
                if (fs.existsSync(videoTemp.name)) {
                  fs.unlinkSync(videoTemp.name);
                  console.log('🗑️ Archivo de video temporal eliminado.');
                }
                if (fs.existsSync(locationTemp.name)) {
                  fs.unlinkSync(locationTemp.name);
                  console.log('🗑️ Archivo de ubicación temporal eliminado.');
                }
    
                return { folderUrl: url, folderName };
              });
            });
          });
        });
    
        const uploads = await Promise.all(uploadTasks);
    
        res.send({
          message: 'Files uploaded successfully',
          uploads: uploads
        });
      } catch (error) {
        console.error('❌ Error en la subida:', error);
        res.status(500).send({ error: error.message });
      }
    }
  );
  

/**
 * Función para subir archivos a Google Cloud Storage.
 */
async function uploadFilesToGCS(videoFilePath, textFilePath, folderName) {
  const bucket = gcs.bucket(bucketName);
  console.log('🚀 Iniciando subida de archivos a GCS...');
  
  await Promise.all([
    new Promise((resolve, reject) => {
      console.log(`📤 Subiendo video: ${videoFilePath} a ${folderName}/video.mp4`);
      fs.createReadStream(videoFilePath)
        .pipe(
          bucket.file(`${folderName}/video.mp4`).createWriteStream({
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
          bucket.file(`${folderName}/location.txt`).createWriteStream({
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
