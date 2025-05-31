// src/controllers/contactsController.js
const service = require('../services/contactsservice');

exports.list = async (req, res, next) => {
  try {
    const contacts = await service.getContacts(req.userId);
    res.json(contacts);
  } catch (err) {
    next(err);
  }
};
exports.create = async (req, res, next) => {
  // Extraemos email y name (si existe) del body
  const { email, name, nombre } = req.body;
  console.log('[ContactsController] create called with:', { email, name, nombre });

  try {
    // Si no nos llega name, usamos nombre; si tampoco viene, usamos la parte antes de la arroba del email
    let alias = name ?? nombre;
    if (!alias) {
      // Ejemplo de fallback: tomar lo que hay antes de la “@” en el email
      alias = email.split('@')[0];
    }

    // Llamamos al servicio con (ownerId, emailContacto, alias)
    const newContact = await service.addContact(req.userId, email, alias);
    return res.status(201).json(newContact);
  } catch (error) {
    if (error.statusCode === 404) {
      return res.status(404).json({ message: error.message });
    }
    console.error(error);
    return res.status(500).json({ message: error.message });
  }
};

exports.delete = async (req, res, next) => {
  // Logueamos el userId y el id del contacto que llega en params
  console.log('[ContactsController] delete called with userId:', req.userId, 'contactUserId:', req.params.id);

  try {
    // Llamamos al service PASANDO EXACTAMENTE (ownerId, contactUserId)
    await service.removeContact(req.userId, parseInt(req.params.id, 10));
    return res.sendStatus(204);
  } catch (err) {
    console.error('[ContactsController] error deleting contact:', err);
    return next(err);
  }
};
