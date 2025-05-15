// com/example/appGrabacion/utils/FolderService.java
package com.example.appGrabacion.services;

import android.content.Context;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.database.Cursor;
import java.io.InputStream;
import java.util.List;

import com.example.appGrabacion.models.FileEntry;
import com.example.appGrabacion.models.FilesResponse;
import com.example.appGrabacion.utils.FolderApi;
import com.example.appGrabacion.utils.RetrofitClient;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FolderService {
    private final FolderApi api;
    private final Context ctx;

    public interface FilesCallback {
        void onSuccess(List<FileEntry> files);
        void onError(Throwable t);
    }

    public interface UploadCallback {
        void onSuccess();
        void onError(Throwable t);
    }

    public FolderService(Context ctx) {
        this.ctx = ctx;
        api = RetrofitClient
                .getRetrofitInstance(ctx)
                .create(FolderApi.class);
    }

    public void fetchFiles(String token, final FilesCallback cb) {
        api.getFiles("Bearer " + token)
                .enqueue(new Callback<FilesResponse>() {
                    @Override
                    public void onResponse(Call<FilesResponse> call, Response<FilesResponse> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            cb.onSuccess(resp.body().getFiles());
                        } else {
                            cb.onError(new RuntimeException("Código:" + resp.code()));
                        }
                    }
                    @Override public void onFailure(Call<FilesResponse> call, Throwable t) {
                        cb.onError(t);
                    }
                });
    }

    public void uploadFile(String token, Uri uri, final UploadCallback cb) {
        try {
            // 1) obtener mime y nombre
            String mime = ctx.getContentResolver().getType(uri);
            String name = null;
            // extraer displayName de ContentProvider si está
            Cursor c = ctx.getContentResolver()
                    .query(uri, null, null, null, null);
            if (c != null) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0 && c.moveToFirst()) {
                    name = c.getString(idx);
                }
                c.close();
            }
            if (name == null) {
                name = uri.getLastPathSegment();
            }

            // 2) leer bytes
            InputStream in = ctx.getContentResolver().openInputStream(uri);
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            in.close();

            // 3) construir MultipartBody.Part
            RequestBody fileBody = RequestBody.create(MediaType.parse(mime), bytes);
            MultipartBody.Part part = MultipartBody.Part
                    .createFormData("file", name, fileBody);

            // 4) llamada Retrofit
            api.uploadFile("Bearer " + token, part)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> r) {
                            if (r.isSuccessful()) {
                                cb.onSuccess();
                            } else {
                                cb.onError(new RuntimeException("Código:" + r.code()));
                            }
                        }
                        @Override public void onFailure(Call<Void> call, Throwable t) {
                            cb.onError(t);
                        }
                    });
        } catch (Exception ex) {
            cb.onError(ex);
        }
    }
}
