package com.example.appGrabacion.utils;

import com.example.appGrabacion.entities.Recurso;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ResourcesApi {
    /** GET /api/servicios — no requiere Authorization */
    @GET("/api/servicios")
    Call<List<Recurso>> getResources();

    /** GET /api/servicios/{id} */
    @GET("/api/servicios/{id}")
    Call<Recurso> getResourceById(@Path("id") int id);

    @GET("/api/servicios/categoria/{id}")
    Call<List<Recurso>> getResourcesByCategoria(@Path("id") int id);

    @GET("/api/servicios/gratuitos")
    Call<List<Recurso>> getResourcesGratuitos();

    @GET("/api/servicios/accesibles")
    Call<List<Recurso>> getResourcesAccesibles();
}
