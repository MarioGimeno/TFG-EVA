// src/controllers/contactsController.js
const service = require('../services/ContactsService');

exports.list = async (req, res, next) => {
  try {
    const contacts = await service.getContacts(req.userId);
    res.json(contacts);
  } catch (err) {
    next(err);
  }
};

exports.create = async (req, res, next) => {
  try {
    const newContact = await service.addContact(req.userId, req.body);
    res.status(201).json(newContact);
  } catch (err) {
    next(err);
  }
};

exports.delete = async (req, res, next) => {
  try {
    await service.removeContact(req.userId, req.params.id);
    res.sendStatus(204);
  } catch (err) {
    next(err);
  }
};
