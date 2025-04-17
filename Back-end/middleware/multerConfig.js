// src/middleware/multerConfig.js
const multer = require('multer');
const fs    = require('fs');
const { TMPDIR } = require('../config');

if (!fs.existsSync(TMPDIR)) {
  fs.mkdirSync(TMPDIR, { recursive: true });
}

const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, TMPDIR),
  filename:    (req, file, cb) => cb(null, `${Date.now()}-${file.originalname}`)
});

module.exports = multer({ storage });
