// ContactEntry.java
package com.example.appGrabacion.models;

import com.google.gson.annotations.SerializedName;

public class ContactEntry {
    /** ID del contacto (viene siempre lleno) */
    private int id;

    /** Puede ser null, así que envoltorio Integer */
    @SerializedName("contact_user_id")
    private Integer contactUserId;

    private String name;
    private String email;

    public ContactEntry() { }

    public ContactEntry(int id, Integer contactUserId, String name, String email) {
        this.id = id;
        this.contactUserId = contactUserId;
        this.name = name;
        this.email = email;
    }

    // getters y setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public Integer getContactUserId() {
        return contactUserId;
    }
    public void setContactUserId(Integer contactUserId) {
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
