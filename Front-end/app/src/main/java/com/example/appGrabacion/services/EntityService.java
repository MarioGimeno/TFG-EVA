// EntityService.java
package com.example.appGrabacion.utils;

import android.content.Context;
import com.example.appGrabacion.models.Entidad;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Servicio para obtener Entidades desde la API.
 * No expone Retrofit directamente, sino un listener propio.
 */
public class EntityService {

    private final EntitiesApi api;

    public EntityService(Context ctx) {
        api = RetrofitClient
                .getRetrofitInstance(ctx)
                .create(EntitiesApi.class);
    }

    /**
     * Callback genérico para resultados de Entidad.
     */
    public interface EntityCallback {
        void onSuccess(List<Entidad> entidades);
        void onError(Throwable t);
    }

    /**
     * Lanza la petición GET /api/entidades y devuelve resultado
     * vía EntityCallback.
     */
    public void fetchAll(final EntityCallback callback) {
        api.getEntities().enqueue(new Callback<List<Entidad>>() {
            @Override
            public void onResponse(Call<List<Entidad>> call, Response<List<Entidad>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    callback.onSuccess(resp.body());
                } else {
                    callback.onError(new RuntimeException("Código: " + resp.code()));
                }
            }

            @Override
            public void onFailure(Call<List<Entidad>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
