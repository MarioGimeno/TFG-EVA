package com.example.appGrabacion.screens;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appGrabacion.R;

public class ImageViewerActivity extends AppCompatActivity {
    public static final String EXTRA_URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        String url = getIntent().getStringExtra(EXTRA_URL);
        ImageView iv = findViewById(R.id.imageView);

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(iv);
    }
}
