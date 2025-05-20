package com.example.appGrabacion.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appGrabacion.MainActivity;
import com.example.appGrabacion.R;
import com.example.appGrabacion.contracts.LoginContract;
import com.example.appGrabacion.presenters.LoginPresenter;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    private LoginPresenter presenter;
    private LinearLayout loginForm, registerForm;
    private TextView tvGoRegister, txtLogin;
    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginForm      = findViewById(R.id.login_form);
        registerForm   = findViewById(R.id.register_form);
        tvGoRegister   = findViewById(R.id.tvGoRegister);
        txtLogin       = findViewById(R.id.txtLogin);

        etEmail        = findViewById(R.id.etEmail);
        etPassword     = findViewById(R.id.etPassword);
        MaterialButton btnLogin    = findViewById(R.id.btnLogin);
        MaterialButton btnRegister = findViewById(R.id.btnRegister);

        loginForm.setVisibility(View.VISIBLE);
        registerForm.setVisibility(View.GONE);

        // Video background
        VideoView videoBg = findViewById(R.id.videoBackground);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.evalogin2);
        videoBg.setVideoURI(videoUri);
        videoBg.setMediaController(null);
        videoBg.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f);
        });
        videoBg.start();

        presenter = new LoginPresenter(this);
        presenter.attachView(this);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            presenter.performLogin(email, pass);
        });

        tvGoRegister.setOnClickListener(v -> {
            loginForm.setVisibility(View.GONE);
            registerForm.setVisibility(View.VISIBLE);
        });

        txtLogin.setOnClickListener(v -> {
            registerForm.setVisibility(View.GONE);
            loginForm.setVisibility(View.VISIBLE);
        });

        btnRegister.setOnClickListener(v -> {
            Toast.makeText(this, "Implementa el registro aquí", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void showLoading() {
        // Mostrar ProgressBar o animación
    }

    @Override
    public void hideLoading() {
        // Ocultar ProgressBar o animación
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }
}
