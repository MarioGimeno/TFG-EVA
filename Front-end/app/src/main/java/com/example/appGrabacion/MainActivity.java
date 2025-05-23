package com.example.appGrabacion;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.appGrabacion.adapters.SliderAdapter;
import com.example.appGrabacion.entities.ContactEntry;
import com.example.appGrabacion.entities.Entidad;
import com.example.appGrabacion.entities.Recurso;
import com.example.appGrabacion.screens.CalculadoraScreen;
import com.example.appGrabacion.screens.CategoriasActivity;
import com.example.appGrabacion.screens.ContactsActivity;
import com.example.appGrabacion.screens.EntidadDetailActivity;
import com.example.appGrabacion.screens.FolderActivity;
import com.example.appGrabacion.screens.LoginActivity;
import com.example.appGrabacion.screens.RecursoDetailActivity;
import com.example.appGrabacion.models.EntityModel;
import com.example.appGrabacion.models.ResourceModel;

import com.example.appGrabacion.utils.ContactManager;
import com.example.appGrabacion.utils.ContactsApi;
import com.example.appGrabacion.utils.MyFirebaseMessagingService;
import com.example.appGrabacion.utils.RetrofitClient;
import com.example.appGrabacion.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQ_PERMISSIONS = 100;
    private ViewPager2 vpSlider;
    private SliderAdapter sliderAdapter;
    private ProgressBar pbLoader;
    private final List<Entidad> entidades = new ArrayList<>();
    private final List<Recurso> recursos = new ArrayList<>();
    private boolean entLoaded = false, recLoaded = false;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private EntityModel entityModel;
    private ResourceModel resourceModel;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Manifest.permission.POST_NOTIFICATIONS : null
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Calculadora_Front);
        setContentView(R.layout.activity_main);

        setupFooterButtons();

        // Referencias a vistas
        vpSlider = findViewById(R.id.vpSlider);
        pbLoader = findViewById(R.id.pbLoader);
        TextView tvDesc = findViewById(R.id.tvSectionDesc);
        tvDesc.setText(Html.fromHtml(
                getString(R.string.section_eva_desc),
                Html.FROM_HTML_MODE_LEGACY
        ));

        // Scroll dinámico
        ImageView bg = findViewById(R.id.videoBackground);
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
            NestedScrollView scroll = findViewById(R.id.scrollView);
            scroll.setOnScrollChangeListener(
                    (NestedScrollView v, int scrollX, int scrollY, int oldX, int oldY) -> {
                        int dy = Math.max(0, Math.min(scrollY, maxScroll));
                        params.topMargin = initialMarginTop - dy;
                        wrapper.setLayoutParams(params);
                    }
            );
        });

        requestAllPermissions();
        syncContacts();
        registerFcmToken();


        ImageView ivManual = findViewById(R.id.ivSectionImage);
        ivManual.setOnClickListener(v -> {
            String fileIdResumen = "1hpYwHZcODV3tUBeArWO5Kn3FuRzRG-GX";
            String urlResumen = "https://drive.google.com/uc?export=download&id=" + fileIdResumen;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlResumen)));

        });
        MaterialButton btnDownload = findViewById(R.id.btnDownloadManual);
        btnDownload.setOnClickListener(v -> {
            String fileId = "1Xq8GgtBpe_kmpsBMbq-Cx_s7wquZHSwD";
            String url = "https://drive.google.com/uc?export=download&id=" + fileId;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });

        // Inicializar servicios y cargar slider

        entityModel = new EntityModel(this);
        resourceModel = new ResourceModel(this);

        entityModel.fetchAll(new EntityModel.EntityCallback() {

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
        resourceModel.fetchAll(new ResourceModel.ResourceCallback() {
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
        sliderHandler.postDelayed(new Runnable() {
            @Override public void run() {
                if (sliderAdapter != null && sliderAdapter.getItemCount() > 0) {
                    int next = (vpSlider.getCurrentItem() + 1) % sliderAdapter.getItemCount();
                    vpSlider.setCurrentItem(next, true);
                }
                sliderHandler.postDelayed(this, 5000);
            }
        }, 5000);
    }

    /** Inicializa el slider cuando ambos servicios han cargado */
    private void tryInitSlider() {
        if (!entLoaded || !recLoaded) return;
        List<SliderAdapter.Item> mixed = new ArrayList<>();
        int max = Math.max(entidades.size(), recursos.size());
        for (int i = 0; i < max; i++) {
            if (i < entidades.size())
                mixed.add(new SliderAdapter.EntidadItem(entidades.get(i)));
            if (i < recursos.size())
                mixed.add(new SliderAdapter.RecursoItem(recursos.get(i)));
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

        vpSlider.setClipToPadding(false);
        vpSlider.setClipChildren(false);
        ((ViewGroup) vpSlider.getParent()).setClipChildren(false);
        vpSlider.setOffscreenPageLimit(3);
        CompositePageTransformer ct = new CompositePageTransformer();
        ct.addTransformer(new MarginPageTransformer(30));
        ct.addTransformer((page, pos) -> {
            float scale = 0.85f + (1 - Math.abs(pos)) * 0.15f;
            page.setScaleY(scale);
        });
        vpSlider.setPageTransformer(ct);

        pbLoader.setVisibility(View.GONE);
        vpSlider.setVisibility(View.VISIBLE);
    }

    /** Muestra el diálogo de huella o PIN del sistema */
    private void showAuthenticationPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt.AuthenticationCallback callback =
                new BiometricPrompt.AuthenticationCallback() {
                    @Override public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(MainActivity.this,
                                "Error de autenticación: " + errString,
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        openCarpetaPersonal();
                    }
                    @Override public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                    }
                };
        BiometricPrompt prompt = new BiometricPrompt(this, executor, callback);
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Acceso a carpeta personal")
                .setSubtitle("Identifícate para continuar")
                .setDescription("Usa tu huella o el PIN/patrón del dispositivo")
                .setDeviceCredentialAllowed(true)
                .build();
        prompt.authenticate(info);
    }

    /** Abre la carpeta personal tras autenticar */
    private void openCarpetaPersonal() {
        startActivity(new Intent(this, FolderActivity.class));
    }

    private void setupFooterButtons() {
        View footer = findViewById(R.id.footerNav);
        SessionManager session = new SessionManager(getApplicationContext());
        boolean loggedIn = session.isLoggedIn();
        Log.e("logueado?", "Token leído: " + session.fetchToken() + " -> " + loggedIn);

        Log.e("logueado? ", String.valueOf(loggedIn));

        View.OnClickListener requireLogin = v -> {
            Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
        };

        footer.findViewById(R.id.btnGoFolder)
                .setOnClickListener(v -> {
                    if (loggedIn) showAuthenticationPrompt();
                    else requireLogin.onClick(v);
                });
        footer.findViewById(R.id.btnGoGrabacion)
                .setOnClickListener(v -> {
                    if (loggedIn) startActivity(new Intent(this, CalculadoraScreen.class));
                    else requireLogin.onClick(v);
                });
        footer.findViewById(R.id.btnGoLogin)
                .setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        footer.findViewById(R.id.btnGoCategorias)
                .setOnClickListener(v -> startActivity(new Intent(this, CategoriasActivity.class)));
        footer.findViewById(R.id.btnGoContacts)
                .setOnClickListener(v -> {
                    if (loggedIn) startActivity(new Intent(this, ContactsActivity.class));
                    else requireLogin.onClick(v);
                });
        ImageView ivFabIcon = findViewById(R.id.ivFabIcon);
        View btnGoLogin = footer.findViewById(R.id.btnGoLogin);

        if (loggedIn) {
            // Cambiar color de fondo y logo al estado "logueado"
            GradientDrawable background = (GradientDrawable) btnGoLogin.getBackground();
            background.setColor(Color.parseColor("#A11991")); // Morado
            ivFabIcon.setImageResource(R.drawable.ic_eva_blanco); // Icono blanco
        } else {
            // Restaurar fondo e icono original (no logueado)
            GradientDrawable background = (GradientDrawable) btnGoLogin.getBackground();
            background.setColor(Color.parseColor("#FFFFFF")); // Fondo blanco original
            ivFabIcon.setImageResource(R.drawable.ic_logo_eva); // Icono original
        }



        footer.bringToFront();
    }


    private void requestAllPermissions() {
        List<String> toReq = new ArrayList<>();
        for (String p : REQUIRED_PERMISSIONS) {
            if (p != null && ContextCompat.checkSelfPermission(this, p)
                    != PackageManager.PERMISSION_GRANTED) {
                toReq.add(p);
            }
        }
        if (!toReq.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, toReq.toArray(new String[0]), REQ_PERMISSIONS
            );
        }
    }

    private void syncContacts() {
        String bearer = "Bearer " + SessionManager.getToken(this);
        Log.d(TAG, "Contacts synced: " + SessionManager.getToken(this));
        ContactsApi api = RetrofitClient.getRetrofitInstance(this)
                .create(ContactsApi.class);
        api.getContacts(bearer).enqueue(new Callback<List<ContactEntry>>() {
            @Override public void onResponse(Call<List<ContactEntry>> call,
                                             Response<List<ContactEntry>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    new ContactManager(MainActivity.this)
                            .saveContacts(resp.body());
                    Log.d(TAG, "Contacts synced: " + resp.body().size());
                } else {
                    Log.e(TAG, "Error fetching contacts: " + resp.code());
                }
            }
            @Override public void onFailure(Call<List<ContactEntry>> call,
                                            Throwable t) {
                Log.e(TAG, "Failed to sync contacts", t);
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int code,
                                           @NonNull String[] perms,
                                           @NonNull int[] grants) {
        super.onRequestPermissionsResult(code, perms, grants);
        if (code == REQ_PERMISSIONS) {
            for (int i = 0; i < perms.length; i++) {
                if (grants[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                            "Necesito " + perms[i],
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sliderHandler.removeCallbacksAndMessages(null);
    }
    @Override
    protected void onResume() {
        super.onResume();
        setupFooterButtons();  // refresca la visibilidad/estado de los botones
    }

    private void downloadFile(String url, String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(fileName);
        request.setDescription("Descargando…");
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        );
        // guardarlo en la carpeta “Downloads”
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, fileName
        );
        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        dm.enqueue(request);
    }

}