const express = require('express');
const { list, byId } = require('../controllers/CategoriaController');
const router = express.Router();

router.get('/', list);
router.get('/:id', byId);

module.exports = router;
