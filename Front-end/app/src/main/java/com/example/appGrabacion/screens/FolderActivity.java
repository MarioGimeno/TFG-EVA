package com.example.appGrabacion.screens;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.MainActivity;
import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.FileAdapter;
import com.example.appGrabacion.models.FileEntry;
import com.example.appGrabacion.utils.FolderService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_FILE = 1234;

    private final List<FileEntry> files = new ArrayList<>();
    private FileAdapter adapter;
    private String token;
    private FolderService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        // Botón de vuelta a Main
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(FolderActivity.this, MainActivity.class));
            finish();
        });

        // Comprueba sesión
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        token = prefs.getString("auth_token", "");
        if (token.isEmpty()) {
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // RecyclerView
        RecyclerView rv = findViewById(R.id.rvFiles);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(this, files);
        rv.setAdapter(adapter);

        // Servicio Retrofit
        service = new FolderService(this);

        // Botón subir
        findViewById(R.id.imgUpload).setOnClickListener(v -> pickFile());

        // Scroll dinámico
        ImageView videoBg = findViewById(R.id.videoBackground);
        FrameLayout wrapper = findViewById(R.id.card_wrapper);
        ConstraintLayout.LayoutParams params =
                (ConstraintLayout.LayoutParams) wrapper.getLayoutParams();
        final int initialMarginTop = params.topMargin;
        videoBg.post(() -> {
            int overlapPx = Math.round(
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            40,
                            getResources().getDisplayMetrics()
                    )
            );
            int maxScroll = videoBg.getHeight() - overlapPx;
            rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                int accumulatedDy = 0;
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    accumulatedDy = Math.max(0, Math.min(accumulatedDy + dy, maxScroll));
                    params.topMargin = initialMarginTop - accumulatedDy;
                    wrapper.setLayoutParams(params);
                }
            });
        });

        // Carga inicial
        loadFiles();
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "image/*", "video/*", "application/pdf", "text/plain"
        });
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FILE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                uploadFile(uri);
            }
        }
    }

    private void loadFiles() {
        service.fetchFiles(token, new FolderService.FilesCallback() {
            @Override
            public void onSuccess(List<FileEntry> fetched) {
                files.clear();
                files.addAll(fetched);
                runOnUiThread(adapter::notifyDataSetChanged);
            }

            @Override
            public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(FolderActivity.this,
                                "Error al cargar: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void uploadFile(Uri uri) {
        service.uploadFile(token, uri, new FolderService.UploadCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(FolderActivity.this,
                            "Subida exitosa", Toast.LENGTH_SHORT).show();
                    loadFiles();
                });
            }

            @Override
            public void onError(Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(FolderActivity.this,
                                "Error al subir: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
