// decryptWorker.js
const { parentPort } = require('worker_threads');
const crypto = require('crypto');

const IV_SIZE = 12;    // 12 bytes para GCM
const TAG_SIZE = 16;   // 16 bytes para el tag
const SECRET_KEY = '1234567890123456'; // Debe coincidir con el front
const MAGIC = Buffer.from("CHNK");

function decryptFileFlexible(inputBuffer) {
  if (inputBuffer.slice(0, 4).equals(MAGIC)) {
    console.log("Worker: Formato chunked detectado.");
    let offset = 4;
    const chunkSize = inputBuffer.readInt32BE(offset);
    offset += 4;
    const decryptedChunks = [];
    while (offset < inputBuffer.length) {
      const ivLength = inputBuffer.readInt32BE(offset);
      offset += 4;
      const iv = inputBuffer.slice(offset, offset + ivLength);
      offset += ivLength;
      const encChunkLength = inputBuffer.readInt32BE(offset);
      offset += 4;
      const encryptedChunk = inputBuffer.slice(offset, offset + encChunkLength);
      offset += encChunkLength;
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
    console.log("Worker: Formato streaming detectado.");
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
}

parentPort.on('message', (data) => {
  try {
    const decryptedBuffer = decryptFileFlexible(Buffer.from(data));
    // Transferir el ArrayBuffer subyacente para evitar conversiones costosas
    const ab = decryptedBuffer.buffer.slice(
      decryptedBuffer.byteOffset,
      decryptedBuffer.byteOffset + decryptedBuffer.byteLength
    );
    parentPort.postMessage({ success: true, decryptedBuffer: ab }, [ab]);
  } catch (err) {
    parentPort.postMessage({ success: false, error: err.message });
  }
});
// decryptWorker.js
const { parentPort } = require('worker_threads');
const crypto = require('crypto');

const IV_SIZE = 12;    // 12 bytes para GCM
const TAG_SIZE = 16;   // 16 bytes para el tag
const SECRET_KEY = '1234567890123456'; // Debe coincidir con el front
const MAGIC = Buffer.from("CHNK");

function decryptFileFlexible(inputBuffer) {
  if (inputBuffer.slice(0, 4).equals(MAGIC)) {
    console.log("Worker: Formato chunked detectado.");
    let offset = 4;
    const chunkSize = inputBuffer.readInt32BE(offset);
    offset += 4;
    const decryptedChunks = [];
    while (offset < inputBuffer.length) {
      const ivLength = inputBuffer.readInt32BE(offset);
      offset += 4;
      const iv = inputBuffer.slice(offset, offset + ivLength);
      offset += ivLength;
      const encChunkLength = inputBuffer.readInt32BE(offset);
      offset += 4;
      const encryptedChunk = inputBuffer.slice(offset, offset + encChunkLength);
      offset += encChunkLength;
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
    console.log("Worker: Formato streaming detectado.");
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
}

parentPort.on('message', (data) => {
  try {
    const decryptedBuffer = decryptFileFlexible(Buffer.from(data));
    // Transferir el ArrayBuffer subyacente para evitar conversiones costosas
    const ab = decryptedBuffer.buffer.slice(
      decryptedBuffer.byteOffset,
      decryptedBuffer.byteOffset + decryptedBuffer.byteLength
    );
    parentPort.postMessage({ success: true, decryptedBuffer: ab }, [ab]);
  } catch (err) {
    parentPort.postMessage({ success: false, error: err.message });
  }
});
