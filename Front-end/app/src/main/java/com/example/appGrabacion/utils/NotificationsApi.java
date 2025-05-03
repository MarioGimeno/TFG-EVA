package com.example.appGrabacion.utils;

import com.example.appGrabacion.models.EmailRequest;
import com.example.appGrabacion.models.EmailResponse;
import com.example.appGrabacion.models.LocationUpdateRequest;
import com.example.appGrabacion.models.NotifyRequest;
import com.example.appGrabacion.models.NotifyResponse;

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
