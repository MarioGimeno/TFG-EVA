package com.example.appGrabacion.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.appGrabacion.models.ContactEntry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ContactManager {
    private static final String PREFS = "contacts_prefs";
    private static final String KEY_CONTACTS = "key_contacts";
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public ContactManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<ContactEntry> getContacts() {
        String json = prefs.getString(KEY_CONTACTS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<ContactEntry>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveContacts(List<ContactEntry> list) {
        String json = gson.toJson(list);
        prefs.edit().putString(KEY_CONTACTS, json).apply();
    }

    public void addContact(ContactEntry c) {
        List<ContactEntry> list = getContacts();
        list.add(c);
        saveContacts(list);
    }

    /**
     * Devuelve los IDs numéricos de tus contactos almacenados.
     */
    public List<Integer> getContactIds() {
        List<ContactEntry> contacts = getContacts();
        List<Integer> ids = new ArrayList<>();
        for (ContactEntry c : contacts) {
            ids.add(c.getId());
        }
        return ids;
    }

    public void removeContact(ContactEntry c) {
        List<ContactEntry> list = getContacts();
        list.remove(c);
        saveContacts(list);
    }
}
