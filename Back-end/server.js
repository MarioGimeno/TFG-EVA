require('dotenv').config({ path: './keys.env' });
const express = require('express');
const multer = require('multer');
const { v4: uuidv4 } = require('uuid');
const { Storage } = require('@google-cloud/storage');
const cors = require('cors');
const tmp = require('tmp');
const fs = require('fs');
const crypto = require('crypto');

const app = express();
app.use(cors());
app.use(express.json());

const storage = multer.memoryStorage();
const upload = multer({ storage: storage });

const PORT = process.env.PORT || 3000;
const gcs = new Storage({
    keyFilename: process.env.GOOGLE_APPLICATION_CREDENTIALS
});
const bucketName = process.env.BUCKET_NAME;
const IV_SIZE = 12;    // 12 bytes para GCM
const TAG_SIZE = 16;   // 16 bytes para el tag
const SECRET_KEY = '1234567890123456'; // Debe coincidir con el cliente
const MAGIC = Buffer.from("CHNK"); // Magic number para archivos chunked

/**
 * Función para desencriptar un buffer que puede estar en formato streaming o chunked.
 * Para formato chunked se espera que el buffer comience con la cabecera MAGIC ("CHNK")
 * seguida de un entero que indica el tamaño de chunk, y luego, para cada chunk:
 * - Un entero con la longitud del IV
 * - El IV (12 bytes generalmente)
 * - Un entero con la longitud del segmento encriptado
 * - El segmento encriptado (que incluye el tag al final)
 *
 * Para formato streaming se espera:
 * - Los primeros 12 bytes son el IV
 * - El resto es el ciphertext con el tag (últimos 16 bytes)
 */
function decryptFileFlexible(inputBuffer) {
  try {
    // Verificamos si el buffer empieza con el magic number "CHNK"
    if (inputBuffer.slice(0, 4).equals(MAGIC)) {
      // --- Formato chunked ---
      console.log("Formato chunked detectado.");
      let offset = 4;
      // Leer el tamaño de chunk (int32) que escribiste en el header
      const chunkSize = inputBuffer.readInt32BE(offset);
      offset += 4;
      const decryptedChunks = [];
      while (offset < inputBuffer.length) {
        // Leer la longitud del IV
        const ivLength = inputBuffer.readInt32BE(offset);
        offset += 4;
        const iv = inputBuffer.slice(offset, offset + ivLength);
        offset += ivLength;

        // Leer la longitud del chunk encriptado
        const encChunkLength = inputBuffer.readInt32BE(offset);
        offset += 4;
        const encryptedChunk = inputBuffer.slice(offset, offset + encChunkLength);
        offset += encChunkLength;

        // En el modo chunked, cada encryptedChunk se generó con doFinal(),
        // por lo que incluye el tag al final (últimos TAG_SIZE bytes)
        if (encryptedChunk.length < TAG_SIZE) {
          throw new Error("Encrypted chunk too short to contain auth tag");
        }
        const authTag = encryptedChunk.slice(encryptedChunk.length - TAG_SIZE);
        const ciphertext = encryptedChunk.slice(0, encryptedChunk.length - TAG_SIZE);

        const decipher = crypto.createDecipheriv('aes-128-gcm', Buffer.from(SECRET_KEY, 'utf8'), iv);
        decipher.setAuthTag(authTag);
        const decryptedChunk = Buffer.concat([decipher.update(ciphertext), decipher.final()]);
        decryptedChunks.push(decryptedChunk);
      }
      return Buffer.concat(decryptedChunks);
    } else {
      // --- Formato streaming ---
      console.log("Formato streaming detectado.");
      if (inputBuffer.length < IV_SIZE + TAG_SIZE) {
        throw new Error("Buffer demasiado corto para encriptación streaming");
      }
      const iv = inputBuffer.slice(0, IV_SIZE);
      const tag = inputBuffer.slice(inputBuffer.length - TAG_SIZE);
      const ciphertext = inputBuffer.slice(IV_SIZE, inputBuffer.length - TAG_SIZE);

      const decipher = crypto.createDecipheriv('aes-128-gcm', Buffer.from(SECRET_KEY, 'utf8'), iv);
      decipher.setAuthTag(tag);
      return Buffer.concat([decipher.update(ciphertext), decipher.final()]);
    }
  } catch (error) {
    console.error("❌ Error durante la desencriptación:", error);
    throw error;
  }
}

app.post(
    '/upload-video-location',
    upload.fields([
      { name: 'video', maxCount: 1 },
      { name: 'location', maxCount: 1 }
    ]),
    async (req, res) => {
      let videoTemp, locationTemp;
      try {
        console.log('📥 Recibiendo archivos...');
  
        // --- Desencriptar el video ---
        const encryptedVideoBuffer = req.files.video[0].buffer;
        let decryptedVideoBuffer;
        try {
          decryptedVideoBuffer = decryptFileFlexible(encryptedVideoBuffer);
          console.log('✅ Video desencriptado correctamente.');
        } catch (err) {
          throw new Error("La desencriptación del video falló. Verifica que la encriptación se realizó correctamente en el cliente.");
        }
  
        // Guardar el video desencriptado temporalmente
        videoTemp = tmp.fileSync({ postfix: '.mp4' });
        fs.writeFileSync(videoTemp.name, decryptedVideoBuffer);
        console.log(`✅ Video guardado temporalmente en: ${videoTemp.name}`);
  
        // --- Desencriptar la localización ---
        const encryptedLocationBuffer = req.files.location[0].buffer;
        let decryptedLocationBuffer;
        try {
          decryptedLocationBuffer = decryptFileFlexible(encryptedLocationBuffer);
          console.log('✅ Ubicación desencriptada correctamente.');
        } catch (err) {
          throw new Error("La desencriptación de la ubicación falló. Verifica que la encriptación se realizó correctamente en el cliente.");
        }
  
        // Guardar la ubicación desencriptada temporalmente en un archivo de texto
        locationTemp = tmp.fileSync({ postfix: '.txt' });
        fs.writeFileSync(locationTemp.name, decryptedLocationBuffer);
        console.log(`✅ Ubicación guardada temporalmente en: ${locationTemp.name}`);
  
        const folderName = uuidv4();
        console.log(`📂 Creando carpeta en GCS: ${folderName}`);
  
        // Subir ambos archivos a Google Cloud Storage
        const url = await uploadFilesToGCS(videoTemp.name, locationTemp.name, folderName);
        console.log(`🎉 Subida completa: ${url}`);
  
        res.send({
          message: 'Files uploaded successfully',
          folderUrl: url,
          locationData: decryptedLocationBuffer.toString('utf-8')
        });
      } catch (error) {
        console.error('❌ Error en la subida:', error);
        res.status(500).send({ error: error.message });
      } finally {
        if (videoTemp && fs.existsSync(videoTemp.name)) {
          fs.unlinkSync(videoTemp.name);
          console.log('🗑️ Archivo de video temporal eliminado.');
        }
        if (locationTemp && fs.existsSync(locationTemp.name)) {
          fs.unlinkSync(locationTemp.name);
          console.log('🗑️ Archivo de ubicación temporal eliminado.');
        }
      }
    }
  );
  

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
              metadata: {
                contentType: 'video/mp4'
              }
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
              metadata: {
                contentType: 'text/plain'
              }
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
