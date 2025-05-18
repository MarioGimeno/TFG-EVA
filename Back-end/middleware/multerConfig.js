const multer  = require('multer');
const path    = require('path');
const { TMPDIR } = require('../config');

// Storage que escriba directamente en TMPDIR
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, TMPDIR);
  },
  filename: (req, file, cb) => {
    // pon un nombre único para evitar colisiones
    const unique = Date.now() + '-' + Math.round(Math.random()*1e9);
    cb(null, `${unique}-${file.fieldname}`);
  }
});

module.exports = multer({ storage });
