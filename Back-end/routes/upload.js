// src/routes/upload.js
const express          = require('express');
const router           = express.Router();
const uploadMiddleware = require('../middleware/multerConfig');
const auth             = require('../middleware/authMiddleware');
const UploadController = require('../controllers/uploadcontroller');

// POST /api/upload/upload-chunk
router.post(
  '/upload-chunk',
  auth,
  uploadMiddleware.single('chunkData'),
  (req, res, next) => UploadController.uploadChunk(req, res, next)
);

module.exports = router;
