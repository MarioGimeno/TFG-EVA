// Carga las variables de entorno desde el archivo "keys.env"
require('dotenv').config({ path: './keys.env' });

// Importa los módulos necesarios
const express = require('express');                         // Framework para crear el servidor web
const multer = require('multer');                           // Middleware para manejo de multipart/form-data (subida de archivos)
const { v4: uuidv4 } = require('uuid');                     // Generador de identificadores únicos (UUID)
const { Storage } = require('@google-cloud/storage');       // Cliente para Google Cloud Storage (GCS)
const cors = require('cors');                               // Middleware para habilitar CORS
const fs = require('fs');                                   // Módulo para manejo de archivos y sistema de archivos
const path = require('path');                               // Módulo para trabajar con rutas de archivos
const { Worker } = require('worker_threads');               // Permite crear hilos (threads) para tareas en segundo plano

// Crea la aplicación Express
const app = express();

// Habilita CORS y el parseo de cuerpos en formato JSON
app.use(cors());
app.use(express.json());

// CONFIGURACIÓN DEL DIRECTORIO TEMPORAL (TMPDIR)
// Se asegura que la variable de entorno TMPDIR tenga un valor; de lo contrario se asigna '/mnt/uploads/tmp'
process.env.TMPDIR = process.env.TMPDIR || '/mnt/uploads/tmp';
// Si el directorio TMPDIR no existe, se crea (de forma recursiva, para crear directorios padre si es necesario)
if (!fs.existsSync(process.env.TMPDIR)) {
  fs.mkdirSync(process.env.TMPDIR, { recursive: true });
}
console.log('TMPDIR:', process.env.TMPDIR);

// CONFIGURACIÓN DE MULTER
// Se configura multer para guardar los archivos subidos en el directorio TMPDIR
const storage = multer.diskStorage({
  // Función para definir la carpeta de destino de cada archivo subido
  destination: function (req, file, cb) {
    cb(null, process.env.TMPDIR);
  },
  // Función para definir el nombre de cada archivo subido
  filename: function (req, file, cb) {
    // Se utiliza la fecha actual (en milisegundos) y el nombre original del archivo para crear un nombre único
    cb(null, Date.now() + '-' + file.originalname);
  }
});
const upload = multer({ storage: storage });

// Se define el puerto en el que se ejecutará el servidor, ya sea desde la variable de entorno PORT o el puerto 3000 por defecto
const PORT = process.env.PORT || 3000;

// Se crea una instancia del cliente de Google Cloud Storage utilizando el archivo de credenciales (definido en GOOGLE_APPLICATION_CREDENTIALS)
const gcs = new Storage({ keyFilename: process.env.GOOGLE_APPLICATION_CREDENTIALS });
// Se obtiene el nombre del bucket (contenedor) de GCS desde la variable de entorno BUCKET_NAME
const bucketName = process.env.BUCKET_NAME;


/**
 * Ejecuta un Worker Thread para desencriptar un archivo.
 * Se le pasan dos rutas:
 *   - inputFilePath: ruta del archivo encriptado
 *   - outputFilePath: ruta donde se guardará el archivo desencriptado
 * Además, se envían otros parámetros de encriptación (SECRET_KEY, IV_SIZE, TAG_SIZE) a través de workerData.
 */
function runDecryptionWorker(inputFilePath, outputFilePath) {
  return new Promise((resolve, reject) => {
    // Crea un nuevo Worker que ejecuta el script 'decryptWorker.js'
    const worker = new Worker('./decryptWorker.js', {
      workerData: { inputFilePath, outputFilePath, SECRET_KEY, IV_SIZE, TAG_SIZE }
    });
    // Escucha el mensaje del worker; si se indica éxito, resuelve la promesa con la ruta de salida
    worker.on('message', (message) => {
      if (message.success) {
        resolve(outputFilePath);
      } else {
        // Si el worker indica error, rechaza la promesa con el mensaje de error
        reject(new Error(message.error));
      }
    });
    // Escucha errores emitidos por el worker
    worker.on('error', reject);
    // Si el worker finaliza con un código distinto de 0, se rechaza la promesa
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
 *
 * @param {string} fileId - Identificador único del archivo.
 * @param {number} totalChunks - Número total de chunks esperados.
 */
function assembleFile(fileId, totalChunks) {
  // Se define el directorio donde se almacenan los chunks, basado en el fileId
  const fileDir = path.join('uploads', fileId);
  // Ruta para el archivo encriptado ensamblado
  const encryptedFilePath = path.join('uploads', `${fileId}-encrypted.mp4`);
  // Ruta para el archivo desencriptado (resultado final)
  const decryptedFilePath = path.join('uploads', `${fileId}-decrypted.mp4`);
  // Se crea un WriteStream para escribir el archivo encriptado ensamblado
  const writeStream = fs.createWriteStream(encryptedFilePath);
  
  let currentChunk = 0;  // Contador para llevar el seguimiento de los chunks procesados
  
  // Función recursiva que va leyendo y anexando cada chunk al archivo ensamblado
  function appendNextChunk() {
    // Si se han procesado todos los chunks...
    if (currentChunk >= totalChunks) {
      // Finaliza el WriteStream
      writeStream.end();
      console.log('Archivo ensamblado correctamente:', encryptedFilePath);
      
      // Ejecuta el worker para desencriptar el archivo ensamblado
      runDecryptionWorker(encryptedFilePath, decryptedFilePath)
        .then(() => {
          console.log('Archivo desencriptado correctamente:', decryptedFilePath);
          // Una vez desencriptado, se sube el video (y la ubicación, si existe) a Google Cloud Storage
          uploadVideoAndLocationToGCS(decryptedFilePath, fileId)
            .then((url) => {
              console.log('Video y ubicación subidos correctamente:', url);
              // (Opcional) Se limpian los archivos temporales
              cleanupFiles(fileDir, encryptedFilePath, decryptedFilePath);
            })
            .catch(err => console.error('Error subiendo video/ubicación a GCS:', err));
        })
        .catch(err => console.error('Error al desencriptar el archivo:', err));
      return;
    }
    
    // Ruta del chunk actual
    const chunkPath = path.join(fileDir, `chunk_${currentChunk}`);
    // Se crea un ReadStream para el chunk actual
    const readStream = fs.createReadStream(chunkPath);
    
    // Se "pipea" (envía) el contenido del chunk al WriteStream sin finalizarlo (end:false)
    readStream.pipe(writeStream, { end: false });
    // Cuando se termina de leer el chunk...
    readStream.on('end', () => {
      console.log(`Chunk ${currentChunk} agregado.`);
      currentChunk++;      // Se incrementa el contador
      appendNextChunk();   // Se llama recursivamente para procesar el siguiente chunk
    });
    // En caso de error al leer el chunk, se imprime el error y se cierra el WriteStream
    readStream.on('error', (err) => {
      console.error('Error al leer chunk:', err);
      writeStream.close();
    });
  }
  
  // Inicia la unión de los chunks
  appendNextChunk();
}


/**
 * Función para subir el video desencriptado a Google Cloud Storage (GCS).
 * Si existe un archivo de ubicación encriptado, se desencripta y se sube junto con el video.
 *
 * @param {string} videoFilePath - Ruta del archivo de video desencriptado.
 * @param {string} fileId - Identificador único del archivo (utilizado para organizar en el bucket).
 * @returns {Promise<string>} - Promesa que resuelve con la URL base del archivo subido.
 */
function uploadVideoAndLocationToGCS(videoFilePath, fileId) {
  return new Promise((resolve, reject) => {
    // Se obtiene el bucket de GCS a partir del nombre definido
    const bucket = gcs.bucket(bucketName);
    // Se define la ruta de destino para el video dentro del bucket
    const videoDestination = `${fileId}/video.mp4`;
    // Se define la ruta de destino para la ubicación (desencriptada) dentro del bucket
    const locationDestination = `${fileId}/location.txt`;
    console.log(`Iniciando subida del video desencriptado: ${videoFilePath} a ${videoDestination}`);
    
    // Primero, se sube el video desencriptado
    fs.createReadStream(videoFilePath)
      .pipe(bucket.file(videoDestination).createWriteStream({
        metadata: { contentType: 'video/mp4' },
        resumable: true  // Permite la subida en partes si es necesario
      }))
      .on('finish', () => {
        console.log('Video subido correctamente.');
        // Se busca el archivo de ubicación encriptado en el directorio "uploads/<fileId>/location.txt"
        const encryptedLocationPath = path.join('uploads', fileId, 'location.txt');
        if (fs.existsSync(encryptedLocationPath)) {
          console.log(`Ubicación encriptada encontrada en ${encryptedLocationPath}. Se procederá a desencriptarla.`);
          // Se define la ruta para el archivo de ubicación desencriptado
          const decryptedLocationPath = path.join('uploads', `${fileId}-decrypted-location.txt`);
          // Se ejecuta el Worker para desencriptar el archivo de ubicación
          runDecryptionWorker(encryptedLocationPath, decryptedLocationPath)
            .then(() => {
              console.log(`Ubicación desencriptada correctamente: ${decryptedLocationPath}`);
              // Se sube el archivo de ubicación desencriptado a GCS
              fs.createReadStream(decryptedLocationPath)
                .pipe(bucket.file(locationDestination).createWriteStream({
                  metadata: { contentType: 'text/plain' },
                  resumable: true
                }))
                .on('finish', () => {
                  // Se construye la URL base para acceder a los archivos subidos en el bucket
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
          // Si no existe el archivo de ubicación, se resuelve la promesa solo con la URL del video
          console.log('No se encontró archivo de ubicación. Se subirá solo el video.');
          const videoUrl = `https://storage.googleapis.com/${bucketName}/${videoDestination}`;
          resolve(videoUrl);
        }
      })
      .on('error', reject);
  });
}


/**
 * Función para limpiar archivos temporales creados durante el proceso.
 *
 * @param {string} chunksDir - Directorio que contiene los chunks.
 * @param {string} encryptedFilePath - Ruta del archivo encriptado ensamblado.
 * @param {string} decryptedFilePath - Ruta del archivo desencriptado.
 */
function cleanupFiles(chunksDir, encryptedFilePath, decryptedFilePath) {
  // Eliminar la carpeta de chunks y todos sus archivos
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
 * Endpoint para recibir cada chunk (parte) del archivo.
 * Se espera que el cliente envíe, en el body, los siguientes datos:
 *   - fileId: identificador único del archivo.
 *   - chunkIndex: índice del chunk (en formato String).
 *   - totalChunks: número total de chunks esperados para el video.
 * Además, en el campo 'chunkData' se envía el archivo correspondiente.
 */
app.post('/upload-chunk', upload.single('chunkData'), (req, res) => {
  try {
    // Se extraen los metadatos enviados en el body de la petición
    const fileId = req.body.fileId;
    const chunkIndexStr = req.body.chunkIndex; // Se recibe como string
    const totalChunks = req.body.totalChunks;   // Número total de chunks para el video

    // Se define el directorio donde se almacenarán los chunks para este fileId
    const fileDir = path.join('uploads', fileId);
    if (!fs.existsSync(fileDir)) {
      fs.mkdirSync(fileDir, { recursive: true });
    }
    
    // Caso especial: Si chunkIndex es "-1", se trata del archivo de ubicación
    if (chunkIndexStr === "-1") {
      // Se define la ruta del archivo de ubicación y se mueve (renombra) el archivo subido a esa ruta
      const locationFilename = path.join(fileDir, "location.txt");
      fs.renameSync(req.file.path, locationFilename);
      console.log(`Ubicación recibida para fileId ${fileId}. Guardada en ${locationFilename}`);
    } else {
      // Caso normal: se trata de un chunk del video
      const chunkFilename = path.join(fileDir, `chunk_${chunkIndexStr}`);
      fs.renameSync(req.file.path, chunkFilename);
      console.log(`Chunk ${chunkIndexStr} del archivo ${fileId} recibido.`);
    }
    
    // Se verifica si ya se han recibido todos los chunks (solo se cuentan los archivos cuyo nombre comienza con "chunk_")
    const receivedChunks = fs.readdirSync(fileDir).filter(name => name.startsWith('chunk_')).length;
    if (parseInt(totalChunks) === receivedChunks) {
      console.log('Todos los chunks del video recibidos. Iniciando el ensamblado.');
      // Se inicia el proceso para unir los chunks, desencriptar y subir a GCS
      assembleFile(fileId, totalChunks);
    }
    
    // Se responde al cliente indicando que el chunk ha sido recibido correctamente
    res.status(200).send({ message: 'Chunk recibido' });
  } catch (error) {
    console.error('Error en /upload-chunk:', error);
    res.status(500).send({ error: error.message });
  }
});

// Inicia el servidor Express en el puerto definido y muestra un mensaje en la consola
app.listen(PORT, () => {
  console.log(`🚀 Servidor corriendo en el puerto ${PORT}`);
});
