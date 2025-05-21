package com.example.appGrabacion.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Patterns;

import com.example.appGrabacion.contracts.LoginContract;
import com.example.appGrabacion.entities.LoginRequest;
import com.example.appGrabacion.entities.LoginResponse;
import com.example.appGrabacion.utils.AuthApi;
import com.example.appGrabacion.utils.RetrofitClient;
import com.example.appGrabacion.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginModel implements LoginContract.Service {
    private final AuthApi api;
    private final Context context;

    public LoginModel(Context context) {
        this.context = context;
        this.api = RetrofitClient.getRetrofitInstance(context).create(AuthApi.class);
    }

    @Override
    public void login(String email, String password, LoginContract.Service.LoginCallback callback) {
        // Validar formato email
        if (email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onError(new Exception("Formato de correo inválido"));
            return;
        }
        api.login(new LoginRequest(email, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().token;
                    Log.d("LoginService", "Token recibido del servidor: " + token);

                    // Guardar token localmente
                    SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("auth_token", token).apply();
                    new SessionManager(context).saveToken(token);

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
