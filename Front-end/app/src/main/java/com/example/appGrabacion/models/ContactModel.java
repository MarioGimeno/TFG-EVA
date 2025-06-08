package com.example.appGrabacion.models;

import android.content.Context;
import android.util.Log;

import com.example.appGrabacion.entities.ContactEntry;
import com.example.appGrabacion.utils.ContactsApi;
import com.example.appGrabacion.utils.RetrofitClient;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;  // <-- retrofit Callback
import retrofit2.Response;

public class ContactModel {
    private final ContactsApi api;
    private final String token;

    public interface ContactCallback<T> {
        void onSuccess(T result);
        void onError(Throwable t);
    }

    public ContactModel(Context context, String token) {
        api = RetrofitClient.getRetrofitInstance(context).create(ContactsApi.class);
        this.token = token;
    }

    public void loadContacts(ContactCallback<List<ContactEntry>> callback) {
        api.getContacts(token).enqueue(new Callback<List<ContactEntry>>() { // retrofit callback
            @Override
            public void onResponse(Call<List<ContactEntry>> call, Response<List<ContactEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ContactEntry> contacts = response.body();

                    // Invertimos el orden de la lista
                    Collections.reverse(contacts);

                    callback.onSuccess(contacts);                } else {
                    callback.onError(new Exception("Error cargando contactos: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<List<ContactEntry>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
    public void addContact(ContactEntry entry, ContactCallback<ContactEntry> callback) {
        api.addContact(token, entry).enqueue(new Callback<ContactEntry>() {
            @Override
            public void onResponse(Call<ContactEntry> call, Response<ContactEntry> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        String message = jsonObject.optString("message", "");

                        if (message.contains("pk_usuario_contacto")) {
                            // Violación de clave única → usuario ya agregado
                            callback.onError(new Exception("Este usuario ya está agregado."));
                        }
                        else if (message.contains("chk_no_self_contact")) {
                            // Violación de check → intento de agregarse a sí mismo
                            callback.onError(new Exception("No puedes agregarte a ti mismo."));
                        }
                        else {
                            // Cualquier otro error
                            callback.onError(new Exception("Error al agregar un contacto. Inténtalo de nuevo."));
                        }
                    } catch (Exception e) {
                        callback.onError(new Exception("Error desconocido al procesar la respuesta."));
                    }
                }
            }

            @Override
            public void onFailure(Call<ContactEntry> call, Throwable t) {
                callback.onError(t);
            }
        });
    }





    public void deleteContact(int contactId, ContactCallback<Void> callback) {
        api.deleteContact(token, contactId).enqueue(new Callback<Void>() { // retrofit callback
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(new Exception("Error borrando: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
