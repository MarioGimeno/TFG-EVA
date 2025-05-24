package com.example.appGrabacion.widget;

import static com.example.appGrabacion.MicrophoneService.CHANNEL_ID;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.appGrabacion.BackgroundRecordingManager;
import com.example.appGrabacion.R;

import java.io.IOException;
import java.util.Arrays;

public class RecordingService extends Service {

    private static final String TAG = "RecordingService";
    private BackgroundRecordingManager recordingManager;
    private SurfaceTexture dummySurfaceTexture;

    @Override
    public void onCreate() {
        super.onCreate();

        // Crea un dummy SurfaceTexture para la grabación
        dummySurfaceTexture = new SurfaceTexture(10);
        dummySurfaceTexture.setDefaultBufferSize(1920, 1080);

        // Crea una versión anónima de BackgroundRecordingManager para sobrescribir los métodos que dependen del TextureView
        recordingManager = new BackgroundRecordingManager(this, null) {
            @Override
            protected void startVideoRecording() {
                try {
                    // Verifica que se tenga el permiso de cámara antes de continuar
                    if (ContextCompat.checkSelfPermission(RecordingService.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "Permiso de cámara no concedido");
                        stopSelf();
                        return;
                    }

                    setupMediaRecorder();
                    String cameraId = cameraManager.getCameraIdList()[0];

                    cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(CameraDevice camera) {
                            cameraDevice = camera;
                            try {
                                // Usamos el dummy SurfaceTexture para crear un Surface
                                dummySurfaceTexture.setDefaultBufferSize(1920, 1080);
                                Surface dummySurface = new Surface(dummySurfaceTexture);
                                Surface recorderSurface = mediaRecorder.getSurface();
                                cameraDevice.createCaptureSession(
                                        Arrays.asList(dummySurface, recorderSurface),
                                        new CameraCaptureSession.StateCallback() {
                                            @Override
                                            public void onConfigured(CameraCaptureSession session) {
                                                captureSession = session;
                                                // Llamamos a nuestro método sobrescrito para iniciar la previsualización y grabación
                                                startPreviewAndRecording(session);
                                            }

                                            @Override
                                            public void onConfigureFailed(CameraCaptureSession session) {
                                                Log.e(TAG, "No se pudo configurar la sesión de captura en el servicio");
                                            }
                                        },
                                        new Handler(Looper.getMainLooper())
                                );
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "Error al crear la sesión de captura en el servicio", e);
                            }
                        }

                        @Override
                        public void onDisconnected(CameraDevice camera) {
                            camera.close();
                        }

                        @Override
                        public void onError(CameraDevice camera, int error) {
                            Log.e(TAG, "Error en la cámara en el servicio, código: " + error);
                            camera.close();
                        }
                    }, new Handler(Looper.getMainLooper()));
                } catch (IOException | CameraAccessException e) {
                    Log.e(TAG, "Error en startVideoRecording del servicio", e);
                }
            }

            // Sobrescribimos este método para no depender de textureView
            @Override
            protected void startPreviewAndRecording(CameraCaptureSession session) {
                try {
                    // Creamos un CaptureRequest utilizando el dummySurface y la superficie del MediaRecorder.
                    CaptureRequest.Builder captureRequestBuilder =
                            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

                    // Creamos el dummySurface a partir del dummySurfaceTexture
                    Surface dummySurface = new Surface(dummySurfaceTexture);
                    captureRequestBuilder.addTarget(dummySurface);
                    captureRequestBuilder.addTarget(mediaRecorder.getSurface());

                    session.setRepeatingRequest(captureRequestBuilder.build(), null,
                            new Handler(Looper.getMainLooper()));
                    mediaRecorder.start();
                    isRecording = true;
                    Log.d(TAG, "Grabación de video iniciada (en servicio)");
                } catch (CameraAccessException e) {
                    Log.e(TAG, "Error en startPreviewAndRecording override", e);
                }
            }
        };

        // Inicia la grabación sin esperar callback de UI
        recordingManager.startRecording(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Crea el canal de notificaciones si es necesario (para API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Grabación", NotificationManager.IMPORTANCE_MIN);
            channel.setSound(null, null);                // sin sonido
            channel.enableVibration(false);              // sin vibración
            channel.setShowBadge(false);                 // sin badge
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Crea una notificación mínima para iniciar el servicio en primer plano
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_trasparente) // Usa un ícono válido en tu proyecto
                .setContentTitle("")                    // sin texto
                .setContentText("")                     // sin texto
                .setSilent(true)                        // API 26+ silencia completamente
                .setOngoing(true)                       // persistente
                .setShowWhen(false)                     // no muestra hora
                .build();

        startForeground(1, notification);

        // Llama a startForeground con la notificación
        startForeground(1, notification);

        Log.d(TAG, "RecordingService iniciado en primer plano");

        // Continúa con la lógica de grabación...
        // (Por ejemplo, llamar a tu lógica en BackgroundRecordingManager)
        recordingManager.startRecording(null);

        // START_STICKY o el valor que más se adecúe a tu caso
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        if (recordingManager != null) {
            recordingManager.stopRecording(null);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
