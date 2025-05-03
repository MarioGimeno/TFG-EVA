// routes/tokens.js
const express = require('express');
const router = express.Router();
const { pool } = require('../config');
const authenticate = require('../middleware/authMiddleware');

// Debug
router.use((req, res, next) => {
  console.log('🛎️ Tokens router:', req.method, req.originalUrl);
  next();
});

/**
 * POST /api/tokens
 * Body: { token: string }
 */
router.post(
  '/',
  authenticate,
  async (req, res) => {
    const userId = req.userId;
    const { token } = req.body;
    if (!token) return res.status(400).json({ error: 'Falta token en el body' });

    try {
      // INSERT o UPDATE (upsert) para no duplicar
      await pool.query(
        `INSERT INTO fcm_tokens(user_id, token)
         VALUES($1, $2)
         ON CONFLICT (user_id) 
         DO UPDATE SET token = EXCLUDED.token, updated_at = now()`,
        [userId, token]
      );
      console.log(`✅ Token guardado para user ${userId}`);
      return res.sendStatus(204);
    } catch (err) {
      console.error('Error guardando token:', err);
      return res.status(500).json({ error: 'Error interno guardando token' });
    }
  }
);

module.exports = router;
