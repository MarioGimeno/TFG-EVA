package com.example.appGrabacion.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appGrabacion.MainActivity;
import com.example.appGrabacion.R;
import com.example.appGrabacion.models.LoginRequest;
import com.example.appGrabacion.models.LoginResponse;
import com.example.appGrabacion.utils.AuthApi;
import com.example.appGrabacion.utils.RetrofitClient;
import com.example.appGrabacion.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private SessionManager session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(this);
        session.clear();

        EditText etEmail    = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin     = findViewById(R.id.btnLogin);
        TextView tvReg      = findViewById(R.id.tvGoRegister);

        AuthApi api = RetrofitClient.getRetrofitInstance(this)
                .create(AuthApi.class);
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            api.login(new LoginRequest(email, pass))
                    .enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> res) {
                            if (res.isSuccessful() && res.body()!=null) {
                                String jwt = res.body().token;  // <-- aquí tienes tu token

                                // 1) lo guardas en SharedPreferences
                                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                                prefs.edit()
                                        .putString("auth_token", jwt)
                                        .apply();

                                // 2) también lo guardas en tu SessionManager si lo usas
                                session.saveToken(jwt);

                                // 3) y ya puedes ir al MainActivity
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Credenciales inválidas", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            Toast.makeText(LoginActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tvReg.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
