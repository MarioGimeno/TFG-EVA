package com.example.appGrabacion.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.example.appGrabacion.MainActivity;
import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.ImageGridAdapter;
import com.example.appGrabacion.contracts.CategoriasContract;
import com.example.appGrabacion.entities.Categoria;
import com.example.appGrabacion.entities.Entidad;
import com.example.appGrabacion.entities.Recurso;
import com.example.appGrabacion.models.CategoriaModel;
import com.example.appGrabacion.models.GenericActivityModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenericListActivity extends AppCompatActivity {
    public static final String EXTRA_LIST_TYPE    = "list_type";      // "entidades","servicios","gratuitos","accesibles"
    public static final String EXTRA_CATEGORY_ID  = "category_id";    // int para categoría
    public static final String EXTRA_TYPE         = "type";           // lo mantenemos

    private RecyclerView rv;
    private GenericActivityModel service;
    private final Set<String> loadedUrls = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic);

        Intent intent = getIntent();
        boolean hasCategory = intent.hasExtra(EXTRA_CATEGORY_ID);
        String listType     = intent.getStringExtra(EXTRA_LIST_TYPE);

        ImageView bg = findViewById(R.id.videoBackground);
        rv = findViewById(R.id.rvItems);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        service = new GenericActivityModel(this);

        if (hasCategory) {
            // Modo: categoría
            int catId = intent.getIntExtra(EXTRA_CATEGORY_ID, -1);
            if (catId < 0) {
                Toast.makeText(this, "Categoría inválida", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            loadCategoryAndServicios(catId, bg);

        } else if ("entidades".equalsIgnoreCase(listType)) {
            // Modo: Entidades
            bg.setImageResource(R.drawable.entidades);
            setupEntidadesGrid();

        } else if ("gratuitos".equalsIgnoreCase(listType)) {
            // Modo: Gratuitos
            bg.setImageResource(R.drawable.fondo_gratuitos);
            setupGratuitosGrid();

        } else if ("accesibles".equalsIgnoreCase(listType)) {
            // Modo: Accesibles
            bg.setImageResource(R.drawable.fondo_accesibles);
            setupAccesiblesGrid();

        } else {
            // Modo: Todos los servicios
            bg.setImageResource(R.drawable.servicios);
            setupServiciosGrid();
        }
    }

    /** 1) Categoría → fondo + grid */
    private void loadCategoryAndServicios(int categoryId, ImageView bg) {
        new CategoriaModel(this).fetchById(categoryId, new CategoriasContract.Service.Callback<Categoria>() {
            @Override public void onSuccess(Categoria cat) {
                Log.d("GenericList",
                        "Cat → id:" + cat.getIdCategoria() +
                                " nombre:\"" + cat.getNombre() +
                                "\" img:" + cat.getImgCategoria()
                );
                String url = cat.getImgCategoria();
                if (url != null && !url.isEmpty()) {
                    Picasso.get()
                            .load(url)
                            .placeholder(R.drawable.eva)
                            .error(R.drawable.eva)
                            .into(bg);
                }
                setupServiciosPorCategoriaGrid(categoryId);
            }
            @Override public void onError(Throwable t) {
                Toast.makeText(GenericListActivity.this,
                        "Error cargando categoría: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /** Grid para recursos de una categoría */
    private void setupServiciosPorCategoriaGrid(int categoryId) {
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        DiffUtil.ItemCallback<Recurso> diff = getRecursoDiff();
        ImageGridAdapter.Binder<Recurso> binder = getRecursoBinder();
        ImageGridAdapter.OnItemClickListener<Recurso> listener = r -> {
            Intent i = new Intent(this, RecursoDetailActivity.class);
            i.putExtra("id_recurso", r.getId());
            startActivity(i);
        };
        ImageGridAdapter<Recurso> grid = new ImageGridAdapter<>(diff, binder, listener);
        rv.setAdapter(grid);

        service.loadServiciosPorCategoria(categoryId, new GenericActivityModel.LoadCallback<Recurso>() {
            @Override public void onSuccess(List<Recurso> items) {
                runOnUiThread(() -> {
                    if (items.size() < 2) {
                        // horizontal si <5
                        rv.setLayoutManager(new LinearLayoutManager(
                                GenericListActivity.this,
                                LinearLayoutManager.HORIZONTAL,
                                false
                        ));
                    }
                    grid.submitList(items);
                });
            }
            @Override public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(GenericListActivity.this,
                                "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /** Grid: todas las entidades */
    private void setupEntidadesGrid() {
        rv.setLayoutManager(new GridLayoutManager(this, 2));
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

        service.loadEntidades(new GenericActivityModel.LoadCallback<Entidad>() {
            @Override public void onSuccess(List<Entidad> items) {
                List<Entidad> reordered = new ArrayList<>(items);
                // desplaza email especial al frente
                for (int i = 0; i < reordered.size(); i++) {
                    Entidad ent = reordered.get(i);
                    if ("casamujer@zaragoza.es".equalsIgnoreCase(ent.getEmail())) {
                        reordered.remove(i);
                        reordered.add(0, ent);
                        break;
                    }
                }
                runOnUiThread(() -> grid.submitList(reordered));
            }
            @Override public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(GenericListActivity.this,
                                "Error entidades: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /** Grid: TODOS los servicios */
    private void setupServiciosGrid() {
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        DiffUtil.ItemCallback<Recurso> diff = getRecursoDiff();
        ImageGridAdapter.Binder<Recurso> binder = getRecursoBinder();
        ImageGridAdapter.OnItemClickListener<Recurso> listener = r -> {
            Intent i = new Intent(this, RecursoDetailActivity.class);
            i.putExtra("id_recurso", r.getId());
            startActivity(i);
        };
        ImageGridAdapter<Recurso> grid = new ImageGridAdapter<>(diff, binder, listener);
        rv.setAdapter(grid);

        service.loadServicios(new GenericActivityModel.LoadCallback<Recurso>() {
            @Override public void onSuccess(List<Recurso> items) {
                Collections.shuffle(items);
                runOnUiThread(() -> grid.submitList(items));
            }
            @Override public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(GenericListActivity.this,
                                "Error servicios: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /** Grid: sólo gratuitos */
    private void setupGratuitosGrid() {
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        DiffUtil.ItemCallback<Recurso> diff = getRecursoDiff();
        ImageGridAdapter.Binder<Recurso> binder = getRecursoBinder();
        ImageGridAdapter.OnItemClickListener<Recurso> listener = r -> {
            Intent i = new Intent(this, RecursoDetailActivity.class);
            i.putExtra("id_recurso", r.getId());
            startActivity(i);
        };
        ImageGridAdapter<Recurso> grid = new ImageGridAdapter<>(diff, binder, listener);
        rv.setAdapter(grid);

        service.loadGratuitos(new GenericActivityModel.LoadCallback<Recurso>() {
            @Override public void onSuccess(List<Recurso> items) {
                runOnUiThread(() -> grid.submitList(items));
            }
            @Override public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(GenericListActivity.this,
                                "Error gratuitos: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /** Grid: sólo accesibles */
    private void setupAccesiblesGrid() {
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        DiffUtil.ItemCallback<Recurso> diff = getRecursoDiff();
        ImageGridAdapter.Binder<Recurso> binder = getRecursoBinder();
        ImageGridAdapter.OnItemClickListener<Recurso> listener = r -> {
            Intent i = new Intent(this, RecursoDetailActivity.class);
            i.putExtra("id_recurso", r.getId());
            startActivity(i);
        };
        ImageGridAdapter<Recurso> grid = new ImageGridAdapter<>(diff, binder, listener);
        rv.setAdapter(grid);

        service.loadAccesibles(new GenericActivityModel.LoadCallback<Recurso>() {
            @Override public void onSuccess(List<Recurso> items) {
                runOnUiThread(() -> grid.submitList(items));
            }
            @Override public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(GenericListActivity.this,
                                "Error accesibles: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @NonNull
    private DiffUtil.ItemCallback<Recurso> getRecursoDiff() {
        return new DiffUtil.ItemCallback<Recurso>() {
            @Override public boolean areItemsTheSame(@NonNull Recurso a, @NonNull Recurso b) {
                return a.getId() == b.getId();
            }
            @Override public boolean areContentsTheSame(@NonNull Recurso a, @NonNull Recurso b) {
                return a.equals(b);
            }
        };
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

    private CircularProgressDrawable makeLoader() {
        CircularProgressDrawable l = new CircularProgressDrawable(this);
        l.setStrokeWidth(5f);
        l.setCenterRadius(30f);
        l.start();
        return l;
    }
}
