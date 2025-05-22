// src/controllers/TokensController.js
const service = require('../services/tokensService');

class TokensController {
  /**
   * Handler para POST /api/tokens
   */
  async create(req, res, next) {
    try {
      const userId = req.userId;
      const { token } = req.body;
      await service.saveToken(userId, token);
      console.log(`✅ Token guardado para user ${userId}`);
      res.sendStatus(204);
    } catch (err) {
      console.error('Error guardando token:', err.message);
      res.status(err.status || 500).json({ error: err.message });
    }
  }
}

module.exports = new TokensController();
