// com/example/appGrabacion/activities/EntidadesActivity.java
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
import com.example.appGrabacion.adapters.EntidadesAdapter;
import com.example.appGrabacion.contracts.EntidadesContract;
import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.presenters.EntidadesPresenter;
import com.example.appGrabacion.services.EntityModel;

import java.util.List;

public class EntidadesActivity extends AppCompatActivity implements EntidadesContract.View {
    private static final String TAG = "EntidadesActivity";

    private EntidadesPresenter presenter;
    private EntidadesAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_entidades);

        RecyclerView rv = findViewById(R.id.rvEntidades);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EntidadesAdapter(ent -> {
            Intent i = new Intent(this, EntidadDetailActivity.class);
            i.putExtra("id_entidad", ent.getIdEntidad());
            startActivity(i);
        });
        rv.setAdapter(adapter);

        presenter = new EntidadesPresenter(new EntityModel(this));
        presenter.attachView(this);
        presenter.loadEntidades();
    }

    @Override
    public void showLoading() {
        // Opcional: mostrar progress bar
    }

    @Override
    public void hideLoading() {
        // Opcional: ocultar progress bar
    }

    @Override
    public void showEntidades(List<Entidad> entidades) {
        adapter.submitList(entidades);
    }

    @Override
    public void showError(String message) {
        Log.e(TAG, "Error al cargar entidades: " + message);
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }
}
