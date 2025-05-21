// src/main/java/com/example/appGrabacion/activities/EntidadDetailActivity.java
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
import com.example.appGrabacion.contracts.EntidadDetailContract;
import com.example.appGrabacion.entities.Entidad;
import com.example.appGrabacion.entities.Recurso;
import com.example.appGrabacion.presenters.EntidadDetailPresenter;
import com.example.appGrabacion.models.EntityModel;
import com.example.appGrabacion.models.ResourceModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EntidadDetailActivity extends AppCompatActivity
        implements EntidadDetailContract.View {

    private ImageButton btnBack;
    private ImageView imgEntidadDetail,
            ivWebInline, ivEmailInline, ivPhoneInline, ivHorarioInline, ivAddressInline;
    private TextView tvNombreEntidadDetail,
            tvWebLink, tvEmailText, tvPhoneText, tvHorarioText, tvAddressText;
    private ProgressBar progressImage, progressSlider;
    private RecyclerView rvRecursos;
    private RecursosDetailAdapter recursoAdapter;
    private EntidadDetailPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entidad_detail);

        // Bind views
        btnBack    = findViewById(R.id.btnBack);
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

        // RecyclerView setup
        rvRecursos.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        recursoAdapter = new RecursosDetailAdapter(this);
        rvRecursos.setAdapter(recursoAdapter);
        rvRecursos.setVisibility(View.GONE);

        btnBack.setOnClickListener(v -> finish());

        int idEntidad = getIntent().getIntExtra("id_entidad", -1);
        if (idEntidad < 0) {
            Toast.makeText(this, "Entidad inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Presenter setup
        presenter = new EntidadDetailPresenter(
                new EntityModel(this),
                new ResourceModel(this)
        );
        presenter.attachView(this);
        presenter.loadEntidadAndResources(idEntidad);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    // --- EntidadDetailContract.View methods ---

    @Override
    public void showLoading() {
        progressImage.setVisibility(View.VISIBLE);
        progressSlider.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressImage.setVisibility(View.GONE);
        progressSlider.setVisibility(View.GONE);
    }

    @Override
    public void showEntidad(Entidad e) {
        // load image
        if (!TextUtils.isEmpty(e.getImagen())) {
            Picasso.get()
                    .load(e.getImagen())
                    .into(imgEntidadDetail, new Callback() {
                        @Override public void onSuccess() {
                            progressImage.animate()
                                    .alpha(0f).setDuration(300)
                                    .withEndAction(() -> progressImage.setVisibility(View.GONE))
                                    .start();
                        }
                        @Override public void onError(Exception ex) {
                            progressImage.setVisibility(View.GONE);
                        }
                    });
        }
        // web link
        if (TextUtils.isEmpty(e.getPaginaWeb())) {
            tvWebLink.setText("No disponible");
            ivWebInline.setEnabled(false);
        } else {
            tvWebLink.setText("Accede aquí");
            ivWebInline.setEnabled(true);
            View.OnClickListener openWeb = v -> openUrl(e.getPaginaWeb());
            tvWebLink.setOnClickListener(openWeb);
            ivWebInline.setOnClickListener(openWeb);
        }
        // email
        if (TextUtils.isEmpty(e.getEmail())) {
            tvEmailText.setText("No disponible");
            ivEmailInline.setEnabled(false);
        } else {
            tvEmailText.setText(e.getEmail());
            ivEmailInline.setEnabled(true);
            ivEmailInline.setOnClickListener(v -> sendEmail(e.getEmail()));
        }
        // phone
        if (TextUtils.isEmpty(e.getTelefono())) {
            tvPhoneText.setText("No disponible");
            ivPhoneInline.setEnabled(false);
        } else {
            tvPhoneText.setText(e.getTelefono());
            ivPhoneInline.setEnabled(true);
            ivPhoneInline.setOnClickListener(v -> dialPhone(e.getTelefono()));
        }
        // horario
        if (TextUtils.isEmpty(e.getHorario())) {
            tvHorarioText.setText("No disponible");
            ivHorarioInline.setEnabled(false);
        } else {
            tvHorarioText.setText(e.getHorario());
            ivHorarioInline.setEnabled(true);
            ivHorarioInline.setOnClickListener(v ->
                    Toast.makeText(EntidadDetailActivity.this,
                            "Horario: " + e.getHorario(), Toast.LENGTH_SHORT).show()
            );
        }
        // address
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
    public void showResources(List<Recurso> recursos) {
        recursoAdapter.submitList(recursos);
        rvRecursos.setVisibility(View.VISIBLE);
        rvRecursos.animate().alpha(1f).setDuration(300).start();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    // Helper intents
    private void openUrl(String url) {
        if (!url.startsWith("http")) url = "http://" + url;
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
    private void sendEmail(String mail) {
        startActivity(new Intent(Intent.ACTION_SENDTO,
                Uri.parse("mailto:" + mail)));
    }
    private void dialPhone(String tel) {
        startActivity(new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:" + tel)));
    }
    private void openMap(String addr) {
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
}
