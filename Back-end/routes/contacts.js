// src/routes/contacts.js
const express   = require('express');
const router    = express.Router();
const auth      = require('../middleware/authMiddleware');
const ctrl      = require('../controllers/contactscontroller');

// Debug middleware (opcional)
router.use((req, res, next) => {
  console.log('🛎️ Contacts router:', req.method, req.originalUrl);
  next();
});

router.get(    '/',    auth, ctrl.list);
router.post('/', auth, (req, res, next) => {
  console.log('Middleware justo antes del controlador en POST /api/contacts');
  next();
}, ctrl.create);
router.delete('/:id',  auth, ctrl.delete);

module.exports = router;
