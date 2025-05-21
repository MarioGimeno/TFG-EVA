package com.example.appGrabacion.models;

import android.content.Context;
import com.example.appGrabacion.contracts.CategoriasContract;
import com.example.appGrabacion.entities.Categoria;
import com.example.appGrabacion.utils.CategoriasApi;
import com.example.appGrabacion.utils.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class CategoriaModel implements CategoriasContract.Service {
    private final CategoriasApi api;

    public CategoriaModel(Context ctx) {
        api = RetrofitClient
                .getRetrofitInstance(ctx)
                .create(CategoriasApi.class);
    }

    @Override
    public void fetchAll(final Callback<List<Categoria>> callback) {
        api.getCategorias().enqueue(new retrofit2.Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new RuntimeException("Código: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    @Override
    public void fetchById(int id, final Callback<Categoria> callback) {
        api.getCategoriaById(id).enqueue(new retrofit2.Callback<Categoria>() {
            @Override
            public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new RuntimeException("Código: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<Categoria> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
