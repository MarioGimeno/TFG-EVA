package com.example.appGrabacion.screens;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appGrabacion.R;
import com.example.appGrabacion.models.Recurso;
import com.example.appGrabacion.services.ResourceService;
import com.squareup.picasso.Picasso;

public class RecursoDetailActivity extends AppCompatActivity {
    private ImageView imgRecurso;
    private TextView tvServicio, tvDescripcion, tvRequisitos,
            tvEmail, tvTelefono, tvDireccion,
            tvHorario, tvWeb, tvGratuito, tvAccesible;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurso_detail);

        int idRecurso = getIntent().getIntExtra("id_recurso", -1);
        if (idRecurso < 0) {
            Toast.makeText(this, "Recurso inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind views
        imgRecurso    = findViewById(R.id.imgRecursoDetail);
        tvServicio    = findViewById(R.id.tvServicioDetail);
        tvDescripcion = findViewById(R.id.tvDescripcionDetail);
        tvRequisitos  = findViewById(R.id.tvRequisitosDetail);
        tvEmail       = findViewById(R.id.tvEmailDetail);
        tvTelefono    = findViewById(R.id.tvTelefonoDetail);
        tvDireccion   = findViewById(R.id.tvDireccionDetail);
        tvHorario     = findViewById(R.id.tvHorarioDetail);
        tvWeb         = findViewById(R.id.tvWebDetail);
        tvGratuito    = findViewById(R.id.tvGratuitoDetail);
        tvAccesible   = findViewById(R.id.tvAccesibleDetail);

        // Llamada al servicio
        new ResourceService(this).fetchById(idRecurso, new ResourceService.ResourceDetailCallback() {
            @Override
            public void onSuccess(Recurso r) {
                // Imagen
                if (r.getImagen() != null && !r.getImagen().isEmpty()) {
                    Picasso.get()
                            .load(r.getImagen())
                            .placeholder(R.drawable.eva)
                            .into(imgRecurso);
                }
                // Texto
                tvServicio.setText(r.getServicio());
                tvDescripcion.setText("Descripción: " + r.getDescripcion());
                tvRequisitos.setText("Requisitos: " + r.getRequisitos());
                tvEmail.setText("Email: " + r.getEmail());
                tvTelefono.setText("Teléfono: " + r.getTelefono());
                tvDireccion.setText("Dirección: " + r.getDireccion());
                tvHorario.setText("Horario: " + r.getHorario());
                tvWeb.setText("Web: " + r.getWeb());
                tvGratuito.setText("Gratuito: " + (r.isGratuito() ? "Sí" : "No"));
                tvAccesible.setText("Accesible: " + (r.isAccesible() ? "Sí" : "No"));
            }

            @Override
            public void onError(Throwable t) {
                Toast.makeText(RecursoDetailActivity.this,
                        "Error al cargar recurso: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
