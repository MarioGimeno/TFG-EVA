package com.example.appGrabacion.utils;

import com.example.appGrabacion.entities.Categoria;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CategoriasApi {
    @GET("/api/categorias")
    Call<List<Categoria>> getCategorias();

    @GET("/api/categorias/{id}")
    Call<Categoria> getCategoriaById(@Path("id") int id);
}