package com.example.appGrabacion.screens;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.TextPaint;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.appGrabacion.MainActivity;
import com.example.appGrabacion.R;
import com.example.appGrabacion.contracts.LoginContract;
import com.example.appGrabacion.contracts.RegisterContract;
import com.example.appGrabacion.presenters.LoginPresenter;
import com.example.appGrabacion.presenters.RegisterPresenter;
import com.example.appGrabacion.models.RegisterModel;
import com.example.appGrabacion.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity
        implements LoginContract.View, RegisterContract.View {

    private static final String PREFS      = "app_prefs";
    public static final String KEY_USER = "key_user";

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
    private CheckBox         cbPrivacy;


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
        cbPrivacy           = findViewById(R.id.cbPrivacy);

        // Alinea el checkbox arriba y aplica el texto con span de color
        String full = getString(R.string.accept_privacy_plain);
        SpannableString ss = new SpannableString(full);
        String target = "política de privacidad";
        int start = full.indexOf(target);
        int end   = start + target.length();
        int purple = ContextCompat.getColor(this, R.color.checkbox_tint);

// 1) color
        ss.setSpan(new ForegroundColorSpan(purple),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

// 2) clickable
        ss.setSpan(new ClickableSpan() {
            @Override public void onClick(@NonNull View widget) {
                String fileId = "1ZY5vzdmxYGG6j57DhoFki-o1V7oNDCj7";
                String url = "https://drive.google.com/uc?export=download&id=" + fileId;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
            @Override public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(purple);
                ds.setUnderlineText(false);
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

// 3) aplica al CheckBox
        cbPrivacy.setText(ss);
        cbPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
        cbPrivacy.setHighlightColor(Color.TRANSPARENT);

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
            if (!cbPrivacy.isChecked()) {
                Toast.makeText(this,
                        "Debes aceptar la política de privacidad",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            registerPresenter.register(name, email, pass);
        });

            // Cerrar sesión
            btnLogout.setOnClickListener(v -> {
                SessionManager session = new SessionManager(this);
                session.clear();  // Borra todo el SharedPreferences 'prefs_session'
                Log.d("SessionCheck", "Token tras logout: " + session.getToken(this)); // Debe ser null o vacío
                showForms();
            });

// Al arrancar, si hay usuario guardado, mostrar bienvenida
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String savedUser = prefs.getString(KEY_USER, null);
        Log.d("SessionCheck", "Leído savedUser al cargar login: '" + savedUser + "'");

        if (savedUser != null && !savedUser.trim().isEmpty()) {
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
    @Override public void showLoginSuccess(String fullName) {
        // Guarda el nombre y muestra bienvenida
        showWelcome(fullName);

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
