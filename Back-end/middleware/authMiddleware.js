const { verifyToken } = require('../services/authservice');

function authMiddleware(req, res, next) {
  try {
    const auth = req.headers.authorization;
    if (!auth) {
      console.log('[authMiddleware] No hay header Authorization');
      return res.status(401).json({ error:'No autorizado' });
    }
    const [ , token ] = auth.split(' ');
    if (!token) {
      console.log('[authMiddleware] Header Authorization mal formado:', auth);
      return res.status(401).json({ error:'Token no encontrado' });
    }

    const payload = verifyToken(token);
    if (!payload) {
      console.log('[authMiddleware] Token inválido:', token);
      return res.status(401).json({ error:'Token inválido' });
    }

    console.log('[authMiddleware] Token verificado, payload:', payload);
    req.userId = payload.sub;
    next();

  } catch (error) {
    console.error('[authMiddleware] Error inesperado:', error);
    res.status(500).json({ error: 'Error en autenticación' });
  }
}
module.exports = authMiddleware;
