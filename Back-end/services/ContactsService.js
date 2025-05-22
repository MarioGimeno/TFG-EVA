const contactsRepo = require('../repositories/contactsrepository');

class ContactsService {
    constructor(contactsRepository) {
      this.contactsRepository = contactsRepository;
    }
  
    async getContacts(userId) {
      return this.contactsRepository.findAllByUserId(userId);
    }
 async addContact(ownerId, contactData) {
  try {
    // Busca el userId del email del contacto
    const contactUserId = await this.contactsRepository.findUserIdByEmail(contactData.email);

    // Crea el contacto con los datos
    const newContact = await this.contactsRepository.createContact(
      ownerId,
      contactData.name,
      contactData.email,
      contactUserId
    );

    return newContact;

  } catch (error) {
    if (error.message === 'El email no existe') {
      const err = new Error('El email no existe');
      err.statusCode = 404;
      throw err;
    }
    throw error;
  }
}

    async removeContact(userId, contactId) {
      await this.contactsRepository.deleteContact(contactId, userId);
    }
  }
  
  // Luego exportas pasando la instancia del repositorio
  module.exports = new ContactsService(contactsRepo);
  