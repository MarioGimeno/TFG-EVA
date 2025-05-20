// src/controllers/VideosController.js
const gcsService = require('../services/GcsService');

class VideosController {
  /**
   * GET /api/videos
   */
  async list(req, res, next) {
    try {
      const videos = await gcsService.listUserFiles(req.userId);
      res.json({ videos });
    } catch (err) {
      next(err);
    }
  }
}

module.exports = new VideosController();
