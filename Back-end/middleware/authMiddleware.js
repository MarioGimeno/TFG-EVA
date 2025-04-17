const { verifyToken } = require('../services/authService');

function authMiddleware(req, res, next) {
  const auth = req.headers.authorization;
  if (!auth) return res.status(401).json({ error:'No autorizado' });
  const [ , token ] = auth.split(' ');
  const payload = verifyToken(token);
  if (!payload) return res.status(401).json({ error:'Token inválido' });
  req.userId = payload.sub;
  next();
}

module.exports = authMiddleware;
