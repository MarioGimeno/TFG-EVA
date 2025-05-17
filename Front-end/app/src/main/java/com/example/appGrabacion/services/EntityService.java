// com/example/appGrabacion/utils/EntityService.java
package com.example.appGrabacion.services;

import android.content.Context;
import com.example.appGrabacion.models.Entidad;
import com.example.appGrabacion.utils.EntitiesApi;
import com.example.appGrabacion.utils.RetrofitClient;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EntityService {
    private final EntitiesApi api;

    public interface EntityCallback {
        void onSuccess(List<Entidad> entidades);
        void onError(Throwable t);
    }
    public interface EntityDetailCallback {
        void onSuccess(Entidad entidad);
        void onError(Throwable t);
    }

    public EntityService(Context ctx) {
        api = RetrofitClient
                .getRetrofitInstance(ctx)
                .create(EntitiesApi.class);
    }

    public void fetchAll(final EntityCallback cb) {
        api.getEntities().enqueue(new Callback<List<Entidad>>() {
            @Override public void onResponse(Call<List<Entidad>> c, Response<List<Entidad>> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(r.body());
                else cb.onError(new RuntimeException("Código:"+r.code()));
            }
            @Override public void onFailure(Call<List<Entidad>> c, Throwable t) { cb.onError(t); }
        });
    }

    public void fetchById(int id, final EntityDetailCallback cb) {
        api.getEntityById(id).enqueue(new Callback<Entidad>() {
            @Override public void onResponse(Call<Entidad> c, Response<Entidad> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(r.body());
                else cb.onError(new RuntimeException("Código:"+r.code()));
            }
            @Override public void onFailure(Call<Entidad> c, Throwable t) { cb.onError(t); }
        });
    }
}
