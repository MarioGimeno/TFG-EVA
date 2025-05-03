
// 2) ContactsActivity.java
package com.example.appGrabacion.screens;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.ContactsAdapter;
import com.example.appGrabacion.models.ContactEntry;
import com.example.appGrabacion.utils.ContactsApi;
import com.example.appGrabacion.utils.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactsActivity extends AppCompatActivity {
    private RecyclerView rv;
    private ContactsAdapter adapter;
    private ProgressBar progress;
    private EditText etName, etPhone;
    private Button btnAdd;
    private ContactsApi api;
    private String token;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // UI refs
        rv = findViewById(R.id.rvContacts);
        progress = findViewById(R.id.progressContacts);
        etName = findViewById(R.id.etContactName);
        etPhone = findViewById(R.id.etEmail);
        btnAdd = findViewById(R.id.btnAddContact);

        // RecyclerView setup
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter();
        rv.setAdapter(adapter);

        // Retrofit API
        api = RetrofitClient.getRetrofitInstance(this).create(ContactsApi.class);
        token = "Bearer " + getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("auth_token", "");

        btnAdd.setOnClickListener(v -> addContact());

        loadContacts();
    }

    private void loadContacts() {
        progress.setVisibility(View.VISIBLE);
        api.getContacts(token).enqueue(new Callback<List<ContactEntry>>() {
            @Override
            public void onResponse(Call<List<ContactEntry>> call, Response<List<ContactEntry>> res) {
                progress.setVisibility(View.GONE);
                if (res.isSuccessful()) {
                    adapter.setContacts(res.body());
                } else
                    Toast.makeText(ContactsActivity.this, "Error cargando contactos", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<ContactEntry>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ContactsActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addContact() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Nombre y teléfono son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Usamos el constructor de cuatro parámetros, dejando los IDs a 0
        ContactEntry c = new ContactEntry(0, 0, name, phone);

        progress.setVisibility(View.VISIBLE);
        api.addContact(token, c).enqueue(new Callback<ContactEntry>() {
            @Override
            public void onResponse(Call<ContactEntry> call, Response<ContactEntry> res) {
                progress.setVisibility(View.GONE);
                if (res.isSuccessful()) {
                    etName.setText("");
                    etPhone.setText("");
                    loadContacts();
                } else {
                    Toast.makeText(ContactsActivity.this, "Error guardando", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ContactEntry> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ContactsActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

