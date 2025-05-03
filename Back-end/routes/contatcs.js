// routes/contacts.js
const express = require('express');
const router = express.Router();
const { pool } = require('../config');
const authenticate = require('../middleware/authMiddleware');

// Debug middleware
router.use((req, res, next) => {
  console.log('🛎️ Contacts router middleware:', req.method, req.originalUrl);
  next();
});


/**
 * GET /api/contacts
 */
router.get(
    '/',
    authenticate,
    async (req, res) => {
      try {
        const result = await pool.query(
          `SELECT id, name, email, contact_user_id
             FROM contacts
            WHERE user_id = $1
         ORDER BY id`,
          [req.userId]
        );
        res.json(result.rows);
      } catch (err) {
        console.error('Error fetching contacts:', err);
        res.status(500).json({ error: 'Error interno listando contactos' });
      }
    }
  );
  

/**
 * POST /api/contacts
 */
router.post(
    '/',
    authenticate,
    async (req, res) => {
      const ownerId = req.userId;
      const { name, email } = req.body;
      if (!name || !email) {
        return res.status(400).json({ error: 'Faltan name o email' });
      }
  
      try {
        // 1) Intentar encontrar el usuario destino
        const userRes = await pool.query(
          'SELECT id FROM users WHERE email = $1',
          [email]
        );
        const contactUserId = userRes.rowCount
          ? userRes.rows[0].id
          : null;
  
        // 2) Insertar en contacts incluyendo contact_user_id
        const insertRes = await pool.query(
          `INSERT INTO contacts(user_id, name, email, contact_user_id)
           VALUES($1, $2, $3, $4)
           RETURNING id, name, email, contact_user_id`,
          [ownerId, name, email, contactUserId]
        );
  
        res.status(201).json(insertRes.rows[0]);
      } catch (err) {
        console.error('Error creating contact:', err);
        res.status(500).json({ error: 'Error interno creando contacto' });
      }
    }
  );
  
/**
 * DELETE /api/contacts/:id
 */
router.delete(
  '/:id',
  authenticate,
  async (req, res) => {
    const userId = req.userId;
    const contactId = req.params.id;
    console.log(`→ DELETE contact ${contactId} for userId ${userId}`);
    try {
      await pool.query(
        'DELETE FROM contacts WHERE id = $1 AND user_id = $2',
        [contactId, userId]
      );
      console.log('✅ Deleted contact id', contactId);
      res.sendStatus(204);
    } catch (err) {
      console.error('Error deleting contact:', err);
      res.status(500).json({ error: 'Error interno borrando contacto' });
    }
  }
);

module.exports = router;
