// src/main/java/com/example/appGrabacion/activities/ContactsActivity.java
package com.example.appGrabacion.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.MainActivity;
import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.ContactsAdapter;
import com.example.appGrabacion.contracts.ContactsContract;
import com.example.appGrabacion.entities.ContactEntry;
import com.example.appGrabacion.presenters.ContactsPresenter;

import java.util.List;

public class ContactsActivity extends AppCompatActivity
        implements ContactsContract.View {

    private RecyclerView rv;
    private ContactsAdapter adapter;
    private ProgressBar progressHeader;
    private EditText etName, etPhone;
    private Button btnAdd;
    private ContactsPresenter presenter;
    private String token;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // UI refs
        rv       = findViewById(R.id.rvContacts);
        etName   = findViewById(R.id.etContactName);
        etPhone  = findViewById(R.id.etEmail);
        btnAdd   = findViewById(R.id.btnAddContact);
        progressHeader = findViewById(R.id.progressHeader);

        // RecyclerView setup
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter(contactId -> confirmDelete(contactId));
        rv.setAdapter(adapter);

        // token from prefs
        token = "Bearer " + getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("auth_token", "");

        // Presenter setup
        presenter = new ContactsPresenter(this, token);
        presenter.attachView(this);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // Add contact
        btnAdd.setOnClickListener(v -> {
            String name  = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Nombre y email son obligatorios", Toast.LENGTH_SHORT).show();
            } else {
                presenter.addContact(name, phone);
            }
        });

        // Load initial list
        presenter.loadContacts();
    }

    private void confirmDelete(int contactId) {
        new AlertDialog.Builder(this)
                .setTitle("Borrar contacto")
                .setMessage("¿Seguro que quieres borrarlo?")
                .setPositiveButton("Sí", (d,w) -> presenter.deleteContact(contactId))
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    // --- ContactsContract.View methods ---

    @Override
    public void showLoading() {
        progressHeader.setVisibility(View.VISIBLE);
        progressHeader.setAlpha(0f);
        progressHeader.animate().alpha(1f).setDuration(300).start();
    }

    @Override
    public void hideLoading() {
        progressHeader.animate()
                .alpha(0f).setDuration(300)
                .withEndAction(() -> progressHeader.setVisibility(View.GONE))
                .start();
    }


    @Override
    public void showContacts(List<ContactEntry> contacts) {
        adapter.setContacts(contacts);
    }

    @Override
    public void showAddContactSuccess() {
        etName.setText("");
        etPhone.setText("");
        Toast.makeText(this, "Contacto añadido", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showDeleteContactSuccess() {
        Toast.makeText(this, "Contacto borrado", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
