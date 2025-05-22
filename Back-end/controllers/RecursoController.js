// src/controllers/RecursoController.js
const service = require('../services/recursoservice');

class RecursoController {
    async list(req, res, next) {
        try { res.json(await service.getAll()); }
        catch (e) { next(e); }
      }
      async byCategoria(req, res, next) {
        try { res.json(await service.getByCategoria(req.params.id)); }
        catch (e) { next(e); }
      }
      async gratuitos(req, res, next) {
        try { res.json(await service.getGratuitos()); }
        catch (e) { next(e); }
      }
      async accesibles(req, res, next) {
        try { res.json(await service.getAccesibles()); }
        catch (e) { next(e); }
      }
      async getOne(req, res, next) {
        try { res.json(await service.getById(req.params.id)); }
        catch (e) { next(e); }
      }
      async create(req, res, next) {
        try { res.status(201).json(await service.create(req.body)); }
        catch (e) { next(e); }
      }
      async update(req, res, next) {
        try { res.json(await service.update(req.params.id, req.body)); }
        catch (e) { next(e); }
      }
      async delete(req, res, next) {
        try { await service.delete(req.params.id); res.sendStatus(204); }
        catch (e) { next(e); }
      }
}

module.exports = new RecursoController();
