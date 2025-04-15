package com.example.appGrabacion;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.appGrabacion.encriptación.CryptoUtils;
import com.example.appGrabacion.pojo.ChunkedRequestBody;
import com.example.appGrabacion.utils.ApiService;
import com.example.appGrabacion.utils.RetrofitClient;
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
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Clase que encapsula la lógica de grabación de video con audio (incluyendo el manejo del MediaRecorder, la cámara,
 * el servicio de micrófono y la combinación con la ubicación), sin modificar la funcionalidad original.
 */
public class BackgroundRecordingManager implements TextureView.SurfaceTextureListener {

    private static final String TAG = "BackgroundRecordingManager";

    private Context context;
    private TextureView textureView;
    private CameraManager cameraManager;
    private FusedLocationProviderClient fusedLocationClient;

    private CameraDevice cameraDevice;
    private MediaRecorder mediaRecorder;
    private CameraCaptureSession captureSession;

    private boolean isRecording = false;
    private String startLocation = "";
    private String endLocation = "";

    public BackgroundRecordingManager(Context context, TextureView textureView) {
        this.context = context;
        this.textureView = textureView;
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        // Se asigna el listener a la TextureView (para recibir los callbacks de la cámara)
        if (this.textureView != null) {
            this.textureView.setSurfaceTextureListener(this);
        }
    }

    /**
     * Interfaz para el callback de la obtención de la ubicación.
     */
    public interface LocationCallback {
        void onLocationReceived();
    }

    /**
     * Obtiene la ubicación actual y actualiza el valor de startLocation o endLocation según el parámetro isStart.
     *
     * @param isStart  true para la ubicación de inicio, false para la ubicación de fin.
     * @param callback Se invoca cuando se recibe la ubicación (o si es nula).
     */
    private void getLocation(final boolean isStart, final LocationCallback callback) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "No se tienen permisos de localización");
            if (callback != null) {
                callback.onLocationReceived();
            }
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        String locationData = "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude();
                        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        if (isStart) {
                            startLocation = "Inicio (" + timestamp + "): " + locationData;
                            Log.d(TAG, "Inicio: " + startLocation);
                        } else {
                            endLocation = "Fin (" + timestamp + "): " + locationData;
                            Log.d(TAG, "Fin: " + endLocation);
                        }
                    } else {
                        Log.e(TAG, "La ubicación es null");
                    }
                    if (callback != null) {
                        callback.onLocationReceived();
                    }
                });
    }

    /**
     * Inicia el servicio del micrófono.
     */
    private void startMicrophoneService() {
        Intent intent = new Intent(context, MicrophoneService.class);
        context.startService(intent);
    }

    /**
     * Detiene el servicio del micrófono.
     */
    private void stopMicrophoneService() {
        Intent intent = new Intent(context, MicrophoneService.class);
        context.stopService(intent);
    }

    /**
     * Inicia la grabación; se obtiene la ubicación de inicio, se arranca el servicio del micrófono y se inicia la grabación de video.
     *
     * @param callback Callback que se ejecuta al obtener la ubicación.
     */
    public void startRecording(final LocationCallback callback) {
        if (isRecording) {
            Log.d(TAG, "Ya se está grabando");
            return;
        }
        getLocation(true, new LocationCallback() {
            @Override
            public void onLocationReceived() {
                startMicrophoneService();
                startVideoRecording();
                if (callback != null) {
                    callback.onLocationReceived();
                }
            }
        });
    }

    /**
     * Detiene la grabación; se obtiene la ubicación final, se para el servicio del micrófono y se finaliza la grabación de video,
     * luego se combina la ubicación con el video (encriptación y subida).
     *
     * @param callback Callback que se ejecuta al finalizar la obtención de la ubicación final.
     */
    public void stopRecording(final LocationCallback callback) {
        if (!isRecording) {
            Log.d(TAG, "No hay grabación en curso");
            return;
        }
        getLocation(false, new LocationCallback() {
            @Override
            public void onLocationReceived() {
                stopMicrophoneService();
                stopVideoRecording();
                combineAudioAndLocation();
                if (callback != null) {
                    callback.onLocationReceived();
                }
            }
        });
    }

    /**
     * Inicia la grabación de video (se asume que ya se tienen los permisos necesarios).
     */
    private void startVideoRecording() {
        // Verifica que el SurfaceTexture no sea nulo.
        if (textureView.getSurfaceTexture() == null) {
            Log.d(TAG, "SurfaceTexture era null, se instancia uno nuevo manualmente");
            textureView.setSurfaceTexture(new SurfaceTexture(0));
        }

        try {
            setupMediaRecorder();
            // Se obtiene el ID de la primera cámara disponible
            String cameraId = cameraManager.getCameraIdList()[0];
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "No se tienen permisos de cámara");
                return;
            }
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
            Log.e(TAG, "Error al iniciar la grabación de video", e);
        }
    }

    /**
     * Configura el MediaRecorder con los parámetros necesarios.
     */
    private void setupMediaRecorder() throws IOException {
        mediaRecorder = new MediaRecorder();

        // Configuración de las fuentes de audio y video.
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        // Establece el formato de salida.
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        // Establece el archivo de salida en el directorio externo de la app.
        File outputFile = new File(context.getExternalFilesDir(null), "recorded_video.mp4");
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());

        // Configuración de audio.
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(96000);
        mediaRecorder.setAudioSamplingRate(44100);

        // Configuración de video.
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(1920, 1080);

        // Ajusta la orientación del video.
        mediaRecorder.setOrientationHint(90);

        // Limita la grabación a 15 minutos.
        mediaRecorder.setMaxDuration(15 * 60 * 1000);

        // Listener para el evento de tiempo máximo alcanzado.
        mediaRecorder.setOnInfoListener((mr, what, extra) -> {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                Log.d(TAG, "Se alcanzó la duración máxima, deteniendo la grabación.");
                // Para llamar al stopRecording en el hilo principal:
                new Handler(Looper.getMainLooper()).post(() -> stopRecording(null));
            }
        });

        mediaRecorder.prepare();
    }

    /**
     * Crea la sesión de captura combinando el Surface para la previsualización y el del MediaRecorder.
     */
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
                            Toast.makeText(context, "Failed to configure camera", Toast.LENGTH_SHORT).show();
                        }
                    },
                    null
            );
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error al crear la sesión de captura", e);
        }
    }

    /**
     * Inicia la previsualización y la grabación del video.
     */
    private void startPreviewAndRecording(CameraCaptureSession session) {
        try {
            CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            captureRequestBuilder.addTarget(new Surface(textureView.getSurfaceTexture()));
            captureRequestBuilder.addTarget(mediaRecorder.getSurface());

            session.setRepeatingRequest(captureRequestBuilder.build(), null, null);
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Grabación de video iniciada");
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error al iniciar la previsualización y grabación", e);
        }
    }

    /**
     * Detiene la grabación de video y libera los recursos asociados.
     */
    private void stopVideoRecording() {
        try {
            mediaRecorder.stop();
        } catch (RuntimeException e) {
            Log.e(TAG, "Error al detener el MediaRecorder. Quizás no estaba grabando", e);
        }
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        isRecording = false;
        Log.d(TAG, "Grabación de video detenida");
    }

    /**
     * Combina (encripta) el archivo de video y la información de ubicación, y luego los envía al servidor.
     */
    private void combineAudioAndLocation() {
        new Thread(() -> {
            try {
                // Archivo de video grabado.
                File videoFile = new File(context.getExternalFilesDir(null), "recorded_video.mp4");
                if (!videoFile.exists()) {
                    Log.e(TAG, "❌ Error: Archivo de video no encontrado.");
                    return;
                }

                // Encripta el video.
                File encryptedVideoFile = new File(context.getExternalFilesDir(null), "encrypted_video.mp4");
                CryptoUtils.encryptFileFlexible(videoFile, encryptedVideoFile);

                // Prepara el archivo de ubicación concatenando la ubicación de inicio y fin.
                String locationData = startLocation + "\n" + endLocation;
                Log.d(TAG, "Localización a enviar:\n" + locationData);
                File locationFile = new File(context.getExternalFilesDir(null), "location.txt");
                try (FileOutputStream fos = new FileOutputStream(locationFile)) {
                    fos.write(locationData.getBytes("UTF-8"));
                }
                if (!locationFile.exists()) {
                    Log.e(TAG, "❌ Error: Archivo de ubicación no encontrado.");
                    return;
                }

                // Encripta el archivo de ubicación.
                File encryptedLocationFile = new File(context.getExternalFilesDir(null), "encrypted_location.txt");
                CryptoUtils.encryptFileFlexible(locationFile, encryptedLocationFile);

                // Genera un ID único para identificar la subida.
                String fileId = UUID.randomUUID().toString();

                // Envía la ubicación utilizando un endpoint especial (chunkIndex = -1, totalChunks = 0).
                RequestBody fileIdBody = RequestBody.create(MediaType.parse("text/plain"), fileId);
                RequestBody chunkIndexBody = RequestBody.create(MediaType.parse("text/plain"), "-1");
                RequestBody totalChunksBody = RequestBody.create(MediaType.parse("text/plain"), "0");

                RequestBody locationRequestBody = RequestBody.create(MediaType.parse("text/plain"), encryptedLocationFile);
                MultipartBody.Part locationPart = MultipartBody.Part.createFormData("chunkData", encryptedLocationFile.getName(), locationRequestBody);

                ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
                Call<ResponseBody> callLocation = apiService.uploadChunk(fileIdBody, chunkIndexBody, totalChunksBody, locationPart);
                Response<ResponseBody> locationResponse = callLocation.execute();

                if (locationResponse.isSuccessful()) {
                    Log.d(TAG, "✅ Ubicación enviada correctamente.");
                    // Envía los chunks del video usando el mismo fileId.
                    uploadVideoChunks(fileId, encryptedVideoFile);
                } else {
                    Log.e(TAG, "❌ Error al enviar ubicación. Código: " + locationResponse.code());
                }

                // (Opcional) Elimina el archivo original de video.
                deleteFile(videoFile);

            } catch (Exception e) {
                Log.e(TAG, "❌ Error en combineAudioAndLocation", e);
            }
        }).start();
    }

    /**
     * Elimina el archivo pasado como parámetro, si existe.
     *
     * @param file Archivo a eliminar.
     */
    private void deleteFile(File file) {
        if (file.exists() && file.delete()) {
            Log.d(TAG, "Archivo eliminado exitosamente");
        }
    }

    /**
     * Divide un archivo en chunks.
     *
     * @param file      Archivo a dividir.
     * @param chunkSize Tamaño de cada chunk en bytes.
     * @return Lista con cada chunk del archivo.
     * @throws IOException Si ocurre algún error de lectura.
     */
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

    /**
     * Sube los chunks del video encriptado al servidor utilizando Retrofit y un ExecutorService para controlar la concurrencia.
     *
     * @param fileId              Identificador único del archivo.
     * @param encryptedVideoFile  Archivo de video encriptado.
     */
    public void uploadVideoChunks(String fileId, File encryptedVideoFile) {
        // Tamaño del chunk: 50 MB
        long chunkSize = 50L * 1024L * 1024L;
        long fileLength = encryptedVideoFile.length();
        int totalChunks = (int) ((fileLength + chunkSize - 1) / chunkSize);
        Log.d("ChunkUpload", "Total de chunks: " + totalChunks);

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        final int maxConcurrentUploads = 3;
        final int maxRetries = 3;
        ExecutorService executor = Executors.newFixedThreadPool(maxConcurrentUploads);

        for (int i = 0; i < totalChunks; i++) {
            final int chunkIndex = i;
            long offset = chunkIndex * chunkSize;
            long length = Math.min(chunkSize, fileLength - offset);

            // Crea el RequestBody para el chunk.
            ChunkedRequestBody chunkBody = new ChunkedRequestBody(
                    encryptedVideoFile,
                    offset,
                    length,
                    MediaType.parse("application/octet-stream")
            );

            RequestBody fileIdBody = RequestBody.create(MediaType.parse("text/plain"), fileId);
            RequestBody chunkIndexBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(chunkIndex));
            RequestBody totalChunksBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(totalChunks));

            MultipartBody.Part chunkPart = MultipartBody.Part.createFormData("chunkData", "chunk_" + chunkIndex, chunkBody);

            // Se envía cada chunk asíncronamente con reintentos.
            executor.submit(() -> {
                int attempt = 0;
                boolean uploaded = false;
                while (!uploaded && attempt < maxRetries) {
                    attempt++;
                    try {
                        Call<ResponseBody> call = apiService.uploadChunk(fileIdBody, chunkIndexBody, totalChunksBody, chunkPart);
                        Response<ResponseBody> response = call.execute();
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Chunk " + chunkIndex + " enviado correctamente.");
                            uploaded = true;
                        } else {
                            Log.e(TAG, "Error al enviar chunk " + chunkIndex + ": " + response.code() + ". Reintento " + attempt);
                            Thread.sleep(2000);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Fallo al enviar chunk " + chunkIndex + ": " + e.getMessage() + ". Reintento " + attempt);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            // Ignorar la excepción
                        }
                    }
                }
                if (!uploaded) {
                    Log.e(TAG, "No se pudo enviar el chunk " + chunkIndex + " después de " + maxRetries + " intentos.");
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
                Log.e(TAG, "Tiempo de espera excedido para la subida de chunks.");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Error esperando la finalización de la subida de chunks", e);
        }
    }

    // Métodos del TextureView.SurfaceTextureListener

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "SurfaceTexture disponible en BackgroundRecordingManager");
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // No es necesaria acción
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true; // Libera la textura
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // No es necesaria acción
    }
}
