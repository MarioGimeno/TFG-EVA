// controllers/categoriasController.js
const service = require('../services/categoriasService');

exports.list = async (req, res, next) => {
  try {
    const rows = await service.getAllCategorias();
    res.json(rows);
  } catch (e) {
    next(e);
  }
};

exports.byId = async (req, res, next) => {
  try {
    const cat = await service.getCategoriaById(req.params.id);
    if (!cat) return res.status(404).json({ error: 'No encontrada' });
    res.json(cat);
  } catch (e) {
    next(e);
  }
};
