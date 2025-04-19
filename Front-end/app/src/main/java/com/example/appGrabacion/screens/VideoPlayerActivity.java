package com.example.appGrabacion.screens;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appGrabacion.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

public class VideoPlayerActivity extends AppCompatActivity {

    private SimpleExoPlayer player;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        String url = getIntent().getStringExtra("videoUrl");
        if (url == null || url.isEmpty()) {
            finish();
            return;
        }

        PlayerView playerView = findViewById(R.id.playerView);

        DefaultHttpDataSource.Factory httpFactory = new DefaultHttpDataSource.Factory();
        DefaultDataSource.Factory dataSourceFactory =
                new DefaultDataSource.Factory(this, httpFactory);

        player = new SimpleExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .build();

        playerView.setPlayer(player);
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
