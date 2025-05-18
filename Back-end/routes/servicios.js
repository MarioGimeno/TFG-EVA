// routes/servicios.js
const express = require('express');
const router  = express.Router();
const {
  getAllRecursos,
  getRecursoById,
  createRecurso,
  updateRecurso,
  deleteRecurso,
  getRecursosByCategoria,
  getRecursosFiltrados
} = require('../services/recursoService');

// GET /api/servicios — todos los recursos
router.get('/', async (req, res) => {
  try {
    const rows = await getAllRecursos();
    res.json(rows);
  } catch (e) {
    console.error('GET /api/servicios error', e);
    res.status(500).json({ error: 'Error al obtener servicios' });
  }
});

// GET /api/servicios/categoria/:id — recursos de una categoría
router.get('/categoria/:id', async (req, res) => {
  try {
    const rows = await getRecursosByCategoria(req.params.id);
    res.json(rows);
  } catch (e) {
    console.error('GET /api/servicios/categoria/:id error', e);
    res.status(500).json({ error: 'Error al obtener recursos por categoría' });
  }
});

// GET /api/servicios/gratuitos — recursos filtrados gratuitos
router.get('/gratuitos', async (req, res) => {
  try {
    const rows = await getRecursosFiltrados({ gratuito: true, accesible: undefined });
    res.json(rows);
  } catch (e) {
    console.error('GET /api/servicios/gratuitos error', e);
    res.status(500).json({ error: 'Error al obtener recursos gratuitos' });
  }
});

// GET /api/servicios/accesibles — recursos filtrados accesibles
router.get('/accesibles', async (req, res) => {
  try {
    const rows = await getRecursosFiltrados({ gratuito: undefined, accesible: true });
    res.json(rows);
  } catch (e) {
    console.error('GET /api/servicios/accesibles error', e);
    res.status(500).json({ error: 'Error al obtener recursos accesibles' });
  }
});

// GET /api/servicios/:id — un único recurso por ID
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

// POST /api/servicios — crear nuevo recurso
router.post('/', /* authMiddleware, */ async (req, res) => {
  try {
    const nueva = await createRecurso(req.body);
    res.status(201).json(nueva);
  } catch (e) {
    console.error('POST /api/servicios error', e);
    res.status(400).json({ error: e.message });
  }
});

// PUT /api/servicios/:id — actualizar recurso existente
router.put('/:id', /* authMiddleware, */ async (req, res) => {
  try {
    const updated = await updateRecurso(req.params.id, req.body);
    res.json(updated);
  } catch (e) {
    console.error('PUT /api/servicios/:id error', e);
    res.status(400).json({ error: e.message });
  }
});

// DELETE /api/servicios/:id — borrar recurso
router.delete('/:id', /* authMiddleware, */ async (req, res) => {
  try {
    await deleteRecurso(req.params.id);
    res.status(204).end();
  } catch (e) {
    console.error('DELETE /api/servicios/:id error', e);
    res.status(400).json({ error: e.message });
  }
});

module.exports = router;
