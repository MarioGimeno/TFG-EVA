package com.example.appGrabacion.utils;

import com.example.appGrabacion.entities.LocationUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface NotificationsApi {


    @POST("api/notifications/location")    // <-- añade el /api
    Call<Void> sendLocationUpdate(
            @Header("Authorization") String bearer,
            @Body LocationUpdateRequest body
    );
}
