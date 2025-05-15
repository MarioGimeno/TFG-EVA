package com.example.appGrabacion.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.MainActivity;
import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.EntidadesAdapter;
import com.example.appGrabacion.adapters.RecursosAdapter;
import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.models.Recurso;
import com.example.appGrabacion.services.GenericActivityService;

import java.util.List;

public class GenericListActivity extends AppCompatActivity {
    public static final String EXTRA_TYPE = "type"; // "entidades", "servicios" o nombre de categoría

    private String type;
    private RecyclerView rv;
    private EntidadesAdapter entidadesAdapter;
    private RecursosAdapter recursosAdapter;
    private GenericActivityService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic);

        // 1) Lee tipo
        type = getIntent().getStringExtra(EXTRA_TYPE);
        if (type == null) type = "servicios";

        // 2) Ajusta imagen de fondo
        ImageView bg = findViewById(R.id.videoBackground);
        bg.setImageResource(
                type.equals("entidades") ? R.drawable.entidades : R.drawable.servicios
        );

        // 3) RecyclerView
        rv = findViewById(R.id.rvItems);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // 4) Botón atrás
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // 5) Inicializa servicio genérico
        service = new GenericActivityService(this);

        // 6) Carga datos
        if (type.equals("entidades")) {
            setupEntidades();
        } else if (type.equals("servicios")) {
            setupServicios();
        } else {
            setupServiciosPorCategoria(type);
        }
    }

    private void setupEntidades() {
        entidadesAdapter = new EntidadesAdapter(e -> {
            // al hacer click en entidad
            Intent i = new Intent(this, EntidadDetailActivity.class);
            i.putExtra("id_entidad", e.getIdEntidad());
            startActivity(i);
        });
        rv.setAdapter(entidadesAdapter);

        service.loadEntidades(new GenericActivityService.LoadCallback<Entidad>() {
            @Override
            public void onSuccess(List<Entidad> items) {
                runOnUiThread(() -> entidadesAdapter.submitList(items));
            }
            @Override
            public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(GenericListActivity.this,
                                "Error cargando entidades: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
    private void setupServicios() {
        recursosAdapter = new RecursosAdapter(recurso -> {
            Intent i = new Intent(GenericListActivity.this, RecursoDetailActivity.class);
            i.putExtra("id_recurso", recurso.getId());
            startActivity(i);
        });
        rv.setAdapter(recursosAdapter);

        service.loadServicios(new GenericActivityService.LoadCallback<Recurso>() {
            @Override
            public void onSuccess(List<Recurso> items) {
                runOnUiThread(() -> recursosAdapter.submitList(items));
            }
            @Override
            public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(GenericListActivity.this,
                                "Error cargando servicios: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }


    private void setupServiciosPorCategoria(String categoria) {
        recursosAdapter = new RecursosAdapter(recurso -> {
            Intent i = new Intent(GenericListActivity.this, RecursoDetailActivity.class);
            i.putExtra("id_recurso", recurso.getId());
            startActivity(i);
        });        rv.setAdapter(recursosAdapter);

        service.loadServiciosPorCategoria(categoria, new GenericActivityService.LoadCallback<Recurso>() {
            @Override
            public void onSuccess(List<Recurso> items) {
                runOnUiThread(() -> recursosAdapter.submitList(items));
            }
            @Override
            public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(GenericListActivity.this,
                                "Error categoría " + categoria + ": " + t.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
