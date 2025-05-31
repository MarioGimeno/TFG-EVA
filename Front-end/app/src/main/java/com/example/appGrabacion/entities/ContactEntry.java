package com.example.appGrabacion.entities;

import com.google.gson.annotations.SerializedName;

public class ContactEntry {
    /** Este id ya no se usa para pintar, solo necesitamos contactUserId, name y email */
    //private int id;

    @SerializedName("contact_user_id")
    private Integer contactUserId;

    private String name;
    private String email;

    public ContactEntry() { }

    public ContactEntry(Integer contactUserId, String name, String email) {
        this.contactUserId = contactUserId;
        this.name = name;
        this.email = email;
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
}
