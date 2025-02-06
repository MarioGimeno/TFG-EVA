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


import com.example.intentoandroid.SegundoPlano.MicrophoneService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

        // Configurar la fuente de audio y video
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
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

        // Limitar la grabación a 20 minutos (20 * 60 * 1000 ms)
        mediaRecorder.setMaxDuration(15 * 60 * 1000);

        // Configurar el listener para el límite de duración
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    Log.d(TAG, "Se alcanzó el límite de 20 minutos, deteniendo la grabación.");
                    runOnUiThread(() -> {
                        stopVideoRecording();
                        stopMicrophoneService();
                        combineAudioAndLocation();
                    });
                }
            }
        });

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
        new Thread(() -> {
            try {
                // Ruta del archivo de video grabado
                File videoFile = new File(getExternalFilesDir(null), "recorded_video.mp4");

                if (!videoFile.exists()) {
                    Log.e(TAG, "❌ Error: Archivo de video no encontrado.");
                    return;
                }

                // Encriptar el video completo
                File encryptedVideoFile = new File(getExternalFilesDir(null), "encrypted_video.mp4");
                CryptoUtils.encryptFileFlexible(videoFile, encryptedVideoFile);

                // Preparar archivo de ubicación
                String locationData = startLocation + "\n" + endLocation;
                File locationFile = new File(getExternalFilesDir(null), "location.txt");
                try (FileOutputStream fos = new FileOutputStream(locationFile)) {
                    fos.write(locationData.getBytes("UTF-8"));
                }
                if (!locationFile.exists()) {
                    Log.e(TAG, "❌ Error: Archivo de ubicación no encontrado.");
                    return;
                }

                // Encriptar ubicación
                File encryptedLocationFile = new File(getExternalFilesDir(null), "encrypted_location.txt");
                CryptoUtils.encryptFileFlexible(locationFile, encryptedLocationFile);

                // Enviar la ubicación (encriptada) en un único request
                RequestBody locationRequestBody = RequestBody.create(MediaType.parse("text/plain"), encryptedLocationFile);
                MultipartBody.Part locationPart = MultipartBody.Part.createFormData("location", encryptedLocationFile.getName(), locationRequestBody);
                ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
                Call<ResponseBody> callLocation = apiService.uploadVideoAndLocation(locationPart);
                callLocation.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "✅ Archivo de ubicación enviado correctamente.");
                        } else {
                            Log.e(TAG, "❌ Error al enviar ubicación. Código: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "❌ Error de red al enviar ubicación: " + t.getMessage());
                    }
                });

                // Enviar el video en chunks (el video ya está encriptado)
                uploadVideoChunks(encryptedVideoFile);

                // (Opcional) Eliminar el archivo original si es necesario
                deleteFile(videoFile);

            } catch (Exception e) {
                Log.e(TAG, "❌ Error en combineAudioAndLocation", e);
            }
        }).start();
    }

    // Método para eliminar los archivos
    private void deleteFile(File videoFile) {
        if (videoFile.exists() && videoFile.delete()) {
            Log.d(TAG, "Video file deleted successfully");
        }
    }


    public List<byte[]> splitFileIntoChunks(File file, int chunkSize) throws IOException {
        List<byte[]> chunks = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                if (bytesRead < chunkSize) {
                    byte[] lastChunk = Arrays.copyOf(buffer, bytesRead);
                    chunks.add(lastChunk);
                } else {
                    chunks.add(buffer.clone());
                }
            }
        }
        return chunks;
    }
    public void uploadVideoChunks(File encryptedVideoFile) {
        // Tamaño del chunk en bytes (50 MB)
        long chunkSize = 50L * 1024L * 1024L;
        long fileLength = encryptedVideoFile.length();
        int totalChunks = (int) ((fileLength + chunkSize - 1) / chunkSize); // redondeo hacia arriba
        String fileId = UUID.randomUUID().toString();
        Log.d("ChunkUpload", "Total de chunks: " + totalChunks);

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        for (int i = 0; i < totalChunks; i++) {
            final int chunkIndex = i;
            long offset = chunkIndex * chunkSize;
            long length = Math.min(chunkSize, fileLength - offset);

            // Crear el RequestBody para el chunk
            ChunkedRequestBody chunkBody = new ChunkedRequestBody(
                    encryptedVideoFile,
                    offset,
                    length,
                    MediaType.parse("application/octet-stream")
            );

            // Crear RequestBody para los parámetros de texto
            RequestBody fileIdBody = RequestBody.create(MediaType.parse("text/plain"), fileId);
            RequestBody chunkIndexBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(chunkIndex));
            RequestBody totalChunksBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(totalChunks));

            MultipartBody.Part chunkPart = MultipartBody.Part.createFormData("chunkData", "chunk_" + chunkIndex, chunkBody);

            // Envolver la llamada en un hilo
            new Thread(() -> {
                Call<ResponseBody> call = apiService.uploadChunk(fileIdBody, chunkIndexBody, totalChunksBody, chunkPart);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Chunk " + chunkIndex + " enviado correctamente.");
                        } else {
                            Log.e(TAG, "Error al enviar chunk " + chunkIndex + ": " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "Fallo al enviar chunk " + chunkIndex + ": " + t.getMessage());
                    }
                });
            }).start();
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
