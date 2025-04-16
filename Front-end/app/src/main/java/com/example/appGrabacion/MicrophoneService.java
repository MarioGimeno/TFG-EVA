package com.example.appGrabacion;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.appGrabacion.R;

import java.io.File;
import java.io.IOException;

public class MicrophoneService extends Service {

    private static final String TAG = "MicrophoneService";
    public static final String CHANNEL_ID = "MicrophoneChannel";

    private MediaRecorder mediaRecorder;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        startForegroundNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        if (mediaRecorder == null) {
            setupMediaRecorder();
            startRecording();
        }
        return START_STICKY; // Mantén el servicio vivo incluso si el sistema lo mata
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        stopRecording();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Microphone Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Recording Audio")
                .setContentText("The microphone is active")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();

        startForeground(1, notification); // Notificación persistente para evitar que el sistema mate el servicio
    }

    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // Asegúrate de usar el micrófono
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        File outputFile = new File(getExternalFilesDir(null), "audio_record.mp4");
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mediaRecorder.prepare();
            Log.d(TAG, "MediaRecorder prepared");
        } catch (IOException e) {
            Log.e(TAG, "Error preparing MediaRecorder", e);
        }
    }

    private void startRecording() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.start();
                Log.d(TAG, "Recording started");
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error starting MediaRecorder", e);
        }
    }

    private void stopRecording() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                Log.d(TAG, "Recording stopped");
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error stopping MediaRecorder", e);
        }
    }
}
