require('dotenv').config({ path: './keys.env' });
const express = require('express');
const multer = require('multer');
const ffmpeg = require('fluent-ffmpeg');
const { v4: uuidv4 } = require('uuid');
const { Storage } = require('@google-cloud/storage');
const cors = require('cors');
const tmp = require('tmp'); // Necesitas instalar el paquete tmp
const fs = require('fs');
const path = require('path');

const app = express();
app.use(cors());
app.use(express.json());

// Configuración de Multer para almacenamiento en memoria
const storage = multer.memoryStorage();
const upload = multer({ storage: storage });

const fields = [
    { name: 'video', maxCount: 1 },
    { name: 'audio', maxCount: 1 },
    { name: 'location', maxCount: 1 }
];
ffmpeg.setFfmpegPath(path.join(__dirname, 'ffmpeg/bin/ffmpeg.exe'));
//En EC2 ubuntu tendra que ser ffmpeg.setFfmpegPath('/usr/bin/ffmpeg');
    
const PORT = process.env.PORT || 3000;

// Configurar Google Cloud Storage
const gcs = new Storage({
    keyFilename: process.env.GOOGLE_APPLICATION_CREDENTIALS
});
const bucketName = process.env.BUCKET_NAME;
app.get('/saludar', (req, res) => {
    res.send('Hola, mundo!');
});
app.post('/upload', upload.fields(fields), (req, res) => {

    const videoBuffer = req.files.video[0].buffer;
    const audioBuffer = req.files.audio[0].buffer;
    const locationData = req.files.location[0].buffer.toString('utf-8');
    console.log('Hola desde upload', videoBuffer, audioBuffer);

    // Crea un archivo temporal para el texto de la ubicación
    const locationTemp = tmp.fileSync({ postfix: '.txt' });
    fs.writeFileSync(locationTemp.name, locationData);

    mergeAudioAndVideo(videoBuffer, audioBuffer)
        .then((outputFilePath) => {
            const folderName = uuidv4(); // Nombre único de la carpeta
            return uploadFilesToGCS(outputFilePath, locationTemp.name, folderName);
        })
        .then(url => {
            res.send({
                message: 'Files combined and uploaded successfully',
                folderUrl: url,
                locationData: locationData
            });
        })
        .catch(error => {
            console.error('Error combining or uploading files:', error);
            res.status(500).send({ error: 'Failed to combine or upload files' });
        });
});

function uploadFilesToGCS(videoFilePath, textFilePath, folderName) {
    const bucket = gcs.bucket(bucketName);

    return Promise.all([
        // Subir el archivo de video
        new Promise((resolve, reject) => {
            fs.createReadStream(videoFilePath)
                .pipe(bucket.file(`${folderName}/video.mp4`).createWriteStream({
                    metadata: {
                        contentType: 'video/mp4'
                    }
                }))
                .on('finish', resolve)
                .on('error', (err) => {
                    fs.unlinkSync(videoFilePath);
                    reject(err);
                });
        }),
        // Subir el archivo de texto
        new Promise((resolve, reject) => {
            fs.createReadStream(textFilePath)
                .pipe(bucket.file(`${folderName}/location.txt`).createWriteStream({
                    metadata: {
                        contentType: 'text/plain'
                    }
                }))
                .on('finish', resolve)
                .on('error', (err) => {
                    fs.unlinkSync(textFilePath);
                    reject(err);
                });
        })
    ]).then(() => {
        // Limpieza de archivos temporales
        fs.unlinkSync(videoFilePath);
        fs.unlinkSync(textFilePath);
        console.log('Upload complete');
        return `https://storage.googleapis.com/${bucketName}/${folderName}/`;
    });
}

function mergeAudioAndVideo(videoBuffer, audioBuffer) {
    return new Promise((resolve, reject) => {
        const videoTemp = tmp.fileSync({ postfix: '.mp4' });
        const audioTemp = tmp.fileSync({ postfix: '.mp3' });
        const outputTemp = tmp.fileSync({ postfix: '.mp4'}); // Cambia la carpeta de salida

        fs.writeFileSync(videoTemp.name, videoBuffer);
        fs.writeFileSync(audioTemp.name, audioBuffer);

        console.log(`Video temp path: ${videoTemp.name}`);
        console.log(`Audio temp path: ${audioTemp.name}`);
        console.log(`Output temp path: ${outputTemp.name}`);

        const ffmpegProcess = ffmpeg(videoTemp.name)
            .input(audioTemp.name)
            .outputOptions(['-c:v libx264', '-c:a aac', '-strict experimental', '-vf transpose=1'])
            .on('stderr', (stderrLine) => {
                console.log('FFmpeg stderr:', stderrLine);
            })
            .on('error', (err) => {
                console.error('ffmpeg error:', err);
                videoTemp.removeCallback();
                audioTemp.removeCallback();
                outputTemp.removeCallback();
                reject(err);
            })
            .on('end', () => {
                console.log('Merging complete');
                videoTemp.removeCallback();
                audioTemp.removeCallback();
                resolve(outputTemp.name); // Resuelve con la ruta del archivo de salida
            })
            .save(outputTemp.name); // Guarda el archivo de salida en disco
    });
}

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
