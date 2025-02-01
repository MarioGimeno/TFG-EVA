package com.example.intentoandroid;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("/upload")
    Call<ResponseBody> uploadVideoAndAudio(
            @Part MultipartBody.Part video,
            @Part MultipartBody.Part audio,
            @Part MultipartBody.Part location
    );
    @Multipart
    @POST("/upload-video-location")
    Call<ResponseBody> uploadVideoAndLocation(
            @Part MultipartBody.Part video,
              @Part MultipartBody.Part location
    );
}
