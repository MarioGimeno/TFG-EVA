
// com/example/appGrabacion/screens/CategoriaDetailActivity.java

package com.example.appGrabacion.screens;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.CategoriasRecursosAdapter;
import com.example.appGrabacion.models.Categoria;
import com.example.appGrabacion.models.Recurso;
import com.example.appGrabacion.services.ResourceService;
import com.example.appGrabacion.services.CategoriaService;


import java.util.ArrayList;
import java.util.List;

public class CategoriaDetailActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView tvCatTitle;
    private RecyclerView rvRecursos;
    private CategoriasRecursosAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categoria_detail);


        btnBack    = findViewById(R.id.btnBack);
        tvCatTitle = findViewById(R.id.tvCatTitle);
        rvRecursos = findViewById(R.id.rvRecursos);


        btnBack.setOnClickListener(v -> finish());

        adapter = new CategoriasRecursosAdapter();
        rvRecursos.setLayoutManager(new LinearLayoutManager(this));
        rvRecursos.setAdapter(adapter);

        int idCat = getIntent().getIntExtra("id_categoria", -1);
        if (idCat < 0) {
            Toast.makeText(this, "Categoría inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        // Cargamos nombre de la categoría

        new CategoriaService(this).fetchById(idCat, new CategoriaService.CategoriaDetailCallback() {
            @Override
            public void onSuccess(Categoria c) {
                tvCatTitle.setText(c.getNombre());


                // Ahora sí cargamos solo los recursos de esta categoría
                new ResourceService(CategoriaDetailActivity.this)
                        .fetchByCategoria(idCat, new ResourceService.ResourceCallback() {
                            @Override
                            public void onSuccess(List<Recurso> list) {
                                adapter.submitList(list);
                            }
                            @Override
                            public void onError(Throwable t) {
                                Toast.makeText(CategoriaDetailActivity.this,
                                        "Error al cargar recursos: " + t.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

            }
            @Override
            public void onError(Throwable t) {
                Toast.makeText(CategoriaDetailActivity.this,
                        "Error al cargar categoría: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }




    private void cargarRecursos(int idCat) {
        new ResourceService(this).fetchAll(new ResourceService.ResourceCallback() {
            @Override public void onSuccess(List<Recurso> list) {
                List<Recurso> filtrados = new ArrayList<>();
                for (Recurso r : list) {
                    if (r.getIdCategoria() == idCat) filtrados.add(r);
                }
                adapter.submitList(filtrados);
            }
            @Override public void onError(Throwable t) {
                Toast.makeText(CategoriaDetailActivity.this,
                        "Error al cargar recursos: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
    


