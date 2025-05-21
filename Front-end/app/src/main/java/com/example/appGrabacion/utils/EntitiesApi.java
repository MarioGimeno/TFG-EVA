package com.example.appGrabacion.utils;

// 1) EntitiesApi.java


import com.example.appGrabacion.entities.Entidad;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface EntitiesApi {
    /**
     * GET /api/entidades
     * No requiere Authorization
     */
    @GET("/api/entidades")
    Call<List<Entidad>> getEntities();
    @GET("/api/entidades/{id}")
    Call<Entidad> getEntityById(@Path("id") int id);
}
