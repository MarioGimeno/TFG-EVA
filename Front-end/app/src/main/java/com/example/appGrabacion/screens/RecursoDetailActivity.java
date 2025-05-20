// RecursoDetailActivity.java
package com.example.appGrabacion.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appGrabacion.R;
import com.example.appGrabacion.models.Recurso;
import com.example.appGrabacion.services.ResourceModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class RecursoDetailActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private ImageView imgRecurso,
            ivDescripcionInline, ivRequisitosInline,
            ivGratuitoInline, ivAccesibleInline,
            ivEmail, ivPhone, ivAddress, ivHorarioInline, ivWebContact;
    private ProgressBar progressImage;
    private TextView tvDescripcion, tvRequisitosDetail,
            tvGratuito, tvAccesible, tvEmailDetail,
            tvTelefonoDetail, tvDireccionDetail,
            tvHorarioDetail, tvWebDetail;
    private LinearLayout llRequisitos;
    private TextView tvRequisitosLabel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurso_detail);

        btnBack           = findViewById(R.id.btnBack);
        imgRecurso        = findViewById(R.id.imgRecursoDetail);
        progressImage     = findViewById(R.id.progressImage);

        tvDescripcion     = findViewById(R.id.tvDescripcionDetail);

        tvRequisitosDetail= findViewById(R.id.tvRequisitosDetail);
        tvGratuito        = findViewById(R.id.tvGratuitoDetail);
        tvAccesible       = findViewById(R.id.tvAccesibleDetail);
        tvEmailDetail     = findViewById(R.id.tvEmailDetail);
        tvTelefonoDetail  = findViewById(R.id.tvTelefonoDetail);
        tvDireccionDetail = findViewById(R.id.tvDireccionDetail);
        tvHorarioDetail   = findViewById(R.id.tvHorarioDetail);
        tvWebDetail       = findViewById(R.id.tvWebDetail);

        ivDescripcionInline = findViewById(R.id.ivDescripcionInline);

        ivEmail             = findViewById(R.id.ivEmailInline);
        ivPhone             = findViewById(R.id.ivPhoneInline);
        ivAddress           = findViewById(R.id.ivAddressInline);
        ivHorarioInline     = findViewById(R.id.ivHorarioInline);
        ivWebContact        = findViewById(R.id.ivWebContact);

        btnBack.setOnClickListener(v -> finish());

        int idRecurso = getIntent().getIntExtra("id_recurso", -1);
        if (idRecurso < 0) {
            finish();
            return;
        }

        new ResourceModel(this).fetchById(idRecurso, new ResourceModel.ResourceDetailCallback() {
            @Override
            public void onSuccess(Recurso r) {
                // Imagen + loader
                progressImage.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(r.getImagen())) {
                    Picasso.get()
                            .load(r.getImagen())
                            .into(imgRecurso, new Callback() {
                                @Override public void onSuccess() {
                                    progressImage.setVisibility(View.GONE);
                                }
                                @Override public void onError(Exception e) {
                                    progressImage.setVisibility(View.GONE);
                                }
                            });
                } else {
                    progressImage.setVisibility(View.GONE);
                }

                // Descripción
                tvDescripcion.setText(
                        TextUtils.isEmpty(r.getDescripcion())
                                ? "No disponible"
                                : r.getDescripcion()
                );

                // Requisitos: contar palabras y cambiar orientación
                String reqText = TextUtils.isEmpty(r.getRequisitos())
                        ? "No disponible"
                        : r.getRequisitos();
                tvRequisitosDetail.setText(reqText);

                // Gratuito
                tvGratuito.setText(r.isGratuito() ? "Sí" : "No");

                // Accesible
                tvAccesible.setText(
                        r.isAccesible()
                                ? "Disponible para personas con discapacidad auditiva."
                                : "No disponible para personas con discapacidad auditiva."
                );

                // Email
                tvEmailDetail.setText(
                        TextUtils.isEmpty(r.getEmail()) ? "No disponible" : r.getEmail()
                );
                if (!TextUtils.isEmpty(r.getEmail())) {
                    ivEmail.setOnClickListener(v ->
                            startActivity(new Intent(Intent.ACTION_SENDTO,
                                    Uri.parse("mailto:" + r.getEmail())))
                    );
                }

                // Teléfono
                tvTelefonoDetail.setText(
                        TextUtils.isEmpty(r.getTelefono()) ? "No disponible" : r.getTelefono()
                );
                if (!TextUtils.isEmpty(r.getTelefono())) {
                    ivPhone.setOnClickListener(v ->
                            startActivity(new Intent(Intent.ACTION_DIAL,
                                    Uri.parse("tel:" + r.getTelefono())))
                    );
                }

                // Dirección
                tvDireccionDetail.setText(
                        TextUtils.isEmpty(r.getDireccion()) ? "No disponible" : r.getDireccion()
                );
                if (!TextUtils.isEmpty(r.getDireccion())) {
                    ivAddress.setOnClickListener(v -> {
                        Uri geo = Uri.parse("geo:0,0?q=" + Uri.encode(r.getDireccion()));
                        Intent map = new Intent(Intent.ACTION_VIEW, geo)
                                .setPackage("com.google.android.apps.maps");
                        if (map.resolveActivity(getPackageManager()) != null) {
                            startActivity(map);
                        } else {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/maps/search/?api=1&query="
                                            + Uri.encode(r.getDireccion()))));
                        }
                    });
                }

                // Horario
                tvHorarioDetail.setText(
                        TextUtils.isEmpty(r.getHorario()) ? "No disponible" : r.getHorario()
                );

                // Web
                if (!TextUtils.isEmpty(r.getWeb())) {
                    ivWebContact.setOnClickListener(v -> {
                        String url = r.getWeb();
                        if (!url.startsWith("http")) url = "http://" + url;
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    });
                    tvWebDetail.setText("Accede aquí");
                } else {
                    tvWebDetail.setText("No disponible");
                }
            }

            @Override public void onError(Throwable t) {
                finish();
            }
        });
    }
}
