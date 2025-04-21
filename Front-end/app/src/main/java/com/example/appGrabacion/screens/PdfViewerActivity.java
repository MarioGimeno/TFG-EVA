package com.example.appGrabacion.screens;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appGrabacion.R;

public class PdfViewerActivity extends AppCompatActivity {
    public static final String EXTRA_URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        String url = getIntent().getStringExtra(EXTRA_URL);
        WebView web = findViewById(R.id.webView);

        // Cargamos a través de Google Docs Viewer
        web.setWebViewClient(new WebViewClient());
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);

        web.loadUrl("https://docs.google.com/gview?embedded=true&url=" + url);
    }
}
