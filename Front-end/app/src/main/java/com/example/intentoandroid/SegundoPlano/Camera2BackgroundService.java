package com.example.intentoandroid.SegundoPlano;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Camera2BackgroundService extends Service {

    private static final String TAG = "Camera2BackgroundService";
    private static final String CHANNEL_ID = "CameraServiceChannel";
    private static final int NOTIF_ID = 101;

    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;

    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private String videoFilePath;

    private SurfaceTexture dummySurfaceTexture;
    private Surface dummySurface;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification());
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        if (!checkPermissions()) {
            Log.e(TAG, "Faltan permisos de cámara o audio");
            stopSelf();
            return START_NOT_STICKY;
        }

        openCamera();
        return START_STICKY;
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void openCamera() {
        try {
            String cameraId = getBackCameraId();
            if (cameraId == null) {
                Log.e(TAG, "No se encontró una cámara trasera");
                stopSelf();
                return;
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice device) {
                    Log.d(TAG, "Cámara abierta");
                    cameraDevice = device;
                    startRecordingSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice device) {
                    Log.e(TAG, "Cámara desconectada");
                    device.close();
                    cameraDevice = null;
                    stopSelf();
                }

                @Override
                public void onError(@NonNull CameraDevice device, int error) {
                    Log.e(TAG, "Error al abrir la cámara: " + error);
                    device.close();
                    cameraDevice = null;
                    stopSelf();
                }
            }, null);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Error al acceder a la cámara", e);
            stopSelf();
        }
    }

    private String getBackCameraId() throws CameraAccessException {
        for (String id : cameraManager.getCameraIdList()) {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
            if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                return id;
            }
        }
        return null;
    }

    private void startRecordingSession() {
        try {
            setupMediaRecorder();

            Surface recorderSurface = mediaRecorder.getSurface();
            dummySurfaceTexture = new SurfaceTexture(0);
            dummySurfaceTexture.setDefaultBufferSize(1280, 720);
            dummySurface = new Surface(dummySurfaceTexture);

            List<Surface> surfaces = new ArrayList<>();
            surfaces.add(recorderSurface);
            surfaces.add(dummySurface);

            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) return;

                    captureSession = session;
                    startRecording();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "Falló la configuración de la sesión de captura");
                    stopSelf();
                }
            }, null);

        } catch (IOException | CameraAccessException e) {
            Log.e(TAG, "Error al iniciar la sesión de grabación", e);
            stopSelf();
        }
    }

    private void setupMediaRecorder() throws IOException {
        File videoDir = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "Camera2");
        if (!videoDir.exists()) videoDir.mkdirs();

        videoFilePath = videoDir.getAbsolutePath() + "/video_" + new Date().getTime() + ".mp4";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoSize(1280, 720);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setOutputFile(videoFilePath);
        mediaRecorder.prepare();
    }

    private void startRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Grabación iniciada: " + videoFilePath);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
    }

    private void stopRecording() {
        if (isRecording) {
            try {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
            } catch (Exception e) {
                Log.e(TAG, "Error al detener la grabación", e);
            }
        }
        if (cameraDevice != null) {
            cameraDevice.close();
        }
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
                    "Background Camera Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private android.app.Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Grabación en curso")
                .setContentText("Grabando video y audio en segundo plano")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build();
    }
}
