package com.example.intentoandroid.utils;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("upload-chunk")
    Call<ResponseBody> uploadChunk(
            @Part("fileId") RequestBody fileId,
            @Part("chunkIndex") RequestBody chunkIndex,
            @Part("totalChunks") RequestBody totalChunks,
            @Part MultipartBody.Part chunkData
    );
}
