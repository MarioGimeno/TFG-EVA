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
import com.example.appGrabacion.contracts.ResourceContract;
import com.example.appGrabacion.entities.Recurso;
import com.example.appGrabacion.presenters.ResourcePresenter;
import com.example.appGrabacion.models.ResourceModel;

import java.util.List;

public class RecursosActivity extends AppCompatActivity implements ResourceContract.View {

    private static final String TAG = "RecursosActivity";
    private RecyclerView rvRecursos;
    private RecursosAdapter adapter;
    private ResourceContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recursos);

        rvRecursos = findViewById(R.id.rvRecursos);
        rvRecursos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecursosAdapter(recurso -> {
            Intent i = new Intent(RecursosActivity.this, RecursoDetailActivity.class);
            i.putExtra("id_recurso", recurso.getId());
            startActivity(i);
        });
        rvRecursos.setAdapter(adapter);

        // Inicializar service y presenter
        ResourceModel service = new ResourceModel(this);
        presenter = new ResourcePresenter(service);
        presenter.attachView(this);

        presenter.loadAllResources();
    }

    @Override
    public void showLoading() {
        // Aquí podrías mostrar un ProgressBar si quieres
        Log.d(TAG, "Cargando recursos...");
    }

    @Override
    public void hideLoading() {
        // Ocultar ProgressBar aquí si lo implementas
        Log.d(TAG, "Carga finalizada");
    }

    @Override
    public void showResources(List<Recurso> recursos) {
        Log.d(TAG, "Recursos cargados: " + recursos.size());
        adapter.submitList(recursos);
    }

    @Override
    public void showResourceDetail(Recurso recurso) {
        // No se usa aquí, pero está en el contrato
    }

    @Override
    public void showError(String message) {
        Log.e(TAG, "Error al cargar recursos: " + message);
        Toast.makeText(this, "Error al cargar recursos: " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }
}
