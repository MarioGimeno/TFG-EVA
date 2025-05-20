package com.example.appGrabacion.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.appGrabacion.contracts.RegisterContract;
import com.example.appGrabacion.models.RegisterRequest;
import com.example.appGrabacion.models.RegisterResponse;
import com.example.appGrabacion.utils.AuthApi;
import com.example.appGrabacion.utils.RetrofitClient;
import com.example.appGrabacion.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Response;

public class RegisterModel implements RegisterContract.Service {

    private static final String TAG = "RegisterService";
    private final AuthApi api;
    private Context context;

    public RegisterModel(Context context) {
        this.context = context;  // guardamos el contexto
        api = RetrofitClient.getRetrofitInstance(context).create(AuthApi.class);
    }
    @Override
    public void performRegister(String fullName, String email, String password, Callback callback) {
        RegisterRequest body = new RegisterRequest(fullName, email, password);

        api.register(body).enqueue(new retrofit2.Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken(); // Asumiendo que RegisterResponse tiene método getToken()

                    // Guardar token localmente
                    SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("auth_token", token).apply();
                    new SessionManager(context).saveToken(token);

                    callback.onSuccess(token);
                } else {
                    callback.onError("Error al registrar: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                callback.onError("Error de red");
            }
        });
    }


}
