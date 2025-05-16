package com.example.appGrabacion.utils;

import android.content.Context;
import com.example.appGrabacion.models.Categoria;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoriaService {
    private final CategoriasApi api;

    public interface CategoriaCallback {
        void onSuccess(List<Categoria> categorias);
        void onError(Throwable t);
    }

    public interface CategoriaDetailCallback {
        void onSuccess(Categoria categoria);
        void onError(Throwable t);
    }

    public CategoriaService(Context ctx) {
        api = RetrofitClient
                .getRetrofitInstance(ctx)
                .create(CategoriasApi.class);
    }

    public void fetchAll(final CategoriaCallback cb) {
        api.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override public void onResponse(Call<List<Categoria>> c, Response<List<Categoria>> r) {
                if (r.isSuccessful() && r.body() != null) cb.onSuccess(r.body());
                else cb.onError(new RuntimeException("Código:"+r.code()));
            }
            @Override public void onFailure(Call<List<Categoria>> c, Throwable t) { cb.onError(t); }
        });
    }

    public void fetchById(int id, final CategoriaDetailCallback cb) {
        api.getCategoriaById(id).enqueue(new Callback<Categoria>() {
            @Override
            public void onResponse(Call<Categoria> call, Response<Categoria> r) {
                if (r.isSuccessful() && r.body()!=null) cb.onSuccess(r.body());
                else cb.onError(new RuntimeException("Código:"+r.code()));
            }


            @Override public void onFailure(Call<Categoria> c, Throwable t) { cb.onError(t); }
        });
    }
}