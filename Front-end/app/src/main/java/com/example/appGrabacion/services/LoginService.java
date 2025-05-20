package com.example.appGrabacion.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.appGrabacion.models.LoginRequest;
import com.example.appGrabacion.models.LoginResponse;
import com.example.appGrabacion.utils.AuthApi;
import com.example.appGrabacion.utils.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginService {
    private final AuthApi api;
    private final Context context;

    public interface LoginCallback {
        void onSuccess(String token);
        void onError(Throwable t);
    }

    public LoginService(Context context) {
        this.context = context;
        this.api = RetrofitClient.getRetrofitInstance(context).create(AuthApi.class);
    }

    public void login(String email, String password, LoginCallback callback) {
        api.login(new LoginRequest(email, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().token;

                    // Guardar token localmente
                    SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("auth_token", token).apply();

                    callback.onSuccess(token);
                } else {
                    callback.onError(new Exception("Credenciales inválidas"));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
