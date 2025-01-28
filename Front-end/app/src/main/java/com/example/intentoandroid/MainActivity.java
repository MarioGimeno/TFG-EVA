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
<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
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
            getLocation(true);
            startMicrophoneService();
            startVideoRecording();
        }

        startRecordingButton.setOnClickListener(v -> {
            if (!isRecording) {
                // Obtener ubicación al inicio
                getLocation(true);
                startMicrophoneService();
                startVideoRecording();
            }
        });

        stopRecordingButton.setOnClickListener(v -> {
            if (isRecording) {
                stopVideoRecording();
                stopMicrophoneService();
                // Obtener ubicación al final
                getLocation(false);
                combineAudioAndVideo();
            }
        });
    }

    private void getLocation(boolean isStart) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<android.location.Location>() {
                    @Override
                    public void onSuccess(android.location.Location location) {
                        if (location != null) {
                            String locationData = "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude();
                            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                            // Asigna la ubicación al inicio o al final
                            if (isStart) {
                                startLocation = "Inicio (" + timestamp + "): " + locationData;
                                Log.d(TAG, "Inicio: " + startLocation);
                            } else {
                                endLocation = "Fin (" + timestamp + "): " + locationData;
                                Log.d(TAG, "Fin: " + endLocation);
                            }

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
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        File outputFile = new File(getExternalFilesDir(null), "recorded_video.mp4");
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());

        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(1920, 1080);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

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
<<<<<<< Updated upstream
        mediaRecorder.stop();
=======
        try {
            mediaRecorder.stop();
        } catch (RuntimeException e) {
            Log.e(TAG, "Error al detener el MediaRecorder, puede que no estuviera grabando", e);
        }
>>>>>>> Stashed changes
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        // Obtener la ubicación final
        getLocation(false);
        isRecording = false;
        Log.d(TAG, "Video recording stopped");
    }

    private void combineAudioAndVideo() {
        // Rutas de los archivos de video y audio grabados
        File videoFile = new File(getExternalFilesDir(null), "recorded_video.mp4");
        File audioFile = new File(getExternalFilesDir(null), "audio_record.mp4");

        if (!videoFile.exists()) {
            Log.e(TAG, "Video file does not exist.");
            return;
        }

        if (!audioFile.exists()) {
            Log.e(TAG, "Audio file does not exist.");
            return;
        }

        // Crear los objetos RequestBody para los archivos
        RequestBody videoRequestBody = RequestBody.create(MediaType.parse("video/mp4"), videoFile);
        MultipartBody.Part videoPart = MultipartBody.Part.createFormData("video", videoFile.getName(), videoRequestBody);

        RequestBody audioRequestBody = RequestBody.create(MediaType.parse("audio/mp4"), audioFile);
        MultipartBody.Part audioPart = MultipartBody.Part.createFormData("audio", audioFile.getName(), audioRequestBody);

        // Combinar ambas ubicaciones
        String locationData = startLocation + "\n" + endLocation;
        Log.e("MainActivityLocatlion", "Start location "+ startLocation);
        Log.e("MainActivityLocatlion", "End location "+ startLocation);

        RequestBody locationRequestBody = RequestBody.create(MediaType.parse("text/plain"), startLocation + " " + endLocation);
        MultipartBody.Part locationPart = MultipartBody.Part.createFormData("location", "location.txt", locationRequestBody);

        // Llamada a la API de Retrofit
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<ResponseBody> call = apiService.uploadVideoAndAudio(videoPart, audioPart, locationPart);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Archivos enviados correctamente!");
                    deleteFiles(videoFile, audioFile);
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
    private void deleteFiles(File videoFile, File audioFile) {
        if (videoFile.exists() && videoFile.delete()) {
            Log.d(TAG, "Video file deleted successfully");
        }
        if (audioFile.exists() && audioFile.delete()) {
            Log.d(TAG, "Audio file deleted successfully");
        }
    }
<<<<<<< Updated upstream
=======

    private void guardarGrabacion(){
        stopVideoRecording();
        stopMicrophoneService();
        // Obtener ubicación al final
        getLocation(false);
        combineAudioAndVideo();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopVideoRecording();
        stopMicrophoneService();
        // Obtener ubicación al final
        getLocation(false);
        combineAudioAndVideo();
    }

    // Implementación de TextureView.SurfaceTextureListener
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "SurfaceTexture disponible");
        // Obtener ubicación al inicio
        getLocation(true);
        startMicrophoneService();
        startVideoRecording();
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


>>>>>>> Stashed changes
}
