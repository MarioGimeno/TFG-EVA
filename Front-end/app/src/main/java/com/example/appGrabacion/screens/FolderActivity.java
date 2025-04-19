// FolderActivity.java
package com.example.appGrabacion.screens;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.FileAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private final List<String> urls = new ArrayList<>();
    private FileAdapter adapter;
    private String token; // <-- guardamos el token

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        token = prefs.getString("auth_token", "");
        if (token.isEmpty()) {
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        RecyclerView rv = findViewById(R.id.rvFiles);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(this, urls);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabUpload);
        fab.setOnClickListener(v -> pickFile());

        loadFiles();
    }

    private void pickFile() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "image/*","video/*","application/pdf","text/plain"
        });
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, REQUEST_PICK_FILE);
    }

    @Override
    protected void onActivityResult(int request, int result, @Nullable Intent data) {
        super.onActivityResult(request, result, data);
        if (request == REQUEST_PICK_FILE && result == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            uploadFile(uri);
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
                    .url("http://192.168.1.140:3000/api/files")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(body)
                    .build();

            client.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call c, java.io.IOException e) {
                    runOnUiThread(() -> Toast.makeText(FolderActivity.this,
                            "Error al subir: "+e.getMessage(), Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(Call c, Response r) throws java.io.IOException {
                    if (r.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(FolderActivity.this,
                                    "Subida exitosa", Toast.LENGTH_SHORT).show();
                            loadFiles();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(FolderActivity.this,
                                "Error servidor: "+r.code(), Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception ex) {
            Toast.makeText(this, "Error lectura: "+ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFiles() {
        Request req = new Request.Builder()
                .url("http://192.168.1.140:3000/api/files")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call c, java.io.IOException e) { /*…*/ }
            @Override public void onResponse(Call c, Response r) throws java.io.IOException {
                if (!r.isSuccessful()) return;
                try {
                    JSONObject obj = new JSONObject(r.body().string());
                    JSONArray arr = obj.getJSONArray("files");
                    urls.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        // Ahora cada elemento es un objeto { name, url }
                        JSONObject fileObj = arr.getJSONObject(i);
                        String signedUrl = fileObj.getString("url");
                        Log.d("FolderActivity", "Signed URL[" + i + "]: " + signedUrl);
                        urls.add(signedUrl);
                    }
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                } catch (Exception x) { x.printStackTrace(); }
            }
        });
    }
}
