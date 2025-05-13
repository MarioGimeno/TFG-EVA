// routes/entidades.js
const express = require('express');
const router  = express.Router();
const {
  getAllEntidades,
  getEntidadById,
  createEntidad,
  updateEntidad,
  deleteEntidad
} = require('../services/entidadService');

// GET /api/entidades        — público
router.get('/', async (req, res) => {
  try {
    const rows = await getAllEntidades();
    res.json(rows);
  } catch (e) {
    console.error('GET /api/entidades error', e);
    res.status(500).json({ error: 'Error al obtener entidades' });
  }
});

// GET /api/entidades/:id    — público
router.get('/:id', async (req, res) => {
  try {
    const entidad = await getEntidadById(req.params.id);
    if (!entidad) return res.status(404).json({ error: 'No encontrada' });
    res.json(entidad);
  } catch (e) {
    console.error('GET /api/entidades/:id error', e);
    res.status(500).json({ error: 'Error al obtener entidad' });
  }
});

// POST /api/entidades       — protegido (requiere token)
router.post('/', /* authMiddleware, */ async (req, res) => {
  try {
    const nueva = await createEntidad(req.body);
    res.status(201).json(nueva);
  } catch (e) {
    console.error('POST /api/entidades error', e);
    res.status(400).json({ error: e.message });
  }
});

// PUT /api/entidades/:id    — protegido
router.put('/:id', /* authMiddleware, */ async (req, res) => {
  try {
    const updated = await updateEntidad(req.params.id, req.body);
    res.json(updated);
  } catch (e) {
    console.error('PUT /api/entidades/:id error', e);
    res.status(400).json({ error: e.message });
  }
});

// DELETE /api/entidades/:id — protegido
router.delete('/:id', /* authMiddleware, */ async (req, res) => {
  try {
    await deleteEntidad(req.params.id);
    res.status(204).end();
  } catch (e) {
    console.error('DELETE /api/entidades/:id error', e);
    res.status(400).json({ error: e.message });
  }
});

module.exports = router;
