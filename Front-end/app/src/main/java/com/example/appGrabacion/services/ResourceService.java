package com.example.appGrabacion.services;

import android.content.Context;
import com.example.appGrabacion.models.Recurso;
import com.example.appGrabacion.utils.ResourcesApi;
import com.example.appGrabacion.utils.RetrofitClient;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResourceService {

    private final ResourcesApi api;

    public ResourceService(Context ctx) {
        api = RetrofitClient
                .getRetrofitInstance(ctx)
                .create(ResourcesApi.class);
    }

    /** Callback propio para resultados de Recurso */
    public interface ResourceCallback {
        void onSuccess(List<Recurso> recursos);
        void onError(Throwable t);
    }

    /** Obtiene la lista completa de recursos */
    public void fetchAll(final ResourceCallback callback) {
        api.getResources().enqueue(new Callback<List<Recurso>>() {
            @Override
            public void onResponse(Call<List<Recurso>> call,
                                   Response<List<Recurso>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    callback.onSuccess(resp.body());
                } else {
                    callback.onError(new RuntimeException("Código: " + resp.code()));
                }
            }

            @Override
            public void onFailure(Call<List<Recurso>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
