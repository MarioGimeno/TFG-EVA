package com.example.intentoandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.*;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.intentoandroid.SegundoPlano.MicrophoneService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        startRecordingButton = findViewById(R.id.StartService);
        stopRecordingButton = findViewById(R.id.StopService);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        startRecordingButton.setOnClickListener(v -> {
            if (!isRecording) {
                startMicrophoneService();
                startVideoRecording();
            }
        });

        stopRecordingButton.setOnClickListener(v -> {
            if (isRecording) {
                stopVideoRecording();
                stopMicrophoneService();
                combineAudioAndVideo();
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
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION);
            return;
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
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }

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

        // Para la ubicación, puedes enviarla como un texto o archivo
        String locationData = "Ubicación: XYZ"; // Este valor lo puedes obtener del servicio de ubicación
        RequestBody locationRequestBody = RequestBody.create(MediaType.parse("text/plain"), locationData);
        MultipartBody.Part locationPart = MultipartBody.Part.createFormData("location", "location.txt", locationRequestBody);

        // Llamada a la API de Retrofit
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<ResponseBody> call = apiService.uploadVideoAndAudio(videoPart, audioPart, locationPart);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Usar Log para mostrar que los archivos fueron enviados correctamente
                    Log.d(TAG, "Archivos enviados correctamente!");

                    // Borrar los archivos del externalFilesDir después de enviar
                    deleteFiles(videoFile, audioFile);
                } else {
                    // Usar Log para mostrar que hubo un error al enviar los archivos
                    Log.e(TAG, "Error al enviar los archivos. Código de respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Usar Log para mostrar el error de red
                Log.e(TAG, "Error de red: " + t.getMessage());
            }
        });
    }

    // Método para eliminar los archivos del externalFilesDir
    private void deleteFiles(File videoFile, File audioFile) {
        if (videoFile.exists()) {
            if (videoFile.delete()) {
                Log.d(TAG, "Video file deleted successfully");
            } else {
                Log.e(TAG, "Failed to delete video file");
            }
        }

        if (audioFile.exists()) {
            if (audioFile.delete()) {
                Log.d(TAG, "Audio file deleted successfully");
            } else {
                Log.e(TAG, "Failed to delete audio file");
            }
        }
    }

}