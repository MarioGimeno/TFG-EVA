// src/routes/files.js
const express            = require('express');
const multer             = require('multer');
const authenticate       = require('../middleware/authMiddleware');
const FilesController    = require('../controllers/FilesController');

const router = express.Router();
const upload = multer({
  storage: multer.memoryStorage(),
  limits:  { fileSize: 50 * 1024 * 1024 }
});

// GET /api/files — público bajo token
router.get('/', authenticate, (req, res, next) =>
  FilesController.list(req, res, next)
);

// POST /api/files — subir archivo
router.post(
  '/',
  authenticate,
  upload.single('file'),
  (req, res, next) => FilesController.upload(req, res, next)
);

module.exports = router;
