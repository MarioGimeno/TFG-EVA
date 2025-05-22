// src/services/EntidadesService.js
const repo = require('../repositories/entidadesrepository');

class EntidadesService {
  async getAllEntidades() {
    return repo.findAll();
  }

  async getEntidadById(id) {
    const entidad = await repo.findById(id);
    if (!entidad) {
      const err = new Error('Entidad no encontrada');
      err.status = 404;
      throw err;
    }
    return entidad;
  }

  async createEntidad(data) {
    // aquí podrías validar campos obligatorios…
    return repo.create(data);
  }

  async updateEntidad(id, data) {
    // validación de existencia
    await this.getEntidadById(id);
    return repo.update(id, data);
  }

  async deleteEntidad(id) {
    // validación de existencia
    await this.getEntidadById(id);
    await repo.delete(id);
  }
}

module.exports = new EntidadesService();
