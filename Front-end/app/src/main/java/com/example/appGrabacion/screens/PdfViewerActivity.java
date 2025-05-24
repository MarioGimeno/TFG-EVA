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

        WebView webView = findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        String pdfUrl = getIntent().getStringExtra(EXTRA_URL);
        if (pdfUrl != null && !pdfUrl.isEmpty()) {
            // Google Docs Viewer requiere URL codificada
            String finalUrl = "https://docs.google.com/gview?embedded=true&url=" + pdfUrl;
            webView.loadUrl(finalUrl);
        }
    }
}
