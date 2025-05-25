package com.example.appGrabacion.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appGrabacion.R;

import java.util.Locale;

public class MapsActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        webView = findViewById(R.id.webview);

        // Configuración inicial del WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.clearCache(true);

        // Manejar la intención que arrancó esta Activity
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    /**
     * Extrae lat/lon del Intent y carga la URL en el WebView.
     */
    private void handleIntent(Intent intent) {
        if (intent == null) return;

        double lat = intent.getDoubleExtra("lat", Double.NaN);
        double lon = intent.getDoubleExtra("lon", Double.NaN);

        if (Double.isNaN(lat) || Double.isNaN(lon)) {
            Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
            return;
        }

        loadMapInWebView(lat, lon);
    }

    /**
     * Carga en el WebView la URL de Google Maps deshabilitando caché
     * y forzando un parámetro único con timestamp.
     */
    private void loadMapInWebView(double lat, double lon) {
        long ts = System.currentTimeMillis();
        String mapsLink = String.format(
                Locale.ENGLISH,
                "https://maps.google.com/?q=%f,%f&_=%d",
                lat, lon, ts
        );
        webView.loadUrl(mapsLink);
    }

    /**
     * (Opcional) Si prefieres abrir la app nativa de Google Maps:
     */
    private void openMapWithGeoIntent(double lat, double lon) {
        String uri = String.format(
                Locale.ENGLISH,
                "geo:%f,%f?q=%f,%f",
                lat, lon, lat, lon
        );
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Google Maps no está instalada", Toast.LENGTH_SHORT).show();
        }
    }
}
