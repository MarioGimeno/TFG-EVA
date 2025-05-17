// 2) ContactsActivity.java
package com.example.appGrabacion.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.MainActivity;
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

        // 1) Botón de vuelta a Main
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // UI refs
        rv       = findViewById(R.id.rvContacts);
        progress = findViewById(R.id.progressContacts);
        etName   = findViewById(R.id.etContactName);
        etPhone  = findViewById(R.id.etEmail);
        btnAdd   = findViewById(R.id.btnAddContact);

        // RecyclerView setup con auto-measure dentro de ScrollView
        LinearLayoutManager lm = new LinearLayoutManager(this) {
            @Override public boolean isAutoMeasureEnabled() {
                return true;
            }
        };
        rv.setLayoutManager(lm);
        rv.setHasFixedSize(false);
        rv.setNestedScrollingEnabled(false);

        adapter = new ContactsAdapter();
        rv.setAdapter(adapter);
        NestedScrollView scrollAll = findViewById(R.id.scrollAll);
        ImageView header        = findViewById(R.id.imgHeader);
        FrameLayout wrapper     = findViewById(R.id.card_wrapper);
        ConstraintLayout.LayoutParams params =
                (ConstraintLayout.LayoutParams) wrapper.getLayoutParams();
        final int initialMarginTop = params.topMargin;

// ejecutamos cuando header ya ha medido su altura
        header.post(() -> {
            int overlapPx = Math.round(
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            40,
                            getResources().getDisplayMetrics()
                    )
            );
            int maxScroll = header.getHeight() - overlapPx;

            scrollAll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldX, oldY) -> {
                int dy = Math.max(0, Math.min(scrollY, maxScroll));
                params.topMargin = initialMarginTop - dy;
                wrapper.setLayoutParams(params);
            });
        });

        // Eliminado el bloque de scroll dinámico sobre el header

        // Retrofit API
        api = RetrofitClient.getRetrofitInstance(this).create(ContactsApi.class);
        token = "Bearer " + getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("auth_token", "");

        btnAdd.setOnClickListener(v -> addContact());

        loadContacts();
    }

    private void loadContacts() {
        progress.setVisibility(ProgressBar.VISIBLE);
        api.getContacts(token).enqueue(new Callback<List<ContactEntry>>() {
            @Override
            public void onResponse(Call<List<ContactEntry>> call, Response<List<ContactEntry>> res) {
                progress.setVisibility(ProgressBar.GONE);
                if (res.isSuccessful()) {
                    adapter.setContacts(res.body());
                } else {
                    Toast.makeText(ContactsActivity.this, "Error cargando contactos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ContactEntry>> call, Throwable t) {
                progress.setVisibility(ProgressBar.GONE);
                Toast.makeText(ContactsActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addContact() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Nombre y email son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        ContactEntry c = new ContactEntry(0, 0, name, phone);
        progress.setVisibility(ProgressBar.VISIBLE);
        api.addContact(token, c).enqueue(new Callback<ContactEntry>() {
            @Override
            public void onResponse(Call<ContactEntry> call, Response<ContactEntry> res) {
                progress.setVisibility(ProgressBar.GONE);
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
                progress.setVisibility(ProgressBar.GONE);
                Toast.makeText(ContactsActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
