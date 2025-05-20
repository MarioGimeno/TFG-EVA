package com.example.appGrabacion.presenters;

import android.content.Context;

import com.example.appGrabacion.contracts.ContactsContract;
import com.example.appGrabacion.models.ContactEntry;
import com.example.appGrabacion.services.ContactModel;

import java.util.List;

public class ContactsPresenter implements ContactsContract.Presenter {
    private ContactsContract.View view;
    private final ContactModel service;

    public ContactsPresenter(Context ctx, String token) {
        this.service = new ContactModel(ctx, token);
    }

    @Override
    public void attachView(ContactsContract.View view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadContacts() {
        if (view == null) return;
        view.showLoading();

        service.loadContacts(new ContactModel.ContactCallback<List<ContactEntry>>() {
            @Override
            public void onSuccess(List<ContactEntry> result) {
                if (view == null) return;
                view.hideLoading();
                view.showContacts(result);
            }

            @Override
            public void onError(Throwable t) {
                if (view == null) return;
                view.hideLoading();
                view.showError(t.getMessage());
            }
        });
    }

    @Override
    public void addContact(String name, String email) {
        if (view == null) return;
        view.showLoading();

        ContactEntry newEntry = new ContactEntry(0, 0, name, email);
        service.addContact(newEntry, new ContactModel.ContactCallback<ContactEntry>() {
            @Override
            public void onSuccess(ContactEntry result) {
                if (view == null) return;
                view.hideLoading();
                view.showAddContactSuccess();
                loadContacts();
            }

            @Override
            public void onError(Throwable t) {
                if (view == null) return;
                view.hideLoading();
                view.showError(t.getMessage());
            }
        });
    }

    @Override
    public void deleteContact(int contactId) {
        if (view == null) return;
        view.showLoading();

        service.deleteContact(contactId, new ContactModel.ContactCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (view == null) return;
                view.hideLoading();
                view.showDeleteContactSuccess();
                loadContacts();
            }

            @Override
            public void onError(Throwable t) {
                if (view == null) return;
                view.hideLoading();
                view.showError(t.getMessage());
            }
        });
    }
}
