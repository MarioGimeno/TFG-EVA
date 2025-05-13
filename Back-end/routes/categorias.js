// routes/categorias.js
const express = require('express');
const router  = express.Router();
const { getAllCategorias, getCategoriaById } = require('../services/categoriasService');

// GET /api/categorias
router.get('/', async (req, res) => {
  try {
    const rows = await getAllCategorias();
    res.json(rows);
  } catch (e) {
    console.error('GET /api/categorias error', e);
    res.status(500).json({ error: 'Error al obtener categorías' });
  }
});

// GET /api/categorias/:id
router.get('/:id', async (req, res) => {
  try {
    const cat = await getCategoriaById(req.params.id);
    if (!cat) return res.status(404).json({ error: 'No encontrada' });
    res.json(cat);
  } catch (e) {
    console.error('GET /api/categorias/:id error', e);
    res.status(500).json({ error: 'Error al obtener categoría' });
  }
});

module.exports = router;
