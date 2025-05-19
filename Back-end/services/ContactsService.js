// src/services/contactsService.js
const contactsRepo = require('../repositories/ContactsRepository');

class ContactsService {
  async getContacts(userId) {
    return contactsRepo.findAllByUserId(userId);
  }

  async addContact(userId, { name, email }) {
    if (!name || !email) {
      const err = new Error('Faltan name o email');
      err.status = 400;
      throw err;
    }
    const contactUserId = await contactsRepo.findUserIdByEmail(email);
    return contactsRepo.createContact(userId, name, email, contactUserId);
  }

  async removeContact(userId, contactId) {
    await contactsRepo.deleteContact(contactId, userId);
  }
}

module.exports = new ContactsService();
