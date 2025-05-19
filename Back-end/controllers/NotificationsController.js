// src/controllers/NotificationsController.js
const service = require('../services/NotificationsService');

class NotificationsController {
  /**
   * Handler para POST /api/notifications/location
   */
  async sendLocation(req, res, next) {
    try {
      const userId       = req.user?.id ?? req.userId;
      const { latitude, longitude, recipientIds } = req.body;

      // Validaciones básicas
      if (typeof latitude !== 'number' || typeof longitude !== 'number') {
        return res.status(400).json({ error: 'Faltan latitude o longitude válidos' });
      }
      if (!Array.isArray(recipientIds) || recipientIds.some(id => typeof id !== 'number')) {
        return res.status(400).json({ error: 'Faltan recipientIds válidos' });
      }

      const results = await service.sendLocationNotifications(
        userId, latitude, longitude, recipientIds
      );
      res.json({ results });
    } catch (err) {
      // si el servicio lanzó con err.status, úsalo; si no, 500
      const status = err.status || 500;
      res.status(status).json({ error: err.message });
    }
  }
}

module.exports = new NotificationsController();
