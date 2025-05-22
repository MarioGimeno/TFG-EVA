// src/routes/tokens.js
const express            = require('express');
const router             = express.Router();
const authenticate       = require('../middleware/authMiddleware');
const TokensController   = require('../controllers/tokenscontroller');

// Debug
router.use((req, res, next) => {
  console.log('🛎️ Tokens router:', req.method, req.originalUrl);
  next();
});

// POST /api/tokens — guardar o actualizar token FCM
router.post(
  '/',
  authenticate,
  (req, res, next) => TokensController.create(req, res, next)
);

module.exports = router;
