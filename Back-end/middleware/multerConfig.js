// src/middleware/multerConfig.js

const multer = require('multer');
const path   = require('path');
const { TMPDIR } = require('../config');

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, TMPDIR);
  },
  filename: (req, file, cb) => {
    const unique = Date.now() + '-' + Math.round(Math.random() * 1e9);
    // conserva la extensión original
    const ext = path.extname(file.originalname);
    cb(null, `${unique}-${file.fieldname}${ext}`);
  }
});

module.exports = multer({ storage });
