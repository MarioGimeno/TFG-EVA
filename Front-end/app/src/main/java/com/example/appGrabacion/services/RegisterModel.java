package com.example.appGrabacion.services;

import android.content.Context;
import android.util.Log;

import com.example.appGrabacion.contracts.RegisterContract;
import com.example.appGrabacion.models.RegisterRequest;
import com.example.appGrabacion.models.RegisterResponse;
import com.example.appGrabacion.utils.AuthApi;
import com.example.appGrabacion.utils.RetrofitClient;

import retrofit2.Call;
import retrofit2.Response;

public class RegisterModel implements RegisterContract.Service {

    private static final String TAG = "RegisterService";
    private final AuthApi api;

    public RegisterModel(Context context) {
        api = RetrofitClient.getRetrofitInstance(context).create(AuthApi.class);
    }

    @Override
    public void performRegister(String email, String password, Callback callback) {
        RegisterRequest body = new RegisterRequest(email, password);
        Log.d(TAG, "Performing register with email: " + email);

        api.register(body).enqueue(new retrofit2.Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                Log.d(TAG, "Register response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error al registrar: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e(TAG, "Register failure", t);
                callback.onError("Error de red");
            }
        });
    }
}
