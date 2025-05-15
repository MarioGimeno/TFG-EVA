package com.example.appGrabacion.screens;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FolderActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_FILE = 1234;
    private final OkHttpClient client = new OkHttpClient();
    private final List<FileEntry> files = new ArrayList<>();
    private FileAdapter adapter;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(FolderActivity.this, MainActivity.class));
            finish();
        });

        // 0) Comprueba sesión
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        token = prefs.getString("auth_token", "");
        if (token.isEmpty()) {
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 1) Inicializa RecyclerView
        RecyclerView rv = findViewById(R.id.rvFiles);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(this, files);
        rv.setAdapter(adapter);

        // 2) Listener del botón
        ImageView imgUpload = findViewById(R.id.imgUpload);
        imgUpload.setOnClickListener(v -> pickFile());

        // 3) Ajuste dinámico del topMargin para que la tarjeta suba por arriba
        ImageView videoBg   = findViewById(R.id.videoBackground);
        FrameLayout wrapper = findViewById(R.id.card_wrapper);

        // Toma los LayoutParams de ConstraintLayout (tu padre real)
        ConstraintLayout.LayoutParams params =
                (ConstraintLayout.LayoutParams) wrapper.getLayoutParams();
        // Guarda el marginTop inicial (–40dp)
        final int initialMarginTop = params.topMargin;

        videoBg.post(() -> {
            // 40dp convertidos a px
            final int overlapPx = Math.round(
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            40,
                            getResources().getDisplayMetrics()
                    )
            );
            // Altura real de la imagen
            final int imgH = videoBg.getHeight();
            // Scroll máximo: cubrir toda la imagen menos esos 40dp
            final int maxScroll = imgH - overlapPx;

            rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                int accumulatedDy = 0;
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    // Clamp de 0 a maxScroll
                    accumulatedDy = Math.max(0, Math.min(accumulatedDy + dy, maxScroll));
                    // Restamos accumulatedDy al marginTop inicial
                    params.topMargin = initialMarginTop - accumulatedDy;
                    wrapper.setLayoutParams(params);
                }
            });
        });

        // 4) Carga inicial de archivos
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
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == REQUEST_PICK_FILE && result == Activity.RESULT_OK && data != null) {
            uploadFile(data.getData());
        }
    }

    private void uploadFile(Uri uri) {
        try {
            String mime = getContentResolver().getType(uri);
            String name = uri.getLastPathSegment();
            InputStream in = getContentResolver().openInputStream(uri);
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            in.close();

            RequestBody fileBody = RequestBody.create(MediaType.parse(mime), bytes);
            MultipartBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", name, fileBody)
                    .build();

            Request req = new Request.Builder()
                    .url("http://192.168.1.20:3000/api/files")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(body)
                    .build();

            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, java.io.IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(FolderActivity.this,
                                    "Error al subir: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
                }
                @Override public void onResponse(Call call, Response r) throws java.io.IOException {
                    if (r.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(FolderActivity.this,
                                    "Subida exitosa", Toast.LENGTH_SHORT).show();
                            loadFiles();
                        });
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(FolderActivity.this,
                                        "Error servidor: " + r.code(),
                                        Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });
        } catch (Exception ex) {
            Toast.makeText(this,
                    "Error lectura: " + ex.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFiles() {
        Request req = new Request.Builder()
                .url("http://192.168.1.20:3000/api/files")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, java.io.IOException e) { }
            @Override public void onResponse(Call call, Response r) throws java.io.IOException {
                if (!r.isSuccessful()) return;
                try {
                    JSONObject obj = new JSONObject(r.body().string());
                    JSONArray arr = obj.getJSONArray("files");
                    files.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        String fullPath = o.getString("name");
                        String name = fullPath.contains("/")
                                ? fullPath.substring(fullPath.lastIndexOf('/') + 1)
                                : fullPath;
                        String url     = o.getString("url");
                        String created = o.getString("created");
                        long   size    = o.getLong("size");
                        files.add(new FileEntry(name, url, created, size));
                        Log.d("FolderActivity",
                                "Entry[" + i + "] " + name +
                                        " (" + size + " B, " + created + ") → " + url
                        );
                    }
                    runOnUiThread(adapter::notifyDataSetChanged);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
