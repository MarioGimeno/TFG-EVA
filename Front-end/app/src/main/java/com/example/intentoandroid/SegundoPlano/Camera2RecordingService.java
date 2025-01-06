package com.example.intentoandroid.SegundoPlano;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class Camera2RecordingService extends Service {

    private static final String TAG = "Camera2RecordingService";
    private static final String CHANNEL_ID = "CameraServiceChannel";
    private static final int NOTIF_ID = 101;

    public static final String ACTION_START_RECORDING = "START_RECORDING";
    public static final String ACTION_STOP_RECORDING = "STOP_RECORDING";

    private boolean isRecording = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio creado");
        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification("Servicio preparado para grabar"));
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            Log.d(TAG, "onStartCommand recibido con acción: " + action);
            if (ACTION_START_RECORDING.equals(action)) {
                startRecording();
            } else if (ACTION_STOP_RECORDING.equals(action)) {
                stopRecording();
            }
        } else {
            Log.d(TAG, "Intent recibido es null");
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            stopRecording();
        }
        Log.d(TAG, "Servicio destruido");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Camera Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private android.app.Notification buildNotification(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Grabación en curso")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .build();
    }

    private void startRecording() {
        if (!isRecording) {
            isRecording = true;
            Log.d(TAG, "Iniciando grabación...");
            // Lógica real de inicialización de grabación va aquí
            startForeground(NOTIF_ID, buildNotification("Grabando..."));
        } else {
            Log.d(TAG, "La grabación ya está en curso");
        }
    }

    private void stopRecording() {
        if (isRecording) {
            isRecording = false;
            Log.d(TAG, "Deteniendo grabación...");
            // Aquí detiene la lógica de grabación y libera recursos.
            stopForeground(true);
        }
    }
}
