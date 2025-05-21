// src/main/java/com/example/appGrabacion/activities/CategoriaDetailActivity.java
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
import com.example.appGrabacion.contracts.CategoriaDetailContract;
import com.example.appGrabacion.entities.Categoria;
import com.example.appGrabacion.entities.Recurso;
import com.example.appGrabacion.presenters.CategoriaDetailPresenter;
import com.example.appGrabacion.models.CategoriaModel;
import com.example.appGrabacion.models.ResourceModel;

import java.util.List;

public class CategoriaDetailActivity extends AppCompatActivity
        implements CategoriaDetailContract.View {

    private ImageButton btnBack;
    private TextView    tvCatTitle;
    private RecyclerView rvRecursos;
    private CategoriasRecursosAdapter adapter;
    private CategoriaDetailPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categoria_detail);

        btnBack    = findViewById(R.id.btnBack);
        tvCatTitle = findViewById(R.id.tvCatTitle);
        rvRecursos = findViewById(R.id.rvRecursos);
        adapter    = new CategoriasRecursosAdapter();

        rvRecursos.setLayoutManager(new LinearLayoutManager(this));
        rvRecursos.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        int idCat = getIntent().getIntExtra("id_categoria", -1);
        if (idCat < 0) {
            Toast.makeText(this, "Categoría inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar Presenter y cargar datos
        presenter = new CategoriaDetailPresenter(
                new CategoriaModel(this),
                new ResourceModel(this)
        );
        presenter.attachView(this);
        presenter.loadCategoryAndResources(idCat);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    // --- CategoriaDetailContract.View ---

    @Override
    public void showLoading() {
        // mostrar ProgressBar si aplica
    }

    @Override
    public void hideLoading() {
        // ocultar ProgressBar
    }

    @Override
    public void showCategory(Categoria categoria) {
        tvCatTitle.setText(categoria.getNombre());
    }

    @Override
    public void showResources(List<Recurso> recursos) {
        adapter.submitList(recursos);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
