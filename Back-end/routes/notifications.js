// routes/notifications.js

const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const authenticate = require('../middleware/authMiddleware');
const { pool } = require('../config');



// Log de depuración
router.use((req, res, next) => {
  console.log('🛎️ Notifications router:', req.method, req.originalUrl);
  next();
});

/**
 * POST /api/notifications/location
 * Body:
 * {
 *   "latitude": number,
 *   "longitude": number,
 *   "recipientIds": [1,2,3]
 * }
 */
router.post('/location', authenticate, async (req, res) => {
    const userId = req.userId;
    const { latitude, longitude } = req.body;
  
    if (typeof latitude !== 'number' || typeof longitude !== 'number') {
      return res.status(400).json({ error: 'Faltan latitude o longitude válidos' });
    }
  
    const client = await pool.connect();
    try {
      // 1) Obtener los IDs de usuario de tus contactos
      const contactsRes = await client.query(
        `SELECT contact_user_id
           FROM contacts
          WHERE user_id = $1`,
        [userId]
      );
      const contactIds = contactsRes.rows.map(r => r.contact_user_id);
      if (contactIds.length === 0) {
        return res.status(404).json({ error: 'No tienes contactos registrados' });
      }
  
      // 2) Coger solo los tokens de esos contactos
      const tokensRes = await client.query(
        `SELECT token
           FROM fcm_tokens
          WHERE user_id = ANY($1::int[])`,
        [contactIds]
      );
      const tokens = tokensRes.rows.map(r => r.token).filter(Boolean);
      if (tokens.length === 0) {
        return res.status(404).json({ error: 'No hay tokens para tus contactos' });
      }
  
      // 3) Armar mensajes FCM data-only + notificación
      const messages = tokens.map(t => ({
        token: t,
        data: {
          type: 'LIVE_LOCATION',
          lat:  latitude.toString(),
          lon:  longitude.toString()
        },
        notification: {
          title: 'Ubicación en vivo',
          body:  `Lat: ${latitude.toFixed(5)}, Lon: ${longitude.toFixed(5)}`
        }
      }));

    // 3) Envía cada mensaje por separado y recoge resultados
    const results = await Promise.all(
      messages.map(msg =>
        admin.messaging().send(msg)
          .then(messageId => ({ token: msg.token, success: true, messageId }))
          .catch(error => ({ token: msg.token, success: false, error: error.message }))
      )
    );

    // 4) Responde con el resumen de envíos
    return res.json({ results });
  } catch (err) {
    console.error('Error enviando notificaciones FCM:', err);
    return res.status(500).json({ error: 'Error interno enviando notificaciones' });
  }
});

module.exports = router;
