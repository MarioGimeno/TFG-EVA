// src/routes/auth.js
const express        = require('express');
const router         = express.Router();
const AuthController = require('../controllers/authcontroller');

// POST /api/auth/register
router.post('/register', (req, res, next) =>
  AuthController.register(req, res, next)
);

// POST /api/auth/login
router.post('/login', (req, res, next) =>
  AuthController.login(req, res, next)
);

// POST /api/auth/refresh
router.post('/refresh', (req, res, next) =>
  AuthController.refresh(req, res, next)
);

module.exports = router;
