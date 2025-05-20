package com.example.appGrabacion.contracts;

import com.example.appGrabacion.models.ContactEntry;
import java.util.List;

public interface ContactsContract {
    interface View {
        void showLoading();
        void hideLoading();
        void showContacts(List<ContactEntry> contacts);
        void showAddContactSuccess();
        void showDeleteContactSuccess();
        void showError(String message);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();

        void loadContacts();
        void addContact(String name, String email);
        void deleteContact(int contactId);
    }

    interface Service {
        interface Callback<T> {
            void onSuccess(T result);
            void onError(Throwable t);
        }

        void loadContacts(Callback<List<ContactEntry>> callback);
        void addContact(ContactEntry entry, Callback<ContactEntry> callback);
        void deleteContact(int contactId, Callback<Void> callback);
    }
}
