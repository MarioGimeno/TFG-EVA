package com.example.appGrabacion.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.appGrabacion.MainActivity;
import com.example.appGrabacion.R;
import com.example.appGrabacion.contracts.LoginContract;
import com.example.appGrabacion.contracts.RegisterContract;
import com.example.appGrabacion.presenters.LoginPresenter;
import com.example.appGrabacion.presenters.RegisterPresenter;
import com.example.appGrabacion.services.RegisterModel;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity
        implements LoginContract.View, RegisterContract.View {

    private static final String PREFS      = "app_prefs";
    private static final String KEY_USER   = "logged_in_user";

    private SharedPreferences prefs;

    // Formularios
    private LinearLayout loginForm, registerForm;
    private TextView     tvGoRegister, txtLogin;
    private EditText     etEmail, etPassword;
    private EditText     rspFullName, rspEmailRegister, rspPasswordRegister;
    private MaterialButton btnLogin, btnRegister;

    // Bienvenida
    private CardView     cardWelcome;
    private TextView     tvWelcome;
    private MaterialButton btnLogout;

    private LoginPresenter    loginPresenter;
    private RegisterPresenter registerPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        // Vídeo de fondo
        VideoView videoBg = findViewById(R.id.videoBackground);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.evalogin2);
        videoBg.setVideoURI(videoUri);
        videoBg.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f);
        });
        videoBg.start();

        // Botón Atrás
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

        // Bind formularios
        loginForm        = findViewById(R.id.login_form);
        registerForm     = findViewById(R.id.register_form);
        tvGoRegister     = findViewById(R.id.tvGoRegister);
        txtLogin         = findViewById(R.id.txtLogin);
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        rspFullName      = findViewById(R.id.rspFullName);
        rspEmailRegister = findViewById(R.id.rspEmailRegister);
        rspPasswordRegister = findViewById(R.id.rspPasswordRegister);
        btnLogin         = findViewById(R.id.btnLogin);
        btnRegister      = findViewById(R.id.btnRegister);

        // Bind bienvenida
        cardWelcome = findViewById(R.id.card_welcome);
        tvWelcome   = findViewById(R.id.tvWelcome);
        btnLogout   = findViewById(R.id.btnLogout);

        // Presenters
        loginPresenter    = new LoginPresenter(this);
        loginPresenter.attachView(this);
        registerPresenter = new RegisterPresenter(new RegisterModel(this));
        registerPresenter.attachView(this);

        // Login
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            loginPresenter.performLogin(email, pass);
        });

        // Cambiar a registro/login
        tvGoRegister.setOnClickListener(v -> {
            loginForm.setVisibility(LinearLayout.GONE);
            registerForm.setVisibility(LinearLayout.VISIBLE);
        });
        txtLogin.setOnClickListener(v -> {
            registerForm.setVisibility(LinearLayout.GONE);
            loginForm.setVisibility(LinearLayout.VISIBLE);
        });

        // Registro
        btnRegister.setOnClickListener(v -> {
            String name  = rspFullName.getText().toString().trim();
            String email = rspEmailRegister.getText().toString().trim();
            String pass  = rspPasswordRegister.getText().toString().trim();
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            registerPresenter.register(name, email, pass);
        });

        // Cerrar sesión
        btnLogout.setOnClickListener(v -> {
            prefs.edit().remove(KEY_USER).apply();
            showForms();
        });

        // Al arrancar, si hay usuario guardado, mostrar bienvenida
        String savedUser = prefs.getString(KEY_USER, null);
        if (savedUser != null) {
            showWelcome(savedUser);
        } else {
            showForms();
        }
    }

    /** Muestra la tarjeta de bienvenida */
    private void showWelcome(String userName) {
        loginForm.setVisibility(LinearLayout.GONE);
        registerForm.setVisibility(LinearLayout.GONE);
        cardWelcome.setVisibility(CardView.VISIBLE);
        tvWelcome.setText("¡Bienvenida, " + userName + "!");
    }

    /** Muestra los formularios de login/registro */
    private void showForms() {
        loginForm.setVisibility(LinearLayout.VISIBLE);
        registerForm.setVisibility(LinearLayout.GONE);
        cardWelcome.setVisibility(CardView.GONE);
    }

    // --- LoginContract.View ---
    @Override public void showLoading() { }
    @Override public void hideLoading() { }
    @Override public void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    @Override public void navigateToMain() {
        // Si prefieres ir al MainActivity, descomenta:
        // startActivity(new Intent(this, MainActivity.class));
    }
    @Override public void showLoginSuccess(String userName) {
        // Guarda el nombre y muestra bienvenida
        prefs.edit().putString(KEY_USER, userName).apply();
        showWelcome(userName);

    }

    // --- RegisterContract.View ---
    @Override
    public void showSuccess() {
        // Tras un registro exitoso, reutiliza showLoginSuccess
        String name = rspFullName.getText().toString().trim();
        showLoginSuccess(name);
    }

    public void showErrorRegister(String msg) {
        showError(msg);
    }

    @Override
    protected void onDestroy() {
        loginPresenter.detachView();
        registerPresenter.detachView();
        super.onDestroy();
    }
}
