package com.example.appGrabacion.utils;

import com.example.appGrabacion.models.LoginRequest;
import com.example.appGrabacion.models.LoginResponse;
import com.example.appGrabacion.models.RegisterRequest;
import com.example.appGrabacion.models.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest body);

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);
}
