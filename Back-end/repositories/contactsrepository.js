// src/repositories/contactsRepository.js
const { pool } = require('../config/Pool');

class ContactsRepository {
  async findAllByUserId(userId) {
    const { rows } = await pool.query(
      `SELECT id, name, email, contact_user_id
         FROM contacts
        WHERE user_id = $1
     ORDER BY id`,
      [userId]
    );
    return rows;
  }
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
  
  
  async createContact(ownerId, name, email, contactUserId) {
    const { rows } = await pool.query(
      `INSERT INTO contacts(user_id, name, email, contact_user_id)
       VALUES($1, $2, $3, $4)
       RETURNING id, name, email, contact_user_id`,
      [ownerId, name, email, contactUserId]
    );
    return rows[0];
  }

  async deleteContact(contactId, ownerId) {
    await pool.query(
      `DELETE FROM contacts
         WHERE id = $1
           AND user_id = $2`,
      [contactId, ownerId]
    );
  }
}

module.exports = new ContactsRepository();
