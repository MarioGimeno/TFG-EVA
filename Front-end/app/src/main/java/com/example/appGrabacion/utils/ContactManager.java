
// ContactManager.java
package com.example.appGrabacion.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.appGrabacion.entities.ContactEntry;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la persistencia ligera de contactos y provee los IDs remotos para notificaciones.
 */
public class ContactManager {
    private static final String PREFS = "contacts_prefs";
    private static final String KEY_CONTACTS = "key_contacts";
    private static final String KEY_MIGRATED = "migrated_to_v2";
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public ContactManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        // Migración: elimina el JSON antiguo una sola vez
        if (!prefs.getBoolean(KEY_MIGRATED, false)) {
            prefs.edit()
                    .remove(KEY_CONTACTS)
                    .putBoolean(KEY_MIGRATED, true)
                    .apply();
        }
    }

    /**
     * Devuelve todos los ContactEntry guardados.
     */
    public List<ContactEntry> getContacts() {
        String json = prefs.getString(KEY_CONTACTS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<ContactEntry>>() {}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Persiste la lista completa de contactos.
     */
    public void saveContacts(List<ContactEntry> list) {
        String json = gson.toJson(list);
        prefs.edit().putString(KEY_CONTACTS, json).apply();
    }

    /**
     * Añade un contacto y persiste.
     */
    public void addContact(ContactEntry c) {
        List<ContactEntry> list = getContacts();
        list.add(c);
        saveContacts(list);
    }

    /**
     * Elimina un contacto y persiste.
     */
    public void removeContact(ContactEntry c) {
        List<ContactEntry> list = getContacts();
        list.remove(c);
        saveContacts(list);
    }

    /**
     * Devuelve la lista de IDs remotos (contactUserId) que el servidor reconoce.
     */
    public List<Integer> getContactIds() {
        String json = prefs.getString(KEY_CONTACTS, null);
        if (json == null) return new ArrayList<>();

        List<Integer> ids = new ArrayList<>();
        JsonArray array = JsonParser.parseString(json).getAsJsonArray();
        for (JsonElement el : array) {
            JsonObject obj = el.getAsJsonObject();
            if (obj.has("contactUserId") && !obj.get("contactUserId").isJsonNull()) {
                ids.add(obj.get("contactUserId").getAsInt());
            } else if (obj.has("contact_user_id") && !obj.get("contact_user_id").isJsonNull()) {
                ids.add(obj.get("contact_user_id").getAsInt());
            } else if (obj.has("id") && !obj.get("id").isJsonNull()) {
                ids.add(obj.get("id").getAsInt());
            }
        }
        return ids;
    }
}
