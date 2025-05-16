package com.example.appGrabacion.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenericListActivity extends AppCompatActivity {
    public static final String EXTRA_TYPE = "type";

    private RecyclerView rv;
    private GenericActivityService service;
    private final Set<String> loadedUrls = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic);

        String type = getIntent().getStringExtra(EXTRA_TYPE);
        if (type == null) type = "servicios";

        ImageView bg = findViewById(R.id.videoBackground);
        bg.setImageResource(type.equals("entidades")
                ? R.drawable.entidades
                : R.drawable.servicios);

        rv = findViewById(R.id.rvItems);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // scroll dinámico idéntico a FolderActivity
        FrameLayout wrapper = findViewById(R.id.card_wrapper);
        ConstraintLayout.LayoutParams params =
                (ConstraintLayout.LayoutParams) wrapper.getLayoutParams();
        final int initialMarginTop = params.topMargin;
        bg.post(() -> {
            int overlapPx = Math.round(
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            40,
                            getResources().getDisplayMetrics()
                    )
            );
            int maxScroll = bg.getHeight() - overlapPx;
            rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                int accDy = 0;
                @Override public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                    super.onScrolled(rv, dx, dy);
                    accDy = Math.max(0, Math.min(accDy + dy, maxScroll));
                    params.topMargin = initialMarginTop - accDy;
                    wrapper.setLayoutParams(params);
                }
            });
        });

        service = new GenericActivityService(this);

        if (type.equals("entidades")) {
            setupEntidadesGrid();
        } else if (type.equals("servicios")) {
            setupServiciosGrid();
        } else {
            setupServiciosPorCategoriaGrid(type);
        }
    }

    private CircularProgressDrawable makeLoader() {
        CircularProgressDrawable l = new CircularProgressDrawable(this);
        l.setStrokeWidth(5f);
        l.setCenterRadius(30f);
        l.start();
        return l;
    }

    private void setupEntidadesGrid() {
        // DiffUtil para Entidad
        DiffUtil.ItemCallback<Entidad> diff = new DiffUtil.ItemCallback<Entidad>() {
            @Override public boolean areItemsTheSame(@NonNull Entidad a, @NonNull Entidad b) {
                return a.getIdEntidad() == b.getIdEntidad();
            }
            @Override public boolean areContentsTheSame(@NonNull Entidad a, @NonNull Entidad b) {
                return a.equals(b);
            }
        };

        // Binder con spinner y caché de URLs cargadas
        ImageGridAdapter.Binder<Entidad> binder = (iv, e) -> {
            String url = e.getImagen();
            RequestCreator req = Picasso.get()
                    .load(url)
                    .fit()
                    .centerCrop();
            if (loadedUrls.contains(url)) {
                req.into(iv);
            } else {
                req.placeholder(makeLoader())
                        .into(iv, new Callback() {
                            @Override public void onSuccess() {
                                loadedUrls.add(url);
                            }
                            @Override public void onError(Exception ex) { }
                        });
            }
        };

        // Click para detalle de entidad
        ImageGridAdapter.OnItemClickListener<Entidad> listener = e -> {
            Intent i = new Intent(this, EntidadDetailActivity.class);
            i.putExtra("id_entidad", e.getIdEntidad());
            startActivity(i);
        };

        // Crear adapter y asignar
        ImageGridAdapter<Entidad> grid = new ImageGridAdapter<>(diff, binder, listener);
        rv.setAdapter(grid);

        // Cargar entidades y reordenar para poner la que tiene el email en primer lugar
        service.loadEntidades(new GenericActivityService.LoadCallback<Entidad>() {
            @Override public void onSuccess(List<Entidad> items) {
                // Buscar la entidad con email casamujer@zaragoza.es
                List<Entidad> reordered = new ArrayList<>(items);
                for (int i = 0; i < reordered.size(); i++) {
                    Entidad e = reordered.get(i);
                    if ("casamujer@zaragoza.es".equalsIgnoreCase(e.getEmail())) {
                        // mover a índice 0
                        reordered.remove(i);
                        reordered.add(0, e);
                        break;
                    }
                }
                runOnUiThread(() -> grid.submitList(reordered));
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
        // idéntico al anterior, pero sin reordenamiento
        DiffUtil.ItemCallback<Recurso> diff = new DiffUtil.ItemCallback<Recurso>() {
            @Override public boolean areItemsTheSame(@NonNull Recurso a, @NonNull Recurso b) {
                return a.getId() == b.getId();
            }
            @Override public boolean areContentsTheSame(@NonNull Recurso a, @NonNull Recurso b) {
                return a.equals(b);
            }
        };
        ImageGridAdapter.Binder<Recurso> binder = (iv, r) -> {
            String url = r.getImagen();
            RequestCreator req = Picasso.get().load(url).fit().centerCrop();
            if (loadedUrls.contains(url)) {
                req.into(iv);
            } else {
                req.placeholder(makeLoader())
                        .into(iv, new Callback() {
                            @Override public void onSuccess() {
                                loadedUrls.add(url);
                            }
                            @Override public void onError(Exception ex) { }
                        });
            }
        };
        ImageGridAdapter.OnItemClickListener<Recurso> listener = r -> {
            Intent i = new Intent(this, RecursoDetailActivity.class);
            i.putExtra("id_recurso", r.getId());
            startActivity(i);
        };
        ImageGridAdapter<Recurso> grid = new ImageGridAdapter<>(diff, binder, listener);
        rv.setAdapter(grid);
        service.loadServicios(new GenericActivityService.LoadCallback<Recurso>() {
            @Override public void onSuccess(List<Recurso> items) {
                Collections.shuffle(items);
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
        // idéntico a setupServiciosGrid(), cambiando sólo la llamada al servicio
        DiffUtil.ItemCallback<Recurso> diff = new DiffUtil.ItemCallback<Recurso>() {
            @Override public boolean areItemsTheSame(@NonNull Recurso a, @NonNull Recurso b) {
                return a.getId() == b.getId();
            }
            @Override public boolean areContentsTheSame(@NonNull Recurso a, @NonNull Recurso b) {
                return a.equals(b);
            }
        };
        ImageGridAdapter.Binder<Recurso> binder = (iv, r) -> {
            String url = r.getImagen();
            RequestCreator req = Picasso.get().load(url).fit().centerCrop();
            if (loadedUrls.contains(url)) {
                req.into(iv);
            } else {
                req.placeholder(makeLoader())
                        .into(iv, new Callback() {
                            @Override public void onSuccess() {
                                loadedUrls.add(url);
                            }
                            @Override public void onError(Exception ex) { }
                        });
            }
        };
        ImageGridAdapter.OnItemClickListener<Recurso> listener = r -> {
            Intent i = new Intent(this, RecursoDetailActivity.class);
            i.putExtra("id_recurso", r.getId());
            startActivity(i);
        };
        ImageGridAdapter<Recurso> grid = new ImageGridAdapter<>(diff, binder, listener);
        rv.setAdapter(grid);
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
