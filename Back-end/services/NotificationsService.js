// src/services/NotificationsService.js
const admin = require('firebase-admin');
const repo  = require('../repositories/NotificationsRepository');

class NotificationsService {
  /**
   * Envía notificaciones de ubicación a los recipientIds
   * que sean contactos del usuario.
   * Devuelve un array de resultados { token, success, messageId?, error? }.
   */
  async sendLocationNotifications(userId, latitude, longitude, recipientIds) {
    // 1) Obtener los contactos del usuario
    const contactIds = await repo.getContactUserIds(userId);
    if (!contactIds.length) {
      const err = new Error('No tienes contactos registrados');
      err.status = 404;
      throw err;
    }

    // 2) Intersectar con los recipientIds provistos
    const targets = contactIds.filter(id => recipientIds.includes(id));
    if (!targets.length) {
      const err = new Error('Ninguno de los recipientIds es un contacto tuyo');
      err.status = 404;
      throw err;
    }

    // 3) Obtener tokens FCM
    const tokens = await repo.getTokensByUserIds(targets);
    if (!tokens.length) {
      const err = new Error('No hay tokens para estos contactos');
      err.status = 404;
      throw err;
    }

    // 4) Construir y enviar mensajes
    const messages = tokens.map(token => ({
      token,
      data: {
        type:      'LIVE_LOCATION',
        latitude:  latitude.toString(),
        longitude: longitude.toString()
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

    return results;
  }
}

module.exports = new NotificationsService();
