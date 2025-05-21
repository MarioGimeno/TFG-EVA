package com.example.appGrabacion.utils;

import com.example.appGrabacion.entities.LoginRequest;
import com.example.appGrabacion.entities.LoginResponse;
import com.example.appGrabacion.entities.RegisterRequest;
import com.example.appGrabacion.entities.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest body);

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);
}
