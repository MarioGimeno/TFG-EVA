// src/services/authService.js
const bcrypt = require('bcrypt');
const jwt    = require('jsonwebtoken');
const { createUser, findUserByEmail } = require('./userService');
const { JWT_SECRET, JWT_EXPIRES, REFRESH_EXPIRES } = require('../config');

/**
 * Registra un nuevo usuario y devuelve tokens.
 */
async function register({ email, password }) {
  if (await findUserByEmail(email)) {
    throw new Error('Usuario ya existe');
  }
  const hash = await bcrypt.hash(password, 10);
  const user = await createUser({ email, passwordHash: hash });
  return _generateTokens(user.id);
}

/**
 * Login de usuario, comprueba contraseña y emite tokens.
 */
async function login({ email, password }) {
  const user = await findUserByEmail(email);
  if (!user || !await bcrypt.compare(password, user.password)) {
    throw new Error('Credenciales inválidas');
  }
  return _generateTokens(user.id);
}

/** Genera JWT y refresh token */
function _generateTokens(userId) {
  const payload = { sub: userId };
  const token        = jwt.sign(payload, JWT_SECRET, { expiresIn: JWT_EXPIRES });
  const refreshToken = jwt.sign(payload, JWT_SECRET, { expiresIn: REFRESH_EXPIRES });
  return { token, refreshToken };
}

/** Verifica un token JWT */
function verifyToken(token) {
  try {
    return jwt.verify(token, JWT_SECRET);
  } catch {
    return null;
  }
}

module.exports = { register, login, verifyToken };
