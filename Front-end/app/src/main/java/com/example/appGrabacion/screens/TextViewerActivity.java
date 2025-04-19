package com.example.appGrabacion.screens;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appGrabacion.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TextViewerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_viewer);

        String txtUrl = getIntent().getStringExtra("txtUrl");
        if (txtUrl == null || txtUrl.isEmpty()) {
            Toast.makeText(this, "URL de texto inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tv = findViewById(R.id.tvContent);
        new LoadTextTask(tv).execute(txtUrl);
    }

    private static class LoadTextTask extends AsyncTask<String,Void,String> {
        private final TextView tv;
        LoadTextTask(TextView tv) { this.tv = tv; }
        @Override protected String doInBackground(String... urls) {
            try {
                URL u = new URL(urls[0]);
                HttpURLConnection c = (HttpURLConnection)u.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line).append('\n');
                r.close();
                return sb.toString();
            } catch (Exception e) {
                return null;
            }
        }
        @Override protected void onPostExecute(String s) {
            if (s == null) {
                tv.setText("Error cargando texto");
            } else {
                tv.setText(s);
            }
        }
    }
}
