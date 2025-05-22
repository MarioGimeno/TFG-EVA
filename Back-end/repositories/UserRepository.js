// src/repositories/UserRepository.js
const { pool } = require('../config/Pool');

class UserRepository {
  /**
   * Busca un usuario por email. Asume que la columna de contraseña
   * en la tabla es `password`, no `password_hash`.
   */
  async findByEmail(email) {
    const { rows } = await pool.query(
      `SELECT id, email, password, nombre
         FROM users
        WHERE email = $1`,
      [email]
    );
    if (rows.length === 0) return null;
    // Renombramos para mantener getPasswordHash() en el modelo
    return {
      id: rows[0].id,
      email: rows[0].email,
      passwordHash: rows[0].password,
      fullname: rows[0].nombre
    };
  }

  /**
   * Inserta un nuevo usuario usando la columna `password`.
   * Devuelve el nuevo id y email.
   */
  async createUser({ fullName, email, passwordHash }) {
    const { rows } = await pool.query(
        `INSERT INTO users ( email, password, nombre)
         VALUES ($1, $2, $3)
         RETURNING id, nombre, email`,
        [email, passwordHash, fullName]
      );
      return { id: rows[0].id, fullName: rows[0].fullName, email: rows[0].email };
  }
}

module.exports = new UserRepository();
