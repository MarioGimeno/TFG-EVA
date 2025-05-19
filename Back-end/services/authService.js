// src/services/AuthService.js
const bcrypt    = require('bcrypt');
const jwt       = require('jsonwebtoken');
const userRepo  = require('../repositories/UserRepository');
const {
  JWT_SECRET,
  JWT_EXPIRES,
  REFRESH_EXPIRES
} = require('../config/Pool');

class AuthService {
  /**
   * Registra un usuario nuevo y devuelve { token, refreshToken }.
   */
  async register({ email, password }) {
    if (await userRepo.findByEmail(email)) {
      const err = new Error('Usuario ya existe');
      err.status = 400;
      throw err;
    }
    const passwordHash = await bcrypt.hash(password, 10);
    const user = await userRepo.createUser({ email, passwordHash });
    return this._generateTokens(user.id);
  }

  /**
   * Valida credenciales y devuelve { token, refreshToken }.
   */
  async login({ email, password }) {
    const user = await userRepo.findByEmail(email);
    if (!user || !await bcrypt.compare(password, user.password_hash)) {
      const err = new Error('Credenciales inválidas');
      err.status = 400;
      throw err;
    }
    return this._generateTokens(user.id);
  }

  /**
   * Renueva tokens a partir de un refreshToken válido.
   */
  async refresh(refreshToken) {
    const payload = this.verifyToken(refreshToken);
    if (!payload) {
      const err = new Error('Refresh inválido');
      err.status = 401;
      throw err;
    }
    return this._generateTokens(payload.sub);
  }

  /**
   * Verifica un JWT cualquiera y devuelve su payload o null.
   */
  verifyToken(token) {
    try {
      return jwt.verify(token, JWT_SECRET);
    } catch {
      return null;
    }
  }

  /**
   * Genera un par de tokens (access + refresh) para un userId.
   */
  _generateTokens(userId) {
    const payload      = { sub: userId };
    const token        = jwt.sign(payload, JWT_SECRET,     { expiresIn: JWT_EXPIRES });
    const refreshToken = jwt.sign(payload, JWT_SECRET,     { expiresIn: REFRESH_EXPIRES });
    return { token, refreshToken };
  }
}

module.exports = new AuthService();
