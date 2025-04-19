package com.example.appGrabacion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appGrabacion.screens.FolderActivity;
import com.example.appGrabacion.screens.LoginActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Se establece el layout definido en res/layout/activity_main.xml
        setContentView(R.layout.activity_main);
        // Obtenemos referencia al botón
        Button btnGoLogin = findViewById(R.id.btnGoLogin);
        btnGoLogin.setOnClickListener(v -> {
            // Lanzamos LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        // Botón a Carpeta Personal
        Button btnGoFolder = findViewById(R.id.btnGoFolder);
        btnGoFolder.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FolderActivity.class));
        });
    }
}
