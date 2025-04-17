const { Worker } = require('worker_threads');
const { IV_SIZE, TAG_SIZE, SECRET_KEY } = require('../config');

function decryptFile(input, output) {
  return new Promise((resolve, reject) => {
    const worker = new Worker(
      require.resolve('../workers/decryptWorker.js'),
      { workerData: { input, output, SECRET_KEY, IV_SIZE, TAG_SIZE } }
    );
    worker.on('message', m => m.success ? resolve(output) : reject(m.error));
    worker.on('error', reject);
    worker.on('exit', code => code === 0 ? null : reject(`exit ${code}`));
  });
}

module.exports = { decryptFile };
