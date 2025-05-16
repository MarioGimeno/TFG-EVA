// routes/servicios.js
const express = require('express');
const router  = express.Router();
const {
  getAllRecursos,
  getRecursoById,
  createRecurso,
  updateRecurso,
  deleteRecurso,
  getRecursosByCategoria
} = require('../services/recursoService');

// GET /api/servicios
router.get('/', async (req, res) => {
  try {
    const rows = await getAllRecursos();
    res.json(rows);
  } catch (e) {
    console.error('GET /api/servicios error', e);
    res.status(500).json({ error: 'Error al obtener servicios' });
  }
});

// GET /api/servicios/:id
router.get('/:id', async (req, res) => {
  try {
    const item = await getRecursoById(req.params.id);
    if (!item) return res.status(404).json({ error: 'No encontrado' });
    res.json(item);
  } catch (e) {
    console.error('GET /api/servicios/:id error', e);
    res.status(500).json({ error: 'Error al obtener servicio' });
  }
});

// POST /api/servicios
router.post('/', /* authMiddleware, */ async (req, res) => {
  try {
    const nueva = await createRecurso(req.body);
    res.status(201).json(nueva);
  } catch (e) {
    console.error('POST /api/servicios error', e);
    res.status(400).json({ error: e.message });
  }
});

// PUT /api/servicios/:id
router.put('/:id', /* authMiddleware, */ async (req, res) => {
  try {
    const updated = await updateRecurso(req.params.id, req.body);
    res.json(updated);
  } catch (e) {
    console.error('PUT /api/servicios/:id error', e);
    res.status(400).json({ error: e.message });
  }
});

// DELETE /api/servicios/:id
router.delete('/:id', /* authMiddleware, */ async (req, res) => {
  try {
    await deleteRecurso(req.params.id);
    res.status(204).end();
  } catch (e) {
    console.error('DELETE /api/servicios/:id error', e);
    res.status(400).json({ error: e.message });
  }
});
//GET /api/servicios/:id
router.get('/categoria/:id', async (req, res) => {
  try {
    const rows = await getRecursosByCategoria(req.params.id);
    res.json(rows);
  } catch (e) {
    console.error('GET /api/servicios/categoria/:id error', e);
    res.status(500).json({ error: 'Error al obtener recursos por categoría' });
  }
});

module.exports = router;
