const express = require('express');
const router  = express.Router();
const { register, login } = require('../services/authService');

router.post('/register', async (req, res) => {
  try {
    const tokens = await register(req.body);
    res.json(tokens);
  } catch (e) {
    res.status(400).json({ error: e.message });
  }
});

router.post('/login', async (req, res) => {
  try {
    const tokens = await login(req.body);
    res.json(tokens);
  } catch (e) {
    res.status(400).json({ error: e.message });
  }
});
router.post('/refresh', async (req, res) => {
    try {
      const { refreshToken } = req.body;
      const payload = verifyToken(refreshToken);
      if (!payload) throw new Error('Refresh inválido');
      const tokens = _generateTokens(payload.sub);
      res.json(tokens);
    } catch (e) {
      res.status(401).json({ error: e.message });
    }
  });
  
module.exports = router;
