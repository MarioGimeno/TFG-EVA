// src/main/java/com/example/appGrabacion/screens/EntidadDetailActivity.java
package com.example.appGrabacion.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.RecursosDetailAdapter;
import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.models.Recurso;
import com.example.appGrabacion.utils.EntityService;
import com.example.appGrabacion.services.ResourceService;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class EntidadDetailActivity extends AppCompatActivity {
    private ImageView imgEntidadDetail,
            ivWebInline, ivEmailInline, ivPhoneInline, ivHorarioInline, ivAddressInline;
    private TextView tvNombreEntidadDetail,
            tvWebLink, tvEmailText, tvPhoneText, tvHorarioText, tvAddressText;
    private ProgressBar progressImage, progressSlider;
    private RecyclerView rvRecursos;
    private RecursosDetailAdapter recursoAdapter;
    private ImageButton btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entidad_detail);
        // bind back button and handle click
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        // 1) Bind de vistas
        imgEntidadDetail      = findViewById(R.id.imgEntidadDetail);
        progressImage         = findViewById(R.id.progressImage);

        ivWebInline    = findViewById(R.id.ivWebInline);
        tvWebLink      = findViewById(R.id.tvWebLink);

        ivEmailInline  = findViewById(R.id.ivEmailInline);
        tvEmailText    = findViewById(R.id.tvEmailText);

        ivPhoneInline  = findViewById(R.id.ivPhoneInline);
        tvPhoneText    = findViewById(R.id.tvPhoneText);

        ivHorarioInline= findViewById(R.id.ivHorarioInline);
        tvHorarioText  = findViewById(R.id.tvHorarioText);

        ivAddressInline= findViewById(R.id.ivAddressInline);
        tvAddressText  = findViewById(R.id.tvAddressText);

        progressSlider = findViewById(R.id.progressSlider);
        rvRecursos     = findViewById(R.id.rvRecursosDetail);

        // 2) Configurar slider horizontal, invisible inicialmente
        rvRecursos.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recursoAdapter = new RecursosDetailAdapter(this);
        rvRecursos.setAdapter(recursoAdapter);
        rvRecursos.setAlpha(0f);
        rvRecursos.setVisibility(View.GONE);

        // 3) Obtener ID de la entidad
        int idEntidad = getIntent().getIntExtra("id_entidad", -1);
        if (idEntidad < 0) {
            Toast.makeText(this, "Entidad inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 4) Cargar datos de la entidad
        new EntityService(this).fetchById(idEntidad, new EntityService.EntityDetailCallback() {
            @Override
            public void onSuccess(Entidad e) {
                // Mostrar loader de la imagen
                progressImage.setVisibility(View.VISIBLE);

                // Cargar imagen con callback para ocultar loader
                if (!TextUtils.isEmpty(e.getImagen())) {
                    Picasso.get()
                            .load(e.getImagen())
                            .placeholder(R.drawable.eva)
                            .into(imgEntidadDetail, new Callback() {
                                @Override
                                public void onSuccess() {
                                    progressImage.animate()
                                            .alpha(0f)
                                            .setDuration(300)
                                            .withEndAction(() -> progressImage.setVisibility(View.GONE))
                                            .start();
                                }
                                @Override public void onError(Exception ex) {
                                    progressImage.setVisibility(View.GONE);
                                }
                            });
                } else {
                    progressImage.setVisibility(View.GONE);
                }

                // Web
                if (TextUtils.isEmpty(e.getPaginaWeb())) {
                    tvWebLink.setText("No disponible");
                    ivWebInline.setEnabled(false);
                } else {
                    tvWebLink.setText("Accede aquí");
                    ivWebInline.setEnabled(true);
                    tvWebLink.setOnClickListener(v -> openUrl(e.getPaginaWeb()));
                    ivWebInline.setOnClickListener(v -> openUrl(e.getPaginaWeb()));
                }

                // Email
                if (TextUtils.isEmpty(e.getEmail())) {
                    tvEmailText.setText("No disponible");
                    ivEmailInline.setEnabled(false);
                } else {
                    tvEmailText.setText(e.getEmail());
                    ivEmailInline.setEnabled(true);
                    ivEmailInline.setOnClickListener(v -> sendEmail(e.getEmail()));
                }

                // Teléfono
                if (TextUtils.isEmpty(e.getTelefono())) {
                    tvPhoneText.setText("No disponible");
                    ivPhoneInline.setEnabled(false);
                } else {
                    tvPhoneText.setText(e.getTelefono());
                    ivPhoneInline.setEnabled(true);
                    ivPhoneInline.setOnClickListener(v -> dialPhone(e.getTelefono()));
                }

                // Horario
                if (TextUtils.isEmpty(e.getHorario())) {
                    tvHorarioText.setText("No disponible");
                    ivHorarioInline.setEnabled(false);
                } else {
                    tvHorarioText.setText(e.getHorario());
                    ivHorarioInline.setEnabled(true);
                    ivHorarioInline.setOnClickListener(v ->
                            Toast.makeText(EntidadDetailActivity.this,
                                    "Horario: " + e.getHorario(),
                                    Toast.LENGTH_SHORT).show()
                    );
                }

                // Dirección
                if (TextUtils.isEmpty(e.getDireccion())) {
                    tvAddressText.setText("No disponible");
                    ivAddressInline.setEnabled(false);
                } else {
                    tvAddressText.setText(e.getDireccion());
                    ivAddressInline.setEnabled(true);
                    ivAddressInline.setOnClickListener(v -> openMap(e.getDireccion()));
                }
            }

            @Override
            public void onError(Throwable t) {
                Toast.makeText(EntidadDetailActivity.this,
                        "Error al cargar entidad: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });

        // 5) Cargar y prefetch de recursos asociados con loader
        progressSlider.setVisibility(View.VISIBLE);
        new ResourceService(this).fetchAll(new ResourceService.ResourceCallback() {
            @Override
            public void onSuccess(List<Recurso> lista) {
                List<Recurso> filtrados = new ArrayList<>();
                for (Recurso r : lista) {
                    if (r.getIdEntidad() == idEntidad) filtrados.add(r);
                }
                if (filtrados.isEmpty()) {
                    showSliderWithFade();
                } else {
                    prefetchImagesAndShow(filtrados);
                }
            }
            @Override public void onError(Throwable t) {
                progressSlider.setVisibility(View.GONE);
                Toast.makeText(EntidadDetailActivity.this,
                        "Error al cargar recursos: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Pre-carga todas las imágenes de recursos y luego muestra el slider animado */
    private void prefetchImagesAndShow(List<Recurso> list) {
        final int total = list.size();
        final int[] loaded = {0};
        for (Recurso r : list) {
            Picasso.get()
                    .load(r.getImagen())
                    .fetch(new Callback() {
                        @Override public void onSuccess() {
                            if (++loaded[0] == total) {
                                recursoAdapter.submitList(list);
                                showSliderWithFade();
                            }
                        }
                        @Override public void onError(Exception e) {
                            if (++loaded[0] == total) {
                                recursoAdapter.submitList(list);
                                showSliderWithFade();
                            }
                        }
                    });
        }
    }

    /** Cross-fade entre loader y slider */
    private void showSliderWithFade() {
        rvRecursos.setVisibility(View.VISIBLE);
        rvRecursos.setAlpha(0f);

        progressSlider.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> progressSlider.setVisibility(View.GONE))
                .start();

        rvRecursos.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    private void openUrl(String url) {
        if (TextUtils.isEmpty(url)) return;
        if (!url.startsWith("http")) url = "http://" + url;
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void dialPhone(String tel) {
        if (TextUtils.isEmpty(tel)) return;
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel)));
    }

    private void openMap(String addr) {
        if (TextUtils.isEmpty(addr)) return;
        Uri geo = Uri.parse("geo:0,0?q=" + Uri.encode(addr));
        Intent map = new Intent(Intent.ACTION_VIEW, geo)
                .setPackage("com.google.android.apps.maps");
        if (map.resolveActivity(getPackageManager()) != null) {
            startActivity(map);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query="
                            + Uri.encode(addr))));
        }
    }

    private void sendEmail(String mail) {
        if (TextUtils.isEmpty(mail)) return;
        startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + mail)));
    }
}
