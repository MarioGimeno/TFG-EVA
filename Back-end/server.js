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

const TMPDIR = process.env.TMPDIR || '/mnt/uploads/tmp';
if (!fs.existsSync(TMPDIR)) {
  fs.mkdirSync(TMPDIR, { recursive: true });
}

// Configurar almacenamiento de archivos en disco en lugar de RAM
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, TMPDIR);
  },
  filename: function (req, file, cb) {
    cb(null, Date.now() + '-' + file.originalname);
  }
});
const upload = multer({ storage: storage });

const PORT = process.env.PORT || 3000;
const gcs = new Storage({ keyFilename: process.env.GOOGLE_APPLICATION_CREDENTIALS });
const bucketName = process.env.BUCKET_NAME;

// 🔄 Desencriptar un archivo usando un worker thread con streams
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

// 📥 **Ruta para subir archivos**
app.post('/upload-video-location', upload.fields([
  { name: 'video', maxCount: 1000 },
  { name: 'location', maxCount: 1000 }
]), async (req, res) => {
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

    // Procesar archivo de ubicación
    const locationFilePath = locationFiles[0].path;
    const decryptedLocationPath = path.join(TMPDIR, `decrypted-${uuidv4()}.txt`);
    await runDecryptionWorker(locationFilePath, decryptedLocationPath);
    console.log(`✅ Ubicación desencriptada y guardada en: ${decryptedLocationPath}`);

    // Procesar cada video
    const uploadTasks = videoFiles.map(async (file) => {
      const decryptedVideoPath = path.join(TMPDIR, `decrypted-${uuidv4()}.mp4`);
      await runDecryptionWorker(file.path, decryptedVideoPath);
      console.log(`✅ Video desencriptado y guardado en: ${decryptedVideoPath}`);

      const folderName = uuidv4();
      console.log(`📂 Creando carpeta en GCS: ${folderName}`);

      const url = await uploadFilesToGCS(decryptedVideoPath, decryptedLocationPath, folderName);
      console.log(`🎉 Subida completa: ${url}`);

      // 🗑️ Borrar archivos temporales
      [file.path, decryptedVideoPath, decryptedLocationPath].forEach((filePath) => {
        if (fs.existsSync(filePath)) {
          fs.unlinkSync(filePath);
          console.log(`🗑️ Archivo eliminado: ${filePath}`);
        }
      });

      return { folderUrl: url, folderName };
    });

    const uploads = await Promise.all(uploadTasks);
    res.send({ message: 'Files uploaded successfully', uploads });
    
  } catch (error) {
    console.error('❌ Error en la subida:', error);
    res.status(500).send({ error: error.message });
  }
});

// 🚀 **Función para subir archivos a Google Cloud Storage usando streams**
async function uploadFilesToGCS(videoFilePath, textFilePath, folderName) {
  const bucket = gcs.bucket(bucketName);
  console.log('🚀 Iniciando subida de archivos a GCS...');

  await Promise.all([
    new Promise((resolve, reject) => {
      console.log(`📤 Subiendo video: ${videoFilePath} a ${folderName}/video.mp4`);
      fs.createReadStream(videoFilePath)
        .pipe(bucket.file(`${folderName}/video.mp4`).createWriteStream({ metadata: { contentType: 'video/mp4' } }))
        .on('finish', resolve)
        .on('error', reject);
    }),
    new Promise((resolve, reject) => {
      console.log(`📤 Subiendo ubicación: ${textFilePath} a ${folderName}/location.txt`);
      fs.createReadStream(textFilePath)
        .pipe(bucket.file(`${folderName}/location.txt`).createWriteStream({ metadata: { contentType: 'text/plain' } }))
        .on('finish', resolve)
        .on('error', reject);
    })
  ]);

  console.log('🎯 Todos los archivos han sido subidos.');
  return `https://storage.googleapis.com/${bucketName}/${folderName}/`;
}

app.listen(PORT, () => {
  console.log(`🚀 Servidor corriendo en el puerto ${PORT}`);
});
