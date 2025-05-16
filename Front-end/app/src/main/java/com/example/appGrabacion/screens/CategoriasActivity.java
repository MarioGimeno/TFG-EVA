
package com.example.appGrabacion.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.CategoriasAdapter;
import com.example.appGrabacion.models.Categoria;
import com.example.appGrabacion.utils.CategoriaService;

import java.util.List;

public class CategoriasActivity extends AppCompatActivity {
    private static final String TAG = "CategoriasActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate invoked");

        setContentView(R.layout.activity_categorias);

        RecyclerView rv = findViewById(R.id.rvCategorias);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        CategoriasAdapter adapter = new CategoriasAdapter(cat -> {

            // Lanzar detalle directamente con el ID
            Intent i = new Intent(CategoriasActivity.this, CategoriaDetailActivity.class);
            i.putExtra("id_categoria", cat.getIdCategoria());
            startActivity(i);
        });
        rv.setAdapter(adapter);

        new CategoriaService(this).fetchAll(new CategoriaService.CategoriaCallback() {
            @Override public void onSuccess(List<Categoria> list) {
                Log.d(TAG, "onSuccess: fetched " + list.size());

                adapter.submitList(list);
            }
            @Override public void onError(Throwable t) {
                Log.e(TAG, "Error al cargar categorías", t);
                Toast.makeText(CategoriasActivity.this,

                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

