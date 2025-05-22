const express = require('express');
const router = express.Router();
const auth = require('../middleware/authMiddleware');
const contactsController = require('../controllers/contactscontroller');

router.use((req, res, next) => {
  console.log('🛎️ Contacts router:', req.method, req.originalUrl);
  next();
});

router.get('/', auth, contactsController.list);
router.post('/', auth, contactsController.create);
router.delete('/:id', auth, contactsController.delete);

module.exports = router;
