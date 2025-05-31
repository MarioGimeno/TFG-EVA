const contactsRepo = require('../repositories/contactsrepository');

class ContactsService {
    constructor(contactsRepository) {
      this.contactsRepository = contactsRepository;
    }
  
    async getContacts(userId) {
      return this.contactsRepository.findAllByUserId(userId);
    }
  /**
   * - ownerId: id del usuario que agrega el contacto
   * - emailContacto: correo del usuario destino
   * - alias: nombre/apodo que el owner le pone al contacto
   */
  async addContact(ownerId, emailContacto, alias) {
    // 1. Convertimos emailContacto → contactUserId (INTEGER)
    const contactUserId = await this.contactsRepository.findUserIdByEmail(emailContacto);

    // 2. Llamamos al repositorio con (ownerId, alias, contactUserId)
    //    Debe coincidir exactamente con el INSERT VALUES ($1, $2, $3)
    const nuevo = await this.contactsRepository.createContact(
      ownerId,        // $1 → user_id (INTEGER)
      alias,          // $3 → nombre (TEXT)
      contactUserId   // $2 → contact_user_id (INTEGER)
    );

    return nuevo;
  }

  /**
   * @param {number} ownerId        — el user_id que hace la petición (dueño de la sesión)
   * @param {number} contactUserId  — el contact_user_id que queremos eliminar
   */
  async removeContact(ownerId, contactUserId) {
    console.log('[ContactsService] removeContact called with:', { ownerId, contactUserId });
    return await this.contactsRepository.deleteContact(ownerId, contactUserId);
  }
  }
  
  // Luego exportas pasando la instancia del repositorio
  module.exports = new ContactsService(contactsRepo);
  