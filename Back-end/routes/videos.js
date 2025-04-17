const express = require('express');
const auth    = require('../middleware/authMiddleware');
const { listUserFiles } = require('../services/gcsService');
const router  = express.Router();

router.get('/videos', auth, async (req, res) => {
  try {
    const urls = await listUserFiles(req.userId);
    res.json({ videos: urls });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

module.exports = router;
