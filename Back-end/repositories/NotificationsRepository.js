// src/repositories/NotificationsRepository.js
const { pool } = require('../config/Pool');

class NotificationsRepository {
  /**
   * Devuelve los contact_user_id de los contactos de un usuario.
   */
  async getContactUserIds(userId) {
    const { rows } = await pool.query(
      `SELECT contact_user_id
         FROM contacts
        WHERE user_id = $1`,
      [userId]
    );
    return rows.map(r => r.contact_user_id);
  }

  /**
   * Devuelve todos los tokens FCM para una lista de userIds.
   */
  async getTokensByUserIds(userIds) {
    if (!userIds.length) return [];
    const { rows } = await pool.query(
      `SELECT token
         FROM fcm_tokens
        WHERE user_id = ANY($1::int[])`,
      [userIds]
    );
    return rows.map(r => r.token).filter(Boolean);
  }
}

module.exports = new NotificationsRepository();
