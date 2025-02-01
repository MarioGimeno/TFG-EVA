require('dotenv').config({ path: './keys.env' });
const express = require('express');
const multer = require('multer');
const { v4: uuidv4 } = require('uuid');
const { Storage } = require('@google-cloud/storage');
const cors = require('cors');
const tmp = require('tmp');
const fs = require('fs');

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
  
      // Procesar cada video de forma concurrente
      const uploads = await Promise.all(videoFiles.map(async (file) => {
        // No se realiza desencriptación, se usa directamente el buffer recibido
        console.log('✅ Procesando video sin desencriptar.');
  
        // Guardar el video recibido en un archivo temporal
        const videoTemp = tmp.fileSync({ postfix: '.mp4' });
        fs.writeFileSync(videoTemp.name, file.buffer);
        console.log(`✅ Video guardado temporalmente en: ${videoTemp.name}`);
  
        // Para la ubicación, se utiliza el primer archivo recibido (sin desencriptar)
        console.log('✅ Procesando ubicación sin desencriptar.');
        const locationTemp = tmp.fileSync({ postfix: '.txt' });
        fs.writeFileSync(locationTemp.name, locationBuffer);
        console.log(`✅ Ubicación guardada temporalmente en: ${locationTemp.name}`);
  
        const folderName = uuidv4();
        console.log(`📂 Creando carpeta en GCS: ${folderName}`);
  
        // Subir ambos archivos a Google Cloud Storage
        const url = await uploadFilesToGCS(videoTemp.name, locationTemp.name, folderName);
        console.log(`🎉 Subida completa: ${url}`);
  
        // Limpiar archivos temporales
        if (fs.existsSync(videoTemp.name)) {
          fs.unlinkSync(videoTemp.name);
          console.log('🗑️ Archivo de video temporal eliminado.');
        }
        if (fs.existsSync(locationTemp.name)) {
          fs.unlinkSync(locationTemp.name);
          console.log('🗑️ Archivo de ubicación temporal eliminado.');
        }
  
        return { folderUrl: url, folderName };
      }));
  
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
