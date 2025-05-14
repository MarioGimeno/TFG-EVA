// EntidadesActivity.java
package com.example.appGrabacion.screens;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.EntidadesAdapter;
import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.utils.EntityService;
import com.example.appGrabacion.utils.EntityService.EntityCallback;

import java.util.List;

public class EntidadesActivity extends AppCompatActivity {
    private static final String TAG = "EntidadesActivity";
    private RecyclerView rvEntidades;
    private EntidadesAdapter adapter;  // Asume que has creado un adapter para mostrar Entidad

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entidades);

        // 1) Inicializar RecyclerView y Adapter
        rvEntidades = findViewById(R.id.rvEntidades);
        rvEntidades.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EntidadesAdapter();
        rvEntidades.setAdapter(adapter);

        // 2) Llamar al servicio
        EntityService service = new EntityService(this);
        service.fetchAll(new EntityCallback() {
            @Override
            public void onSuccess(List<Entidad> entidades) {
                Log.d(TAG, "Entidades cargadas: " + entidades.size());
                // 3) Actualizar el adapter con la lista recibida
                adapter.submitList(entidades);
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Error al cargar entidades", t);
                Toast.makeText(
                        EntidadesActivity.this,
                        "Error al cargar entidades: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
