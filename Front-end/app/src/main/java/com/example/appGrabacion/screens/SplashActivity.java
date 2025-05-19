package com.example.appGrabacion.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appGrabacion.MainActivity;
import com.example.appGrabacion.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d("SplashActivity","¡Estoy aquí!");
        //Toast.makeText(this,"Splash arrancada",Toast.LENGTH_SHORT).show();
        setContentView(R.layout.splash_screen);
        new android.os.Handler(getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 1000);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Si se abre desde el launcher, relanza Splash
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            Intent splashIntent = new Intent(this, SplashActivity.class);
            splashIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(splashIntent);
            finish();
        }
    }


}
