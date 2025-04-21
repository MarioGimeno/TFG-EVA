package com.example.appGrabacion.screens;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appGrabacion.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TextViewerActivity extends AppCompatActivity {

    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_viewer);
        tvContent = findViewById(R.id.tvContent);

        String url = getIntent().getStringExtra("url");
        if (url == null || !url.toLowerCase().contains(".txt")) {
            Toast.makeText(this, "URL de texto inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Carga en background
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                if (conn.getResponseCode() != 200) {
                    runOnUiThread(() ->
                    {
                        try {
                            Toast.makeText(this, "Error cargando texto: " + conn.getResponseCode(),
                                    Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    return;
                }
                InputStream is = conn.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                r.close();
                String text = sb.toString();
                runOnUiThread(() -> tvContent.setText(text));
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
