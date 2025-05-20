// src/routes/notifications.js
const express                 = require('express');
const router                  = express.Router();
const authenticate            = require('../middleware/authMiddleware');
const NotificationsController = require('../controllers/NotificationsController');

// Log de depuración
router.use((req, res, next) => {
  console.log('🛎️ Notifications router:', req.method, req.originalUrl);
  next();
});

// POST /api/notifications/location
router.post(
  '/location',
  authenticate,
  (req, res, next) => NotificationsController.sendLocation(req, res, next)
);

module.exports = router;
