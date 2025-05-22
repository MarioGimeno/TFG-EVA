// src/services/RecursoService.js
const repo = require('../repositories/recursorepository');

class RecursoService {
  async getAll() {
    return repo.findAll();
  }
  async getById(id) {
    const item = await repo.findById(id);
    if (!item) { const err = new Error('No encontrado'); err.status = 404; throw err; }
    return item;
  }
  async getByCategoria(idCat) {
    return repo.findByCategoria(idCat);
  }
  async getGratuitos() {
    return repo.findFiltered({ gratuito: true });
  }
  async getAccesibles() {
    return repo.findFiltered({ accesible: true });
  }
  async create(data) {
    return repo.create(data);
  }
  async update(id, data) {
    await this.getById(id);
    return repo.update(id, data);
  }
  async delete(id) {
    await this.getById(id);
    await repo.delete(id);
  }
}

module.exports = new RecursoService();
