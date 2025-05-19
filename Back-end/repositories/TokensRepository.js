// src/repositories/TokensRepository.js
const { pool } = require('../config/Pool');

class TokensRepository {
  /**
   * Inserta o actualiza (upsert) el token FCM para un usuario.
   */
  async upsertToken(userId, token) {
    await pool.query(
      `INSERT INTO fcm_tokens(user_id, token)
       VALUES ($1, $2)
       ON CONFLICT (user_id)
       DO UPDATE SET token = EXCLUDED.token, updated_at = now()`,
      [userId, token]
    );
  }
}

module.exports = new TokensRepository();
