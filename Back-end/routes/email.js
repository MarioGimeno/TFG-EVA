// routes/email.js
const express = require('express');
const router  = express.Router();
const { sendEmail } = require('../services/emailService');
const authenticate = require('../middleware/authMiddleware');

router.post('/', authenticate, async (req, res) => {
  const { to, subject, text, html } = req.body;
  if (!to || !subject) return res.status(400).json({ error: 'Missing to or subject' });
  try {
    await sendEmail({ to, subject, text, html });
    res.json({ ok: true });
  } catch (err) {
    console.error('Error sending email:', err);
    res.status(500).json({ error: 'Error interno enviando email' });
  }
});

module.exports = router;
