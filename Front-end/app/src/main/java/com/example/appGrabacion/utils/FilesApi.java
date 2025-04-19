package com.example.appGrabacion.utils;

import com.example.appGrabacion.models.FilesResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FilesApi {
    @GET("api/files")
    Call<FilesResponse> getFiles();
}
