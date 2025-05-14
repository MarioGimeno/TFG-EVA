package com.example.appGrabacion.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class EntidadDetailActivity extends AppCompatActivity {
    private ImageView imgEntidad, ivWeb, ivPhone, ivAddress, ivEmail;
    private TextView tvNombre;
    private RecyclerView rvRecursos;
    private RecursosDetailAdapter recursoAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entidad_detail);

        // 1) Bind de vistas
        imgEntidad  = findViewById(R.id.imgEntidadDetail);
        tvNombre    = findViewById(R.id.tvNombreEntidadDetail);
        ivWeb       = findViewById(R.id.ivWeb);
        ivPhone     = findViewById(R.id.ivPhone);
        ivAddress   = findViewById(R.id.ivAddress);
        ivEmail     = findViewById(R.id.ivEmail);
        rvRecursos  = findViewById(R.id.rvRecursosDetail);

        // 2) Slider horizontal
        rvRecursos.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recursoAdapter = new RecursosDetailAdapter(this);
        rvRecursos.setAdapter(recursoAdapter);

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
                // Imagen
                if (e.getImagen() != null && !e.getImagen().isEmpty()) {
                    Picasso.get()
                            .load(e.getImagen())
                            .placeholder(R.drawable.eva
                            )
                            .into(imgEntidad);
                }

                // Web
                ivWeb.setOnClickListener(v -> {
                    String url = e.getPaginaWeb();
                    if (url == null || url.isEmpty()) return;
                    if (!url.startsWith("http")) url = "http://" + url;
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                });
                // Teléfono
                ivPhone.setOnClickListener(v -> {
                    String tel = e.getTelefono();
                    if (tel == null || tel.isEmpty()) return;
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + tel)));
                });
                // Dirección
                ivAddress.setOnClickListener(v -> {
                    String addr = e.getDireccion();
                    if (addr == null || addr.isEmpty()) return;
                    Uri geo = Uri.parse("geo:0,0?q=" + Uri.encode(addr));
                    Intent map = new Intent(Intent.ACTION_VIEW, geo);
                    map.setPackage("com.google.android.apps.maps");
                    if (map.resolveActivity(getPackageManager()) != null) {
                        startActivity(map);
                    } else {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/maps/search/?api=1&query="
                                        + Uri.encode(addr))));
                    }
                });
                // Email
                ivEmail.setOnClickListener(v -> {
                    String mail = e.getEmail();
                    if (mail == null || mail.isEmpty()) return;
                    Intent email = new Intent(Intent.ACTION_SENDTO);
                    email.setData(Uri.parse("mailto:" + mail));
                    startActivity(email);
                });
            }

            @Override
            public void onError(Throwable t) {
                Toast.makeText(EntidadDetailActivity.this,
                        "Error al cargar entidad: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });

        // 5) Cargar y filtrar recursos asociados
        new ResourceService(this).fetchAll(new ResourceService.ResourceCallback() {
            @Override
            public void onSuccess(List<Recurso> lista) {
                List<Recurso> filtrados = new ArrayList<>();
                for (Recurso r : lista) {
                    if (r.getIdEntidad() == idEntidad) filtrados.add(r);
                }
                recursoAdapter.submitList(filtrados);
            }
            @Override
            public void onError(Throwable t) {
                Toast.makeText(EntidadDetailActivity.this,
                        "Error al cargar recursos: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
