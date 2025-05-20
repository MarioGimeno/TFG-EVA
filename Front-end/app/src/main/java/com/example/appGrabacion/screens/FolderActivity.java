package com.example.appGrabacion.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.MainActivity;
import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.FileAdapter;
import com.example.appGrabacion.contracts.FolderContract;
import com.example.appGrabacion.models.FileEntry;
import com.example.appGrabacion.presenters.FolderPresenter;
import com.example.appGrabacion.services.FolderService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FolderActivity extends AppCompatActivity implements FolderContract.View {

    private static final int REQUEST_PICK_FILE = 1234;

    private final List<FileEntry> files = new ArrayList<>();
    private FileAdapter adapter;
    private String token;
    private FolderPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(FolderActivity.this, MainActivity.class));
            finish();
        });

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        token = prefs.getString("auth_token", "");
        if (token.isEmpty()) {
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        RecyclerView rv = findViewById(R.id.rvFiles);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(this, files);
        rv.setAdapter(adapter);

        // Scroll dinámico (igual que antes)
        ImageView videoBg = findViewById(R.id.videoBackground);
        FrameLayout wrapper = findViewById(R.id.card_wrapper);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) wrapper.getLayoutParams();
        final int initialMarginTop = params.topMargin;
        videoBg.post(() -> {
            int overlapPx = Math.round(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    40,
                    getResources().getDisplayMetrics()));
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

        presenter = new FolderPresenter(new FolderService(this));
        presenter.attachView(this);
        presenter.loadFiles(token);

        findViewById(R.id.imgUpload).setOnClickListener(v -> pickFile());
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                presenter.uploadFile(token, uri);
            }
        }
    }

    @Override
    public void showLoading() {
        // Puedes mostrar un progress bar
    }

    @Override
    public void hideLoading() {
        // Ocultar progress bar
    }

    @Override
    public void showFiles(List<FileEntry> fetchedFiles) {
        // Ordenar por fecha descendente
        Collections.sort(fetchedFiles, new Comparator<FileEntry>() {
            @Override
            public int compare(FileEntry f1, FileEntry f2) {
                return f2.getCreated().compareTo(f1.getCreated());
            }
        });
        files.clear();
        files.addAll(fetchedFiles);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void showUploadSuccess() {
        Toast.makeText(this, "Subida exitosa", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }
}
