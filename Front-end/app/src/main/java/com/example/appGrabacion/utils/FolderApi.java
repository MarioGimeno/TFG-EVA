// com/example/appGrabacion/utils/FolderApi.java
package com.example.appGrabacion.utils;

import com.example.appGrabacion.entities.FilesResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface FolderApi {
    @Multipart
    @POST("api/files")
    Call<Void> uploadFile(
            @Header("Authorization") String auth,
            @Part MultipartBody.Part file
    );

    @GET("api/files")
    Call<FilesResponse> getFiles(
            @Header("Authorization") String auth
    );
}
