package com.example.appGrabacion.services;

import android.content.Context;

import com.example.appGrabacion.models.ContactEntry;
import com.example.appGrabacion.utils.ContactsApi;
import com.example.appGrabacion.utils.RetrofitClient;

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
                    callback.onSuccess(response.body());
                } else {
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
        api.addContact(token, entry).enqueue(new Callback<ContactEntry>() { // retrofit callback
            @Override
            public void onResponse(Call<ContactEntry> call, Response<ContactEntry> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("Error guardando: " + response.code()));
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
