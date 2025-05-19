// src/repositories/UserRepository.js
const { pool } = require('../config/Pool');

class UserRepository {
  async findByEmail(email) {
    const { rows } = await pool.query(
      `SELECT id, email, password_hash
         FROM users
        WHERE email = $1`,
      [email]
    );
    return rows[0] || null;
  }

  async createUser({ email, passwordHash }) {
    const { rows } = await pool.query(
      `INSERT INTO users (email, password_hash)
       VALUES ($1, $2)
       RETURNING id, email`,
      [email, passwordHash]
    );
    return rows[0];
  }
}

module.exports = new UserRepository();
