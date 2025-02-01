package com.example.intentoandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.intentoandroid.ApiService;
import com.example.intentoandroid.R;
import com.example.intentoandroid.RetrofitClient;
import com.example.intentoandroid.SegundoPlano.MicrophoneService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final String TAG = "MainActivity";

    private CameraDevice cameraDevice;
    private TextureView textureView;
    private CameraManager cameraManager;
    private String cameraId;
    private MediaRecorder mediaRecorder;
    private CameraCaptureSession captureSession;

    private Button startRecordingButton;
    private Button stopRecordingButton;

    private boolean isRecording = false;

    private FusedLocationProviderClient fusedLocationClient;
    private String startLocation = "";
    private String endLocation = "";

    @Override
    protected void onResume(){
        super.onResume();
        textureView = findViewById(R.id.textureView);

        // Inicializa el FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
    }

    @Override
    protected void onStart(){
        super.onStart();
        textureView = findViewById(R.id.textureView);
        // Inicializa el FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(textureView == null){
            textureView = findViewById(R.id.textureView);
        }
        startRecordingButton = findViewById(R.id.StartService);
        stopRecordingButton = findViewById(R.id.StopService);

        // Inicializa el FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        // Inicia grabación la primera vez si no está grabando
        if (!isRecording) {
            // Obtener ubicación al inicio
            getLocation(true, new LocationCallback() {
                @Override
                public void onLocationReceived() {
                    startMicrophoneService();
                    startVideoRecording();
                }
            });

        }

        startRecordingButton.setOnClickListener(v -> {
            if (!isRecording) {
                getLocation(true, new LocationCallback() {
                    @Override
                    public void onLocationReceived() {
                        startMicrophoneService();
                        startVideoRecording();
                    }
                });
            }
        });

        stopRecordingButton.setOnClickListener(v -> {
            if (isRecording) {
                stopVideoRecording();
                getLocation(false, new LocationCallback() {
                    @Override
                    public void onLocationReceived() {
                        //combineAudioAndVideo();
                        combineAudioAndLocation();
                    }
                });
            }
        });
    }

    private void getLocation(boolean isStart, LocationCallback callback) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        String locationData = "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude();
                        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                        if (isStart) {
                            startLocation = "Inicio (" + timestamp + "): " + locationData;
                            Log.d(TAG, "Inicio: " + startLocation);
                        } else {
                            endLocation = "Fin (" + timestamp + "): " + locationData;
                            Log.d(TAG, "Fin: " + endLocation);
                        }

                        if (callback != null) {
                            callback.onLocationReceived();
                        }
                    }else {
                        Log.e(TAG, "La ubicación es null");
                        // Asegúrate de llamar al callback incluso si la ubicación es null
                        if (callback != null) {
                            callback.onLocationReceived();
                        }
                    }
                });

    }

    private void startMicrophoneService() {
        Intent intent = new Intent(this, MicrophoneService.class);
        startService(intent);
    }

    private void stopMicrophoneService() {
        Intent intent = new Intent(this, MicrophoneService.class);
        stopService(intent);
    }

    private void startVideoRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        // *** Aquí forzamos instanciar un SurfaceTexture si es null ***
        if (textureView.getSurfaceTexture() == null) {
            Log.d(TAG, "surfaceTexture was null, instanciamos uno nuevo manualmente");
            textureView.setSurfaceTexture(new SurfaceTexture(0));
        }

        try {
            setupMediaRecorder();
            cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    createCaptureSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                }
            }, null);
        } catch (CameraAccessException | IOException e) {
            Log.e(TAG, "Error starting video recording", e);
        }
    }

    private void setupMediaRecorder() throws IOException {
        mediaRecorder = new MediaRecorder();

        // Configurar la fuente de audio (por ejemplo, MIC)
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // Configurar la fuente de video
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        // Establecer el formato de salida
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        // Archivo de salida
        File outputFile = new File(getExternalFilesDir(null), "recorded_video.mp4");
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());

        // Configurar parámetros de audio
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(96000); // Tasa de bits de audio
        mediaRecorder.setAudioSamplingRate(44100);    // Frecuencia de muestreo en Hz

        // Configurar parámetros de video
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(1920, 1080);

        // Girar el video 90 grados
        mediaRecorder.setOrientationHint(90);

        // Preparar el MediaRecorder
        mediaRecorder.prepare();
    }



    private void createCaptureSession() {
        try {
            Surface textureSurface = new Surface(textureView.getSurfaceTexture());
            Surface recorderSurface = mediaRecorder.getSurface();

            cameraDevice.createCaptureSession(
                    Arrays.asList(textureSurface, recorderSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            captureSession = session;
                            startPreviewAndRecording(session);
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(MainActivity.this, "Failed to configure camera", Toast.LENGTH_SHORT).show();
                        }
                    },
                    null
            );
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error creating capture session", e);
        }
    }

    private void startPreviewAndRecording(CameraCaptureSession session) {
        try {
            CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            captureRequestBuilder.addTarget(new Surface(textureView.getSurfaceTexture()));
            captureRequestBuilder.addTarget(mediaRecorder.getSurface());

            session.setRepeatingRequest(captureRequestBuilder.build(), null, null);
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Video recording started");
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error starting preview and recording", e);
        }
    }

    private void stopVideoRecording() {
        try {
            mediaRecorder.stop();
        } catch (RuntimeException e) {
            Log.e(TAG, "Error al detener el MediaRecorder, puede que no estuviera grabando", e);
        }
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        // Obtener la ubicación final
        isRecording = false;
        Log.d(TAG, "Video recording stopped");
    }
    private void combineAudioAndLocation() {
        // Encriptar el video
        File videoFile = new File(getExternalFilesDir(null), "recorded_video.mp4");
        File encryptedVideoFile = new File(getExternalFilesDir(null), "encrypted_video.mp4");
        try {
            CryptoUtils.encryptFile(videoFile, encryptedVideoFile);
        } catch (Exception e) {
            Log.e(TAG, "Error al encriptar el video", e);
            return;
        }
        RequestBody videoRequestBody = RequestBody.create(MediaType.parse("video/mp4"), encryptedVideoFile);
        MultipartBody.Part videoPart = MultipartBody.Part.createFormData("video", encryptedVideoFile.getName(), videoRequestBody);

        // Encriptar la localización
        String locationData = startLocation + "\n" + endLocation;
        File locationFile = new File(getExternalFilesDir(null), "location.txt");
        try (FileOutputStream fos = new FileOutputStream(locationFile)) {
            fos.write(locationData.getBytes("UTF-8"));
        } catch (IOException e) {
            Log.e(TAG, "Error al escribir el archivo de ubicación", e);
            return;
        }
        File encryptedLocationFile = new File(getExternalFilesDir(null), "encrypted_location.txt");
        try {
            CryptoUtils.encryptFile(locationFile, encryptedLocationFile);
        } catch (Exception e) {
            Log.e(TAG, "Error al encriptar la ubicación", e);
            return;
        }
        RequestBody locationRequestBody = RequestBody.create(MediaType.parse("text/plain"), encryptedLocationFile);
        MultipartBody.Part locationPart = MultipartBody.Part.createFormData("location", encryptedLocationFile.getName(), locationRequestBody);

        // Llamada a la API de Retrofit
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<ResponseBody> call = apiService.uploadVideoAndLocation(videoPart, locationPart);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Archivos enviados correctamente!");
                    deleteFile(videoFile);
                } else {
                    Log.e(TAG, "Error al enviar los archivos. Código de respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error de red: " + t.getMessage());
            }
        });
    }


    // Método para eliminar los archivos
    private void deleteFile(File videoFile) {
        if (videoFile.exists() && videoFile.delete()) {
            Log.d(TAG, "Video file deleted successfully");
        }
    }




    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopVideoRecording();
        stopMicrophoneService();
        // Obtener ubicación al final
        getLocation(false, new LocationCallback() {
            @Override
            public void onLocationReceived() {
                combineAudioAndLocation();
            }
        });
    }

    // Implementación de TextureView.SurfaceTextureListener
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "SurfaceTexture disponible");
        // Obtener ubicación al inicio
        getLocation(true, new LocationCallback() {
            @Override
            public void onLocationReceived() {
                startMicrophoneService();
                startVideoRecording();
            }
        });

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "SurfaceTexture size changed");
        // Maneja cambios en el tamaño si es necesario
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "SurfaceTexture destroyed");
        // Libera recursos si es necesario
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Opcional: manejar actualizaciones
    }


    public interface LocationCallback {
        void onLocationReceived();
    }
}
