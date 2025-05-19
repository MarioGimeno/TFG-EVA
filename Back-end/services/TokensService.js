// src/services/TokensService.js
const repo = require('../repositories/TokensRepository');

class TokensService {
  /**
   * Valida input y delega al repositorio para guardar el token.
   */
  async saveToken(userId, token) {
    if (!token || typeof token !== 'string') {
      const err = new Error('Falta token en el body');
      err.status = 400;
      throw err;
    }
    await repo.upsertToken(userId, token);
  }
}

module.exports = new TokensService();
