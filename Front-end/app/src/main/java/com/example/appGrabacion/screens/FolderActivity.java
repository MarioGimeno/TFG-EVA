package com.example.appGrabacion.screens;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appGrabacion.R;
import com.example.appGrabacion.adapters.FileAdapter;
import com.example.appGrabacion.models.FilesResponse;
import com.example.appGrabacion.utils.ApiService;
import com.example.appGrabacion.utils.FilesApi;
import com.example.appGrabacion.utils.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// FolderActivity.java
public class FolderActivity extends AppCompatActivity {
    private RecyclerView rv;
    private FileAdapter adapter;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_folder);
        rv = findViewById(R.id.recyclerFiles);
        rv.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas
        fetchFiles();
    }
    private void fetchFiles() {
        FilesApi api = RetrofitClient
                .getRetrofitInstance(this)
                .create(FilesApi.class);

        api.getFiles().enqueue(new Callback<FilesResponse>() {
            @Override
            public void onResponse(Call<FilesResponse> c, Response<FilesResponse> r) {
                if (r.isSuccessful() && r.body()!=null) {
                    new FileAdapter(FolderActivity.this, r.body().getFiles())
                            .notifyDataSetChanged();
                    rv.setAdapter(new FileAdapter(
                            FolderActivity.this, r.body().getFiles()
                    ));
                } else {
                    Toast.makeText(FolderActivity.this,
                            "Error al cargar archivos", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<FilesResponse> c, Throwable t) {
                Toast.makeText(FolderActivity.this,
                        "Red fallida: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
