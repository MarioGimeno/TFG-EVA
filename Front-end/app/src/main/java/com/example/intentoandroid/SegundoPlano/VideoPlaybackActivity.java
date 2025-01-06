package com.example.intentoandroid.SegundoPlano;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.intentoandroid.R;
import java.io.File;

public class VideoPlaybackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_playback);

        VideoView videoView = findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        // Si recibiste la ruta por intent:
        // String path = getIntent().getStringExtra("VIDEO_PATH");
        // File videoFile = new File(path);

        // O la misma ruta que en MainActivity
        File videoFile = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "recorded_video.mp4");

        if (videoFile.exists()) {
            videoView.setVideoPath(videoFile.getAbsolutePath());
            videoView.setMediaController(mediaController);
            videoView.setOnPreparedListener(mp -> videoView.start());
        } else {
            Log.e("VideoError", "Archivo no encontrado: " + videoFile.getAbsolutePath());
        }


}
}
