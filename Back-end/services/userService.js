// src/services/userService.js
const db = require('./db');

/**
 * Crea un usuario en la tabla `users`.
 * @param {{ email: string, passwordHash: string }} data
 * @returns {Promise<{ id: number, email: string }>}
 */
async function createUser({ email, passwordHash }) {
  const result = await db.query(
    `INSERT INTO users (email, password)
     VALUES ($1, $2)
     RETURNING id, email`,
    [email, passwordHash]
  );
  return result.rows[0];
}

/**
 * Recupera un usuario por su email.
 * @param {string} email
 * @returns {Promise<{ id: number, email: string, password: string }|null>}
 */
async function findUserByEmail(email) {
  const result = await db.query(
    `SELECT id, email, password
     FROM users
     WHERE email = $1`,
    [email]
  );
  return result.rows[0] || null;
}

module.exports = { createUser, findUserByEmail };
