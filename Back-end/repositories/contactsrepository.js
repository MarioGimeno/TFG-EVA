const { pool } = require('../config/Pool');

class ContactsRepository {
  /**
   * Recupera todos los contactos de un userId dado. 
   * Devuelve: contact_user_id, nombre (alias) y email del usuario contacto.
   */
  async findAllByUserId(userId) {
    const query = `
      SELECT
        c.contact_user_id,
        c.nombre AS name,
        u.email
      FROM contacts AS c
      JOIN users AS u
        ON c.contact_user_id = u.id
      WHERE c.user_id = $1
      ORDER BY u.email
    `;
    const { rows } = await pool.query(query, [userId]);
    return rows;
  }

  /**
   * Busca el userId de un usuario a partir de su correo.
   * (Se usa, por ejemplo, antes de crear un contacto.)
   */
  async findUserIdByEmail(email) {
    const { rows, rowCount } = await pool.query(
      `SELECT id FROM users WHERE email = $1`,
      [email]
    );
    if (rowCount === 0) {
      throw new Error('El email no existe');
    }
    return rows[0].id;
  }

  /**
   * Crea un contacto nuevo.
   * @param {number} ownerId       — id del usuario que agrega el contacto
   * @param {string} alias         — nombre o apodo que le pone el owner al contacto
   * @param {number} contactUserId — id del usuario destino (FK a users.id)
   * @returns {Promise<Object>}    — devuelve { contact_user_id, nombre }
   */
  async createContact(ownerId, alias, contactUserId) {
    const query = `
      INSERT INTO contacts (
        user_id,
        contact_user_id,
        nombre
      ) VALUES ($1, $2, $3)
      RETURNING
        contact_user_id,
        nombre
    `;
    const values = [ownerId, contactUserId, alias];
    const { rows } = await pool.query(query, values);
    return rows[0];
  }

  /**
   * Elimina un contacto. Como la PK es compuesta (user_id, contact_user_id),
   * usamos ambas columnas en el WHERE.
   */
  async deleteContact(ownerId, contactUserId) {
    // Logging interno para verificar que llegan los dos valores
    console.log('[ContactsRepository] deleteContact called with:', {
      ownerId,
      contactUserId
    });

    const query = `
      DELETE FROM contacts
       WHERE user_id = $1
         AND contact_user_id = $2
    `;
    const values = [ ownerId, contactUserId ];
    const result = await pool.query(query, values);

    // Opcional: podrías comprobar cuántas filas afectó:
    // console.log('[ContactsRepository] rows deleted:', result.rowCount);
    return result;
  }

}

module.exports = new ContactsRepository();
