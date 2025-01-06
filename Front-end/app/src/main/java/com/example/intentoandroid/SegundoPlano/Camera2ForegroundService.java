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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Camera2ForegroundService extends Service {

    private static final String TAG = "Cam2ForegroundService";
    private static final String CHANNEL_ID = "Camera2ServiceChannel";
    private static final int NOTIF_ID = 123;

    // Camera2
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;

    // MediaRecorder
    private MediaRecorder mediaRecorder;

    // Para saber si estamos grabando
    private boolean isRecording = false;

    // Ruta del archivo final
    private String filePath;

    // Surface “falsa” para la preview
    private SurfaceTexture fakeSurfaceTexture;
    private Surface fakePreviewSurface;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        // Crea el canal de notificaciones y convierte el servicio en Foreground
        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification());

        // Inicializa el CameraManager
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
    }

    /**
     * Creamos un canal de notificación (Android 8.0+).
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "Camera2 Background Recording",
                    NotificationManager.IMPORTANCE_LOW
            );
            ch.setDescription("Canal para grabación en segundo plano con Camera2");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(ch);
            }
        }
    }

    /**
     * Construye la notificación de Foreground.
     */
    private android.app.Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Grabando Video (Camera2)")
                .setContentText("La app está usando la cámara en segundo plano")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .build();
    }

    /**
     * Se llama cada vez que hacemos startService(...).
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        // Verificamos permisos CAMERA y RECORD_AUDIO.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "Faltan permisos de cámara o audio");
            stopSelf();
            return START_NOT_STICKY;
        }

        // Abrimos la cámara en background
        openCamera();

        // Queremos que el servicio se reinicie si el sistema lo mata.
        return START_STICKY;
    }

    /**
     * Abre la cámara trasera con Camera2.
     */
    private void openCamera() {
        try {
            String cameraId = findBackCameraId(cameraManager);
            if (cameraId == null) {
                Log.e(TAG, "No hay cámara trasera");
                stopSelf();
                return;
            }

            Log.d(TAG, "Abriendo cámara con ID=" + cameraId);
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
                    Log.d(TAG, "cameraDevice onOpened");
                    cameraDevice = device;
                    startRecordingSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice device) {
                    Log.e(TAG, "cameraDevice onDisconnected");
                    device.close();
                    cameraDevice = null;
                    stopSelf();
                }

                @Override
                public void onError(@NonNull CameraDevice device, int error) {
                    Log.e(TAG, "cameraDevice onError: " + error);
                    device.close();
                    cameraDevice = null;
                    stopSelf();
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    /**
     * Busca la ID de la cámara trasera.
     */
    private String findBackCameraId(CameraManager manager) throws CameraAccessException {
        for (String camId : manager.getCameraIdList()) {
            CameraCharacteristics chars = manager.getCameraCharacteristics(camId);
            Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                return camId;
            }
        }
        return null;
    }

    /**
     * Configura MediaRecorder y crea la CaptureSession con 2 surfaces:
     * 1) mediaRecorder.getSurface()
     * 2) fakePreviewSurface
     */
    private void startRecordingSession() {
        try {
            setupMediaRecorder();

            // El surface para grabar
            Surface recorderSurface = mediaRecorder.getSurface();

            // Surface “falso” de preview
            fakeSurfaceTexture = new SurfaceTexture(11); // ID cualquiera
            fakeSurfaceTexture.setDefaultBufferSize(1280, 720);
            fakePreviewSurface = new Surface(fakeSurfaceTexture);

            // Necesitamos al menos 2 surfaces para la captureSession
            List<Surface> surfaces = new ArrayList<>();
            surfaces.add(recorderSurface);
            surfaces.add(fakePreviewSurface);

            // Creamos la sesión
            cameraDevice.createCaptureSession(
                    surfaces,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) return;

                            captureSession = session;
                            // Iniciamos la grabación
                            startMediaRecorder();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG, "createCaptureSession onConfigureFailed");
                            session.close();
                            captureSession = null;
                            stopSelf();
                        }
                    },
                    null
            );

        } catch (IOException | CameraAccessException e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    private void setupMediaRecorder() throws IOException {
        Log.d(TAG, "setupMediaRecorder");

        // Ruta de salida
        File dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        File videoFile = new File(dir, "c2_bg_record_" + new Date().getTime() + ".mp4");
        filePath = videoFile.getAbsolutePath();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);  // Camera2 usa SURFACE
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        mediaRecorder.setVideoSize(1280, 720);
        mediaRecorder.setVideoFrameRate(30);

        mediaRecorder.setOutputFile(filePath);
        mediaRecorder.prepare();
    }

    private void startMediaRecorder() {
        if (mediaRecorder != null) {
            Log.d(TAG, "Iniciando grabación con MediaRecorder -> " + filePath);
            mediaRecorder.start();
            isRecording = true;
        }
    }

    /**
     * Detiene la grabación y cierra la cámara.
     */
    private void stopRecording() {
        Log.d(TAG, "stopRecording() isRecording=" + isRecording);
        if (isRecording) {
            // 1) Deja de enviar frames
            if (captureSession != null) {
                try {
                    captureSession.stopRepeating();
                    captureSession.abortCaptures();
                    Log.d(TAG, "captureSession => stopRepeating + abortCaptures OK");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 2) Espera un poco (p. ej. 500 ms) para “drenar” buffers.
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 3) Llamar a mediaRecorder.stop()
            try {
                Log.d(TAG, "Llamando a mediaRecorder.stop()");
                mediaRecorder.stop();
                Log.d(TAG, "mediaRecorder.stop() OK");
            } catch (RuntimeException e) {
                Log.e(TAG, "Error al detener mediaRecorder", e);
            }

            // 4) Liberar el MediaRecorder
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;

            // Enviamos broadcast (opcional)
            Intent intent = new Intent("com.example.intentoandroid.ACTION_RECORDING_FINISHED");
            intent.putExtra("VIDEO_PATH", filePath);
            sendBroadcast(intent);

            Log.d(TAG, "Grabación detenida. Archivo: " + filePath);
            isRecording = false;
        }

        // 5) Cerrar la sesión y la cámara
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    /**
     * Llamado cuando paras el servicio (stopService).
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() => Deteniendo grabación si está activa");
        stopRecording();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
