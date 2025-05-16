package com.example.appGrabacion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.appGrabacion.adapters.SliderAdapter;
import com.example.appGrabacion.models.ContactEntry;
import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.models.Recurso;
import com.example.appGrabacion.screens.ContactsActivity;
import com.example.appGrabacion.screens.EntidadDetailActivity;
import com.example.appGrabacion.screens.FolderActivity;
import com.example.appGrabacion.screens.GenericListActivity;
import com.example.appGrabacion.screens.LoginActivity;
import com.example.appGrabacion.screens.RecursoDetailActivity;
import com.example.appGrabacion.screens.RecursosActivity;
import com.example.appGrabacion.services.ResourceService;
import com.example.appGrabacion.utils.ContactManager;
import com.example.appGrabacion.utils.ContactsApi;
import com.example.appGrabacion.services.EntityService;
import com.example.appGrabacion.utils.MyFirebaseMessagingService;
import com.example.appGrabacion.utils.RetrofitClient;
import com.example.appGrabacion.utils.SessionManager;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQ_PERMISSIONS = 100;

    private ViewPager2 vpSlider;
    private SliderAdapter sliderAdapter;
    private final List<Entidad> entidades = new ArrayList<>();
    private final List<Recurso> recursos = new ArrayList<>();
    private boolean entLoaded = false, recLoaded = false;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());

    // servicios directos
    private EntityService entityService;
    private ResourceService resourceService;

    // permisos
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.POST_NOTIFICATIONS
                    : null
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Calculadora_Front);
        setContentView(R.layout.activity_main);
        requestAllPermissions();
        setupNavigationButtons();
        syncContacts();
        registerFcmToken();

        // init servicios
        entityService   = new EntityService(this);
        resourceService = new ResourceService(this);

        // init ViewPager
        vpSlider = findViewById(R.id.vpSlider);

        // 1) Cargar entidades y servicios
        entityService.fetchAll(new EntityService.EntityCallback() {
            @Override public void onSuccess(List<Entidad> list) {
                entidades.clear();
                entidades.addAll(list);
                entLoaded = true;
                tryInitSlider();
            }
            @Override public void onError(Throwable t) {
                Log.e(TAG, "Error cargando entidades", t);
            }
        });
        resourceService.fetchAll(new ResourceService.ResourceCallback() {
            @Override public void onSuccess(List<Recurso> list) {
                recursos.clear();
                recursos.addAll(list);
                recLoaded = true;
                tryInitSlider();
            }
            @Override public void onError(Throwable t) {
                Log.e(TAG, "Error cargando recursos", t);
            }
        });

        // 2) Autoscroll
        sliderHandler.postDelayed(new Runnable() {
            @Override public void run() {
                if (sliderAdapter != null && sliderAdapter.getItemCount() > 0) {
                    int next = (vpSlider.getCurrentItem() + 1) % sliderAdapter.getItemCount();
                    vpSlider.setCurrentItem(next, true);
                }
                sliderHandler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    /** Solo inicializa el slider cuando ambas listas estén cargadas */
    private void tryInitSlider() {
        if (!entLoaded || !recLoaded) return;

        List<SliderAdapter.Item> mixed = new ArrayList<>();
        int max = Math.max(entidades.size(), recursos.size());
        for (int i = 0; i < max; i++) {
            if (i < entidades.size()) mixed.add(new SliderAdapter.EntidadItem(entidades.get(i)));
            if (i < recursos.size()) mixed.add(new SliderAdapter.RecursoItem(recursos.get(i)));
        }

        sliderAdapter = new SliderAdapter(mixed, item -> {
            if (item instanceof SliderAdapter.EntidadItem) {
                Entidad e = ((SliderAdapter.EntidadItem) item).getEntidad();
                startActivity(new Intent(this, EntidadDetailActivity.class)
                        .putExtra("id_entidad", e.getIdEntidad()));
            } else {
                Recurso r = ((SliderAdapter.RecursoItem) item).getRecurso();
                startActivity(new Intent(this, RecursoDetailActivity.class)
                        .putExtra("id_recurso", r.getId()));
            }
        });
        vpSlider.setAdapter(sliderAdapter);

        // 1) Desactivar clipping
        vpSlider.setClipToPadding(false);
        vpSlider.setClipChildren(false);
        ViewGroup sliderParent = (ViewGroup) vpSlider.getParent();
        sliderParent.setClipChildren(false);

        // 2) Auto-peek y transformaciones
        vpSlider.setOffscreenPageLimit(3);
        CompositePageTransformer ct = new CompositePageTransformer();
        ct.addTransformer(new MarginPageTransformer(30));
        ct.addTransformer((page, position) -> {
            float scale = 0.85f + (1 - Math.abs(position)) * 0.15f;
            page.setScaleY(scale);
        });
        vpSlider.setPageTransformer(ct);
    }


    private void setupNavigationButtons() {
        findViewById(R.id.btnGoLogin).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );
        findViewById(R.id.btnGoFolder).setOnClickListener(v ->
                startActivity(new Intent(this, FolderActivity.class))
        );
        findViewById(R.id.btnGoEntidades).setOnClickListener(v -> {
            Intent i = new Intent(this, GenericListActivity.class);
            i.putExtra(GenericListActivity.EXTRA_TYPE, "entidades");
            startActivity(i);
        });
        findViewById(R.id.btnGoRecursos).setOnClickListener(v -> {
            Intent i = new Intent(this, GenericListActivity.class);
            i.putExtra(GenericListActivity.EXTRA_TYPE, "servicios");
            startActivity(i);
        });
        findViewById(R.id.btnGoContacts).setOnClickListener(v ->
                startActivity(new Intent(this, ContactsActivity.class))
        );
    }

    private void registerFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        MyFirebaseMessagingService.registerTokenWithServer(
                                this, task.getResult()
                        );
                    }
                });
    }

    private void syncContacts() {
        String bearer = "Bearer " + SessionManager.getToken(this);
        ContactsApi api = RetrofitClient
                .getRetrofitInstance(this)
                .create(ContactsApi.class);

        api.getContacts(bearer).enqueue(new Callback<List<ContactEntry>>() {
            @Override
            public void onResponse(Call<List<ContactEntry>> call,
                                   Response<List<ContactEntry>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    ContactManager mgr = new ContactManager(MainActivity.this);
                    mgr.saveContacts(resp.body());
                    Log.d(TAG, "Contacts synced: " + resp.body().size());
                } else {
                    Log.e(TAG, "Error fetching contacts: " + resp.code());
                }
            }

            @Override
            public void onFailure(Call<List<ContactEntry>> call, Throwable t) {
                Log.e(TAG, "Failed to sync contacts", t);
            }
        });
    }


    private void requestAllPermissions() {
        List<String> toReq = new ArrayList<>();
        for (String p : REQUIRED_PERMISSIONS) {
            if (p==null) continue;
            if (ContextCompat.checkSelfPermission(this,p)
                    != PackageManager.PERMISSION_GRANTED) {
                toReq.add(p);
            }
        }
        if (!toReq.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    toReq.toArray(new String[0]),
                    REQ_PERMISSIONS
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sliderHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onRequestPermissionsResult(int code,
                                           @NonNull String[] perms,@NonNull int[] grants) {
        super.onRequestPermissionsResult(code, perms, grants);
        if (code==REQ_PERMISSIONS) {
            for (int i=0; i<perms.length; i++) {
                if (grants[i]!=PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                            "Necesito " + perms[i],
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
