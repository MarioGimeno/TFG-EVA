// com/example/appGrabacion/screens/EntidadesActivity.java
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
import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.utils.EntityService;

import java.util.List;

public class EntidadesActivity extends AppCompatActivity {
    private static final String TAG = "EntidadesActivity";

    @Override
    protected void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_entidades);

        RecyclerView rv = findViewById(R.id.rvEntidades);
        rv.setLayoutManager(new LinearLayoutManager(this));

        EntidadesAdapter adapter = new EntidadesAdapter(ent -> {
            Intent i = new Intent(this, EntidadDetailActivity.class);
            i.putExtra("id_entidad", ent.getIdEntidad());
            startActivity(i);
        });
        rv.setAdapter(adapter);

        new EntityService(this).fetchAll(new EntityService.EntityCallback() {
            @Override public void onSuccess(List<Entidad> list) {
                adapter.submitList(list);
            }
            @Override public void onError(Throwable t) {
                Log.e(TAG, "Error al cargar", t);
                Toast.makeText(EntidadesActivity.this,
                        "Error: "+t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
