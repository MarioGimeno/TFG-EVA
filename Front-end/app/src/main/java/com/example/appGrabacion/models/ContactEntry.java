// ContactEntry.java
package com.example.appGrabacion.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo que representa un contacto, con mapeo del campo remoto "contact_user_id" y correo electrónico.
 */
public class ContactEntry {
    /**
     * ID local (primary key interno, opcional).
     */
    private int id;

    /**
     * ID remoto del usuario en el servidor (contact_user_id).
     */
    @SerializedName("contact_user_id")
    private int contactUserId;

    /**
     * Nombre o alias del contacto.
     */
    private String name;

    /**
     * Email del contacto.
     */
    private String email;

    public ContactEntry() { }

    public ContactEntry(int id, int contactUserId, String name, String email) {
        this.id = id;
        this.contactUserId = contactUserId;
        this.name = name;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContactUserId() {
        return contactUserId;
    }

    public void setContactUserId(int contactUserId) {
        this.contactUserId = contactUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "ContactEntry{" +
                "id=" + id +
                ", contactUserId=" + contactUserId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

