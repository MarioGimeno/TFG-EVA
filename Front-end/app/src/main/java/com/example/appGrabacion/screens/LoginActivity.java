package com.example.appGrabacion.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appGrabacion.MainActivity;
import com.example.appGrabacion.R;
import com.example.appGrabacion.models.LoginRequest;
import com.example.appGrabacion.models.LoginResponse;
import com.example.appGrabacion.utils.AuthApi;
import com.example.appGrabacion.utils.RetrofitClient;
import com.example.appGrabacion.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private SessionManager session;
    private LinearLayout loginForm, registerForm;
    private TextView tvGoRegister, txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 0) Inicializar sesión y limpiar tokens previos
        session = new SessionManager(this);
        session.clear();

        // 1) Referencias a layouts y toggles
        loginForm      = findViewById(R.id.login_form);
        registerForm   = findViewById(R.id.register_form);
        tvGoRegister   = findViewById(R.id.tvGoRegister);
        txtLogin       = findViewById(R.id.txtLogin);

        // Estado inicial: mostrar login, ocultar registro
        loginForm.setVisibility(View.VISIBLE);
        registerForm.setVisibility(View.GONE);

        // 2) Campos y botones de login
        EditText etEmail    = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin     = findViewById(R.id.btnLogin);

        // 3) Botón de registro (dentro del formulario de registro)
        MaterialButton btnRegister = findViewById(R.id.btnRegister);

        // 4) Video de fondo
        VideoView videoBg = findViewById(R.id.videoBackground);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.evalogin2);
        videoBg.setVideoURI(videoUri);
        videoBg.setMediaController(null);
        videoBg.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f);
        });
        videoBg.start();

        // 5) API de autenticación
        AuthApi api = RetrofitClient.getRetrofitInstance(this)
                .create(AuthApi.class);

        // 6) Lógica de login
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
                            if (res.isSuccessful() && res.body() != null) {
                                String jwt = res.body().token;
                                // Guardar token
                                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                                prefs.edit().putString("auth_token", jwt).apply();
                                session.saveToken(jwt);
                                // Ir a MainActivity
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

        // 7) Toggle a registro al pulsar "¿No tienes cuenta? Regístrate aquí"
        tvGoRegister.setOnClickListener(v -> {
            loginForm.setVisibility(View.GONE);
            registerForm.setVisibility(View.VISIBLE);
        });

        // 8) Toggle a login al pulsar "¿Ya tienes cuenta? Inicia sesión aquí"
        txtLogin.setOnClickListener(v -> {
            registerForm.setVisibility(View.GONE);
            loginForm.setVisibility(View.VISIBLE);
        });

        // 9) (Opcional) Lógica de registro si la implementas aquí
        btnRegister.setOnClickListener(v -> {
            // TODO: implementar llamada a tu endpoint de registro
            Toast.makeText(this, "Implementa el registro aquí", Toast.LENGTH_SHORT).show();
        });
    }
}
