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
  // 0) Obtener userId (depende de tu middleware)
  const userId = req.user?.id ?? req.userId;
  console.log('User ID from token:', userId);

  // 1) Validar body
  const { latitude, longitude, recipientIds } = req.body;
  console.log('Request body:', req.body);
  if (typeof latitude !== 'number' || typeof longitude !== 'number') {
    return res.status(400).json({ error: 'Faltan latitude o longitude válidos' });
  }
  if (!Array.isArray(recipientIds) || recipientIds.some(id => typeof id !== 'number')) {
    return res.status(400).json({ error: 'Faltan recipientIds válidos' });
  }

  const client = await pool.connect();
  try {
    // 2) Obtener lista de contactos del user
    const contactsRes = await client.query(
      `SELECT contact_user_id
         FROM contacts
        WHERE user_id = $1`,
      [userId]
    );
    const contactIds = contactsRes.rows.map(r => r.contact_user_id);
    console.log('Contact rows:', contactsRes.rows);
    console.log('Derived contact IDs:', contactIds);

    if (contactIds.length === 0) {
      return res.status(404).json({ error: 'No tienes contactos registrados' });
    }

    // 3) Intersectar con los recipientIds que pide el cliente
    const targetIds = contactIds.filter(id => recipientIds.includes(id));
    console.log('Filtered recipient IDs (intersection):', targetIds);
    if (targetIds.length === 0) {
      return res.status(404).json({ error: 'Ninguno de los recipientIds es un contacto tuyo' });
    }

    // 4) Obtener tokens de esos contactos filtrados
    const tokensRes = await client.query(
      `SELECT user_id, token
         FROM fcm_tokens
        WHERE user_id = ANY($1::int[])`,
      [targetIds]
    );
    console.log('Token rows:', tokensRes.rows);
    const tokens = tokensRes.rows.map(r => r.token).filter(Boolean);
    console.log('Filtered tokens:', tokens);

    if (tokens.length === 0) {
      return res.status(404).json({ error: 'No hay tokens para estos contactos' });
    }

    // 5) Armar y enviar mensajes FCM uno a uno
    const messages = tokens.map(token => ({
      token,
      data: {
        type: 'LIVE_LOCATION',
        latitude:  latitude.toString(),
        longitude:  longitude.toString()
      },
      notification: {
        title: 'Ubicación en vivo',
        body:  `Lat: ${latitude.toFixed(5)}, Lon: ${longitude.toFixed(5)}`
      }
    }));

    const results = await Promise.all(
      messages.map(msg =>
        admin.messaging().send(msg)
          .then(messageId => ({ token: msg.token, success: true, messageId }))
          .catch(error   => ({ token: msg.token, success: false, error: error.message }))
      )
    );
    console.log('FCM send results:', results);

    // 6) Devolver resumen
    return res.json({ results });

  } catch (err) {
    console.error('Error enviando notificaciones FCM:', err);
    return res.status(500).json({ error: 'Error interno enviando notificaciones' });
  } finally {
    client.release();
  }
});

module.exports = router;
