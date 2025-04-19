package com.example.appGrabacion.utils;

import com.example.appGrabacion.models.FileEntry;
import com.example.appGrabacion.models.FilesResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface FilesApi {
    @GET("/api/files")
    Call<List<FileEntry>> listUserFiles(@Header("Authorization") String bearerToken);
}
