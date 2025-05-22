class ContactsService {
    constructor(contactsRepository) {
      this.contactsRepository = contactsRepository;
    }
  
    async getContacts(userId) {
      return this.contactsRepository.findAllByUserId(userId);
    }
  
    async addContact(contactData) {
      try {
        const userId = await this.contactsRepository.findUserIdByEmail(contactData.email);
        // lógica para añadir contacto usando userId
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
  const contactsRepo = require('../repositories/contactsrepository');
  module.exports = new ContactsService(contactsRepo);
  