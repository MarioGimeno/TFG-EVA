// src/routes/videos.js
const express           = require('express');
const router            = express.Router();
const auth              = require('../middleware/authMiddleware');
const VideosController  = require('../controllers/videoscontroller');

// GET /api/videos — requiere token
router.get(
  '/videos',
  auth,
  (req, res, next) => VideosController.list(req, res, next)
);

module.exports = router;
