package com.example.appGrabacion.models;

import android.content.Context;

import com.example.appGrabacion.contracts.ResourceContract;
import com.example.appGrabacion.entities.Recurso;
import com.example.appGrabacion.utils.ResourcesApi;
import com.example.appGrabacion.utils.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResourceModel implements ResourceContract.Service {

    private final ResourcesApi api;

    public ResourceModel(Context ctx) {
        api = RetrofitClient
                .getRetrofitInstance(ctx)
                .create(ResourcesApi.class);
    }

    @Override
    public void fetchAll(final ResourceCallback callback) {
        api.getResources().enqueue(new Callback<List<Recurso>>() {
            @Override
            public void onResponse(Call<List<Recurso>> call, Response<List<Recurso>> resp) {
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

    @Override
    public void fetchByCategory(int categoria, final ResourceCallback callback) {
        api.getResourcesByCategoria(categoria).enqueue(new Callback<List<Recurso>>() {
            @Override
            public void onResponse(Call<List<Recurso>> call, Response<List<Recurso>> resp) {
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

    @Override
    public void fetchById(int id, final ResourceDetailCallback callback) {
        api.getResourceById(id).enqueue(new Callback<Recurso>() {
            @Override
            public void onResponse(Call<Recurso> call, Response<Recurso> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    callback.onSuccess(resp.body());
                } else {
                    callback.onError(new RuntimeException("Código: " + resp.code()));
                }
            }

            @Override
            public void onFailure(Call<Recurso> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    @Override
    public void fetchGratuitos(final ResourceCallback callback) {
        api.getResourcesGratuitos().enqueue(new Callback<List<Recurso>>() {
            @Override
            public void onResponse(Call<List<Recurso>> call, Response<List<Recurso>> resp) {
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

    @Override
    public void fetchAccesibles(final ResourceCallback callback) {
        api.getResourcesAccesibles().enqueue(new Callback<List<Recurso>>() {
            @Override
            public void onResponse(Call<List<Recurso>> call, Response<List<Recurso>> resp) {
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
