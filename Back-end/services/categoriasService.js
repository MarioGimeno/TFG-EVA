// services/categoriasService.js
const repo = require('../repositories/categoriasrepository');

class CategoriasService {
  async getAllCategorias() {
    // aquí podrías mapear DTOs, filtrar campos, etc.
    return repo.findAll();
  }

  async getCategoriaById(id) {
    if (isNaN(id)) throw new Error('ID inválido');
    const cat = await repo.findById(id);
    if (!cat) return null;
    return cat;
  }
}

module.exports = new CategoriasService();
