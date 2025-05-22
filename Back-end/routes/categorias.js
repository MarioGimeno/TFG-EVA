const express = require('express');
const { list, byId } = require('../controllers/categoriacontroller');
const router = express.Router();

router.get('/', list);
router.get('/:id', byId);

module.exports = router;
