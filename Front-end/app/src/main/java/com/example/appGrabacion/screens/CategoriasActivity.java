package com.example.appGrabacion.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.CategoriasAdapter;
import com.example.appGrabacion.models.Categoria;
import com.example.appGrabacion.services.CategoriaService;

import java.util.List;

public class CategoriasActivity extends AppCompatActivity {
    private static final String TAG = "CategoriasActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate invoked");
        setContentView(R.layout.activity_categorias);


        RecyclerView rv = findViewById(R.id.rvCategorias);
        rv.setNestedScrollingEnabled(false);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        CategoriasAdapter adapter = new CategoriasAdapter(cat -> {
            Intent i = new Intent(this, GenericListActivity.class);
            i.putExtra(GenericListActivity.EXTRA_CATEGORY_ID, cat.getIdCategoria());
            startActivity(i);
        });
        rv.setAdapter(adapter);
        // REFERENCIAS A LAS CARDS
        View cardTodos      = findViewById(R.id.cardTodos);
        View cardEntidades  = findViewById(R.id.cardEntidades);
        View cardGratuitos  = findViewById(R.id.cardGratuitos);
        View cardAccesibles = findViewById(R.id.cardAccesibles);

        // LES ASIGNAMOS ICONO + TEXTO
        setupFilter(cardTodos, R.drawable.ic_todos,       "Todos",      () -> {
            Intent i = new Intent(this, GenericListActivity.class);
            i.putExtra(GenericListActivity.EXTRA_LIST_TYPE, "servicios");
            startActivity(i);
        });
        setupFilter(cardEntidades, R.drawable.ic_entidades, "Entidades",  () -> {
            Intent i = new Intent(this, GenericListActivity.class);
            i.putExtra(GenericListActivity.EXTRA_LIST_TYPE, "entidades");
            startActivity(i);
        });
        setupFilter(cardGratuitos, R.drawable.ic_gratuito,  "Gratuitos",  () -> {
            Intent i = new Intent(this, GenericListActivity.class);
            i.putExtra(GenericListActivity.EXTRA_LIST_TYPE, "gratuitos");
            startActivity(i);
        });
        setupFilter(cardAccesibles, R.drawable.ic_auditivos,"Accesibles", () -> {
            Intent i = new Intent(this, GenericListActivity.class);
            i.putExtra(GenericListActivity.EXTRA_LIST_TYPE, "accesibles");
            startActivity(i);
        });


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
    private void setupFilter(View card, @DrawableRes int iconRes,String label, Runnable onClick) {
        ImageView iv = card.findViewById(R.id.ivFilterIcon);
        TextView tv  = card.findViewById(R.id.tvFilterLabel);
        iv.setImageResource(iconRes);
        tv.setText(label);
        card.setOnClickListener(v -> onClick.run());
    }
}
