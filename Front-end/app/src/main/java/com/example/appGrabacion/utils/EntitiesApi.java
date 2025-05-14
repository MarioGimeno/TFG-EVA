package com.example.appGrabacion.utils;

// 1) EntitiesApi.java


import com.example.appGrabacion.models.Entidad;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface EntitiesApi {
    /**
     * GET /api/entidades
     * No requiere Authorization
     */
    @GET("/api/entidades")
    Call<List<Entidad>> getEntities();
}
