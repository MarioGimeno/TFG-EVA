// src/routes/entidades.js
const express               = require('express');
const router                = express.Router();
const EntidadesController   = require('../controllers/entidadescontroller');

// GET /api/entidades        — público
router.get('/',      (req, res, next) => EntidadesController.list(req, res, next));

// GET /api/entidades/:id    — público
router.get('/:id',   (req, res, next) => EntidadesController.getOne(req, res, next));

// POST /api/entidades       — público
router.post('/',     (req, res, next) => EntidadesController.create(req, res, next));

// PUT /api/entidades/:id    — público
router.put('/:id',   (req, res, next) => EntidadesController.update(req, res, next));

// DELETE /api/entidades/:id — público
router.delete('/:id',(req, res, next) => EntidadesController.delete(req, res, next));

module.exports = router;
