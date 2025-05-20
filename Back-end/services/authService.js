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
    console.log('[AuthService] register called with:', { email, password });
    if (await userRepo.findByEmail(email)) {
      const err = new Error('Usuario ya existe');
      err.status = 400;
      throw err;
    }
    const hash = await bcrypt.hash(password, 10);
    console.log('[AuthService] password hashed:', hash);
    const user = await userRepo.createUser({ email, passwordHash: hash });
    console.log('[AuthService] new user created:', { id: user.id, email: user.email });
    const tokens = this._generateTokens(user.id);
    console.log('[AuthService] tokens generated on register:', tokens);
    return tokens;
  }

  /**
   * Valida credenciales y devuelve { token, refreshToken }.
   */
  async login({ email, password }) {
    console.log('[AuthService] login called with:', { email, password });
    const user = await userRepo.findByEmail(email);
    console.log('[AuthService] fetched user:', user);
    if (!user) {
      console.log('[AuthService] no user found for email');
      const err = new Error('Credenciales inválidas');
      err.status = 400;
      throw err;
    }
    const match = await bcrypt.compare(password, user.passwordHash);
    console.log('[AuthService] password match result:', match);
    if (!match) {
      const err = new Error('Credenciales inválidas');
      err.status = 400;
      throw err;
    }
    const tokens = this._generateTokens(user.id);
    console.log('[AuthService] tokens generated on login:', tokens);
    return tokens;
  }

  /**
   * Renueva tokens a partir de un refreshToken válido.
   */
  async refresh(refreshToken) {
    console.log('[AuthService] refresh called with token:', refreshToken);
    const payload = this.verifyToken(refreshToken);
    console.log('[AuthService] refresh payload:', payload);
    if (!payload) {
      const err = new Error('Refresh inválido');
      err.status = 401;
      throw err;
    }
    const tokens = this._generateTokens(payload.sub);
    console.log('[AuthService] tokens generated on refresh:', tokens);
    return tokens;
  }

  /**
   * Verifica un JWT y devuelve el payload, o null si es inválido.
   */
  verifyToken(token) {
    try {
      console.log(token);
      const payload = jwt.verify(token, JWT_SECRET);
      console.log('[AuthService] verifyToken payload:', payload);
      return payload;
    } catch (e) {
      console.log('[AuthService] verifyToken error:', e.message);
      return null;
    }
  }

  /**
   * Genera ambos tokens (access + refresh).
   */
  _generateTokens(userId) {
    const payload      = { sub: userId };
    const token        = jwt.sign(payload, JWT_SECRET,     { expiresIn: JWT_EXPIRES });
    const refreshToken = jwt.sign(payload, JWT_SECRET,     { expiresIn: REFRESH_EXPIRES });
    console.log('[AuthService] generated tokens for userId:', userId);
    console.log('[AuthService] access token:', token);
    console.log('[AuthService] refresh token:', refreshToken);
    return { token, refreshToken };
  }
}

module.exports = new AuthService();
