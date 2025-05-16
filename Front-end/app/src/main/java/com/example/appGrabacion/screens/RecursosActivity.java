package com.example.appGrabacion.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.RecursosAdapter;
import com.example.appGrabacion.models.Recurso;
import com.example.appGrabacion.services.ResourceService;
import com.example.appGrabacion.services.ResourceService.ResourceCallback;

import java.util.List;

public class RecursosActivity extends AppCompatActivity {
    private static final String TAG = "RecursosActivity";
    private RecyclerView rvRecursos;
    private RecursosAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recursos);

        // 1) Configurar RecyclerView y Adapter
        rvRecursos = findViewById(R.id.rvRecursos);
        rvRecursos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecursosAdapter(recurso -> {
            Intent i = new Intent(RecursosActivity.this, RecursoDetailActivity.class);
            i.putExtra("id_recurso", recurso.getId());
            startActivity(i);
        });        rvRecursos.setAdapter(adapter);

        // 2) Llamar al servicio para cargar recursos
        ResourceService service = new ResourceService(this);
        service.fetchAll(new ResourceCallback() {
            @Override
            public void onSuccess(List<Recurso> lista) {
                Log.d(TAG, "Recursos cargados: " + lista.size());
                adapter.submitList(lista);
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Error al cargar recursos", t);
                Toast.makeText(
                        RecursosActivity.this,
                        "Error al cargar recursos: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
