// services/decryptionService.js
const { Worker } = require('worker_threads');
const path = require('path');

function decryptFile(inputPath, outputPath) {
  return new Promise((resolve, reject) => {
    const workerScript = path.join(__dirname, 'decryptWorker.js');  // <— ruta correcta
    const worker = new Worker(workerScript, {
      workerData: { inputFilePath: inputPath, outputFilePath: outputPath }
    });
    worker.on('message', msg => msg.success ? resolve(outputPath) : reject(new Error(msg.error)));
    worker.on('error', reject);
    worker.on('exit', code => code === 0 ? null : reject(new Error(`Worker stopped with ${code}`)));
  });
}

module.exports = { decryptFile };
