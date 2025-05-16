package com.example.appGrabacion.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.example.appGrabacion.MainActivity;
import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.ImageGridAdapter;
import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.models.Recurso;
import com.example.appGrabacion.services.GenericActivityService;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GenericListActivity extends AppCompatActivity {
    public static final String EXTRA_TYPE = "type"; // "entidades", "servicios" o nombre de categoría

    private String type;
    private RecyclerView rv;
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

        // 3) RecyclerView en grid de 2 columnas
        rv = findViewById(R.id.rvItems);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        // 4) Botón atrás
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // 5) Inicializa servicio genérico
        service = new GenericActivityService(this);

        // 6) Carga datos
        if (type.equals("entidades")) {
            setupEntidadesGrid();
        } else if (type.equals("servicios")) {
            setupServiciosGrid();
        } else {
            setupServiciosPorCategoriaGrid(type);
        }
    }

    /**
     * Crea un loader circular para usar como placeholder
     */
    private CircularProgressDrawable makeLoader() {
        CircularProgressDrawable loader = new CircularProgressDrawable(this);
        loader.setStrokeWidth(5f);
        loader.setCenterRadius(30f);
        loader.start();
        return loader;
    }

    private void setupEntidadesGrid() {
        // 1) DiffUtil
        DiffUtil.ItemCallback<Entidad> diffCallback = new DiffUtil.ItemCallback<Entidad>() {
            @Override public boolean areItemsTheSame(@NonNull Entidad a, @NonNull Entidad b) {
                return a.getIdEntidad() == b.getIdEntidad();
            }
            @Override public boolean areContentsTheSame(@NonNull Entidad a, @NonNull Entidad b) {
                return a.equals(b);
            }
        };

        // 2) Binder
        ImageGridAdapter.Binder<Entidad> binder = new ImageGridAdapter.Binder<Entidad>() {
            @Override
            public void bind(ImageView imageView, Entidad item) {
                Picasso.get()
                        .load(item.getImagen())
                        .placeholder(makeLoader())
                        .error(R.drawable.eva)
                        .into(imageView);
            }
        };

        // 3) Listener
        ImageGridAdapter.OnItemClickListener<Entidad> listener =
                new ImageGridAdapter.OnItemClickListener<Entidad>() {
                    @Override public void onItemClick(Entidad entidad) {
                        Intent i = new Intent(GenericListActivity.this, EntidadDetailActivity.class);
                        i.putExtra("id_entidad", entidad.getIdEntidad());
                        startActivity(i);
                    }
                };

        // 4) Adapter
        ImageGridAdapter<Entidad> grid =
                new ImageGridAdapter<>(diffCallback, binder, listener);
        rv.setAdapter(grid);

        // 5) Load
        service.loadEntidades(new GenericActivityService.LoadCallback<Entidad>() {
            @Override public void onSuccess(List<Entidad> items) {
                runOnUiThread(() -> grid.submitList(items));
            }
            @Override public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(GenericListActivity.this,
                                "Error cargando entidades: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void setupServiciosGrid() {
        // 1) DiffUtil
        DiffUtil.ItemCallback<Recurso> diffCallback = new DiffUtil.ItemCallback<Recurso>() {
            @Override public boolean areItemsTheSame(@NonNull Recurso a, @NonNull Recurso b) {
                return a.getId() == b.getId();
            }
            @Override public boolean areContentsTheSame(@NonNull Recurso a, @NonNull Recurso b) {
                return a.equals(b);
            }
        };

        // 2) Binder
        ImageGridAdapter.Binder<Recurso> binder = new ImageGridAdapter.Binder<Recurso>() {
            @Override
            public void bind(ImageView imageView, Recurso item) {
                Picasso.get()
                        .load(item.getImagen())
                        .placeholder(makeLoader())
                        .error(R.drawable.eva)
                        .into(imageView);
            }
        };

        // 3) Listener
        ImageGridAdapter.OnItemClickListener<Recurso> listener =
                new ImageGridAdapter.OnItemClickListener<Recurso>() {
                    @Override public void onItemClick(Recurso recurso) {
                        Intent i = new Intent(GenericListActivity.this, RecursoDetailActivity.class);
                        i.putExtra("id_recurso", recurso.getId());
                        startActivity(i);
                    }
                };

        // 4) Adapter
        ImageGridAdapter<Recurso> grid =
                new ImageGridAdapter<>(diffCallback, binder, listener);
        rv.setAdapter(grid);

        // 5) Load
        service.loadServicios(new GenericActivityService.LoadCallback<Recurso>() {
            @Override public void onSuccess(List<Recurso> items) {
                runOnUiThread(() -> grid.submitList(items));
            }
            @Override public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(GenericListActivity.this,
                                "Error cargando servicios: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void setupServiciosPorCategoriaGrid(String categoria) {
        // 1) DiffUtil
        DiffUtil.ItemCallback<Recurso> diffCallback = new DiffUtil.ItemCallback<Recurso>() {
            @Override public boolean areItemsTheSame(@NonNull Recurso a, @NonNull Recurso b) {
                return a.getId() == b.getId();
            }
            @Override public boolean areContentsTheSame(@NonNull Recurso a, @NonNull Recurso b) {
                return a.equals(b);
            }
        };

        // 2) Binder
        ImageGridAdapter.Binder<Recurso> binder = new ImageGridAdapter.Binder<Recurso>() {
            @Override
            public void bind(ImageView imageView, Recurso item) {
                Picasso.get()
                        .load(item.getImagen())
                        .placeholder(makeLoader())
                        .error(R.drawable.eva)
                        .into(imageView);
            }
        };

        // 3) Listener
        ImageGridAdapter.OnItemClickListener<Recurso> listener =
                new ImageGridAdapter.OnItemClickListener<Recurso>() {
                    @Override public void onItemClick(Recurso recurso) {
                        Intent i = new Intent(GenericListActivity.this, RecursoDetailActivity.class);
                        i.putExtra("id_recurso", recurso.getId());
                        startActivity(i);
                    }
                };

        // 4) Adapter
        ImageGridAdapter<Recurso> grid =
                new ImageGridAdapter<>(diffCallback, binder, listener);
        rv.setAdapter(grid);

        // 5) Load
        service.loadServiciosPorCategoria(categoria, new GenericActivityService.LoadCallback<Recurso>() {
            @Override public void onSuccess(List<Recurso> items) {
                runOnUiThread(() -> grid.submitList(items));
            }
            @Override public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(GenericListActivity.this,
                                "Error cargando categoría \"" + categoria + "\": " + t.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
