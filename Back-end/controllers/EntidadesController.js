// src/controllers/EntidadesController.js
const service = require('../services/entidadesservice');

class EntidadesController {
  async list(req, res, next) {
    try {
      const items = await service.getAllEntidades();
      res.json(items);
    } catch (err) {
      next(err);
    }
  }

  async getOne(req, res, next) {
    try {
      const item = await service.getEntidadById(req.params.id);
      res.json(item);
    } catch (err) {
      next(err);
    }
  }

  async create(req, res, next) {
    try {
      const created = await service.createEntidad(req.body);
      res.status(201).json(created);
    } catch (err) {
      next(err);
    }
  }

  async update(req, res, next) {
    try {
      const updated = await service.updateEntidad(req.params.id, req.body);
      res.json(updated);
    } catch (err) {
      next(err);
    }
  }

  async delete(req, res, next) {
    try {
      await service.deleteEntidad(req.params.id);
      res.sendStatus(204);
    } catch (err) {
      next(err);
    }
  }
}

module.exports = new EntidadesController();
