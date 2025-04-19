package com.example.appGrabacion.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appGrabacion.R;
import com.example.appGrabacion.models.RegisterRequest;
import com.example.appGrabacion.models.RegisterResponse;
import com.example.appGrabacion.screens.LoginActivity;
import com.example.appGrabacion.utils.AuthApi;
import com.example.appGrabacion.utils.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etEmail    = findViewById(R.id.etRegEmail);
        EditText etPassword = findViewById(R.id.etRegPassword);
        Button btnRegister  = findViewById(R.id.btnRegister);

        AuthApi api = RetrofitClient.getRetrofitInstance(this)
                .create(AuthApi.class);
        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();

            Log.d(TAG, "Click REGISTER → email=" + email + " pass=" + pass);

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            RegisterRequest body = new RegisterRequest(email, pass);
            Log.d(TAG, "Enviando petición POST /auth/register con body: " + body.email);

            api.register(body)
                    .enqueue(new Callback<RegisterResponse>() {
                        @Override
                        public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> res) {
                            Log.d(TAG, "onResponse code=" + res.code() + " body=" + res.body());
                            if (res.isSuccessful() && res.body() != null) {
                                Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Error al registrar: " + res.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<RegisterResponse> call, Throwable t) {
                            Log.e(TAG, "onFailure", t);
                            Toast.makeText(RegisterActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
