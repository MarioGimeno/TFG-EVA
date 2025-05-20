package com.example.appGrabacion.services;

import android.content.Context;
import com.example.appGrabacion.contracts.EntidadesContract;
import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.utils.EntitiesApi;
import com.example.appGrabacion.utils.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class EntityModel implements EntidadesContract.Service {
    private final EntitiesApi api;

    public interface EntityCallback {
        void onSuccess(List<Entidad> entidades);
        void onError(Throwable t);
    }
    public interface EntityDetailCallback {
        void onSuccess(Entidad entidad);
        void onError(Throwable t);
    }

    // Constructor debe llamarse igual que la clase
    public EntityModel(Context ctx) {
        api = RetrofitClient
                .getRetrofitInstance(ctx)
                .create(EntitiesApi.class);
    }

    @Override
    public void fetchAll(final EntityCallback cb) {
        api.getEntities().enqueue(new Callback<List<Entidad>>() {
            @Override public void onResponse(Call<List<Entidad>> call, Response<List<Entidad>> response) {
                if (response.isSuccessful() && response.body() != null) cb.onSuccess(response.body());
                else cb.onError(new RuntimeException("Código:" + response.code()));
            }
            @Override public void onFailure(Call<List<Entidad>> call, Throwable t) { cb.onError(t); }
        });
    }

    @Override
    public void fetchById(int id, final EntityDetailCallback cb) {
        api.getEntityById(id).enqueue(new Callback<Entidad>() {
            @Override public void onResponse(Call<Entidad> call, Response<Entidad> response) {
                if (response.isSuccessful() && response.body() != null) cb.onSuccess(response.body());
                else cb.onError(new RuntimeException("Código:" + response.code()));
            }
            @Override public void onFailure(Call<Entidad> call, Throwable t) { cb.onError(t); }
        });
    }
}
