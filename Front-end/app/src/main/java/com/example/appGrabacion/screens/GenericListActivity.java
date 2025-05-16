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
    public static final String EXTRA_LIST_TYPE = "list_type";      // "entidades" or "servicios"
    public static final String EXTRA_CATEGORY_ID = "category_id";  // int for category
    public static final String EXTRA_TYPE = "type" ;

    private RecyclerView rv;
    private GenericActivityService service;
    private final Set<String> loadedUrls = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic);

        // Decide mode: category ID int, or list type string
        Intent intent = getIntent();
        boolean hasCategory = intent.hasExtra(EXTRA_CATEGORY_ID);
        String listType = intent.getStringExtra(EXTRA_LIST_TYPE);

        ImageView bg = findViewById(R.id.videoBackground);
        // Set background depending on mode
        if (hasCategory) {
            bg.setImageResource(R.drawable.servicios);
        } else if ("entidades".equalsIgnoreCase(listType)) {
            bg.setImageResource(R.drawable.entidades);
        } else {
            bg.setImageResource(R.drawable.servicios);
        }

        rv = findViewById(R.id.rvItems);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        service = new GenericActivityService(this);

        if (hasCategory) {
            int categoryId = intent.getIntExtra(EXTRA_CATEGORY_ID, -1);
            if (categoryId < 0) {
                Toast.makeText(this, "Categoría inválida", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            setupServiciosPorCategoriaGrid(categoryId);
        } else if ("entidades".equalsIgnoreCase(listType)) {
            setupEntidadesGrid();
        } else {
            setupServiciosGrid();
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
        DiffUtil.ItemCallback<Entidad> diff = new DiffUtil.ItemCallback<Entidad>() {
            @Override public boolean areItemsTheSame(@NonNull Entidad a, @NonNull Entidad b) {
                return a.getIdEntidad() == b.getIdEntidad();
            }
            @Override public boolean areContentsTheSame(@NonNull Entidad a, @NonNull Entidad b) {
                return a.equals(b);
            }
        };

        ImageGridAdapter.Binder<Entidad> binder = (iv, e) -> {
            String url = e.getImagen();
            RequestCreator req = Picasso.get().load(url).fit().centerCrop();
            if (loadedUrls.contains(url)) {
                req.into(iv);
            } else {
                req.placeholder(makeLoader())
                        .into(iv, new Callback() {
                            @Override public void onSuccess() { loadedUrls.add(url); }
                            @Override public void onError(Exception ex) { }
                        });
            }
        };

        ImageGridAdapter.OnItemClickListener<Entidad> listener = e -> {
            Intent i = new Intent(this, EntidadDetailActivity.class);
            i.putExtra("id_entidad", e.getIdEntidad());
            startActivity(i);
        };

        ImageGridAdapter<Entidad> grid = new ImageGridAdapter<>(diff, binder, listener);
        rv.setAdapter(grid);

        service.loadEntidades(new GenericActivityService.LoadCallback<Entidad>() {
            @Override public void onSuccess(List<Entidad> items) {
                List<Entidad> reordered = new ArrayList<>(items);
                // move special email to front
                for (int i = 0; i < reordered.size(); i++) {
                    Entidad e = reordered.get(i);
                    if ("casamujer@zaragoza.es".equalsIgnoreCase(e.getEmail())) {
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
        DiffUtil.ItemCallback<Recurso> diff = new DiffUtil.ItemCallback<Recurso>() {
            @Override public boolean areItemsTheSame(@NonNull Recurso a, @NonNull Recurso b) {
                return a.getId() == b.getId();
            }
            @Override public boolean areContentsTheSame(@NonNull Recurso a, @NonNull Recurso b) {
                return a.equals(b);
            }
        };
        ImageGridAdapter.Binder<Recurso> binder = getRecursoBinder();
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

    private void setupServiciosPorCategoriaGrid(int categoryId) {
        DiffUtil.ItemCallback<Recurso> diff = new DiffUtil.ItemCallback<Recurso>() {
            @Override public boolean areItemsTheSame(@NonNull Recurso a, @NonNull Recurso b) {
                return a.getId() == b.getId();
            }
            @Override public boolean areContentsTheSame(@NonNull Recurso a, @NonNull Recurso b) {
                return a.equals(b);
            }
        };
        ImageGridAdapter.Binder<Recurso> binder = getRecursoBinder();
        ImageGridAdapter.OnItemClickListener<Recurso> listener = r -> {
            Intent i = new Intent(this, RecursoDetailActivity.class);
            i.putExtra("id_recurso", r.getId());
            startActivity(i);
        };
        ImageGridAdapter<Recurso> grid = new ImageGridAdapter<>(diff, binder, listener);
        rv.setAdapter(grid);

        service.loadServiciosPorCategoria(Integer.parseInt(String.valueOf(categoryId)), new GenericActivityService.LoadCallback<Recurso>() {
            @Override public void onSuccess(List<Recurso> items) {
                runOnUiThread(() -> grid.submitList(items));
            }
            @Override public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(GenericListActivity.this,
                                "Error cargando categoría " + categoryId + ": " + t.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @NonNull
    private ImageGridAdapter.Binder<Recurso> getRecursoBinder() {
        return (iv, r) -> {
            String url = r.getImagen();
            RequestCreator req = Picasso.get().load(url).fit().centerCrop();
            if (loadedUrls.contains(url)) {
                req.into(iv);
            } else {
                req.placeholder(makeLoader())
                        .into(iv, new Callback() {
                            @Override public void onSuccess() { loadedUrls.add(url); }
                            @Override public void onError(Exception ex) { }
                        });
            }
        };
    }
}