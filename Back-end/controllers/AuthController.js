// src/controllers/AuthController.js
const authService = require('../services/AuthService');

class AuthController {
  async register(req, res, next) {
    try {
      console.log('→ POST /auth/register', req.body);
      const tokens = await authService.register(req.body);
      console.log('   Registro OK, tokens:', tokens);
      res.json(tokens);
    } catch (err) {
      console.error('   Error en register:', err.message);
      res.status(err.status || 400).json({ error: err.message });
    }
  }

  async login(req, res, next) {
    try {
      const tokens = await authService.login(req.body);
      res.json(tokens);
    } catch (err) {
      res.status(err.status || 400).json({ error: err.message });
    }
  }

  async refresh(req, res, next) {
    try {
      const { refreshToken } = req.body;
      const tokens = await authService.refresh(refreshToken);
      res.json(tokens);
    } catch (err) {
      res.status(err.status || 401).json({ error: err.message });
    }
  }
}

module.exports = new AuthController();
