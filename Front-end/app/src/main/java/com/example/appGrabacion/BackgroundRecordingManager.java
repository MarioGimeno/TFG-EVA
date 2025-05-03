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
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.appGrabacion.encriptación.CryptoUtils;
import com.example.appGrabacion.models.ContactEntry;
import com.example.appGrabacion.models.EmailRequest;
import com.example.appGrabacion.models.EmailResponse;
import com.example.appGrabacion.models.LocationUpdateRequest;
import com.example.appGrabacion.pojo.ChunkedRequestBody;
import com.example.appGrabacion.utils.ApiService;
import com.example.appGrabacion.utils.NotificationsApi;
import com.example.appGrabacion.utils.RetrofitClient;
import com.example.appGrabacion.utils.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.example.appGrabacion.utils.ContactManager;

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
import retrofit2.Callback;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.gson.Gson;

/**
 * Clase que encapsula la lógica de grabación de video con audio (incluyendo el manejo del MediaRecorder, la cámara,
 * el servicio de micrófono y la combinación con la ubicación), sin modificar la funcionalidad original.
 */
public class BackgroundRecordingManager implements TextureView.SurfaceTextureListener {

    private static final String TAG = "BackgroundRecordingManager";

    private Context context;
    private TextureView textureView;
    private FusedLocationProviderClient fusedLocationClient;
    protected CameraManager cameraManager;
    protected CameraDevice cameraDevice;
    protected MediaRecorder mediaRecorder;
    protected CameraCaptureSession captureSession;
    private ContactManager contactManager;

    public boolean isRecording = false;
    private String startLocation = "";
    private String endLocation = "";
    private LocationRequest liveLocationRequest;
    private com.google.android.gms.location.LocationCallback liveLocationCallback;
    public BackgroundRecordingManager(Context context, TextureView textureView) {
        this.context = context;
        this.textureView = textureView;
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        contactManager = new ContactManager(context);

        // Se asigna el listener a la TextureView (para recibir los callbacks de la cámara)
        if (this.textureView != null) {
            this.textureView.setSurfaceTextureListener(this);
        }
    }


    /**
     * Interfaz para el callback de la obtención de la ubicación.
     */
    public interface RecordingLocationListener {
        void onLocationReceived();
    }

    /**
     * Obtiene la ubicación actual y actualiza el valor de startLocation o endLocation según el parámetro isStart.
     *
     * @param isStart  true para la ubicación de inicio, false para la ubicación de fin.
     * @param callback Se invoca cuando se recibe la ubicación (o si es nula).
     */
    private void getLocation(final boolean isStart, final RecordingLocationListener callback) {
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
    private void startLiveLocationUpdates() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "No hay permiso de ubicación para live updates");
            return;
        }
        liveLocationRequest = LocationRequest.create()
                .setInterval(30000)
                .setFastestInterval(10000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        liveLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null) return;
                double lat = result.getLastLocation().getLatitude();
                double lon = result.getLastLocation().getLongitude();
                String mapsLink = "https://maps.google.com/?q=" + lat + "," + lon;
                String body = "Ubicación en vivo: " + mapsLink;
                sendLiveLocationPush(lat, lon);
            }
        };
        fusedLocationClient.requestLocationUpdates(
                liveLocationRequest,
                liveLocationCallback,
                Looper.getMainLooper()
        );
    }

    private void stopLiveLocationUpdates() {
        if (liveLocationCallback != null) {
            fusedLocationClient.removeLocationUpdates(liveLocationCallback);
        }
    }

    private void sendLiveLocationPush(double lat, double lon) {
        List<Integer> contacts = contactManager.getContactIds(); // IDs o tokens que guardes
        LocationUpdateRequest req = new LocationUpdateRequest(contacts, lat, lon);
        Log.d(TAG, "Contactos recuperados en cliente: " + contacts);
        Log.d(TAG, "Payload JSON enviado: " + new Gson().toJson(req));
        NotificationsApi api = RetrofitClient
                .getRetrofitInstance(context)
                .create(NotificationsApi.class);
        String bearer = "Bearer " + SessionManager.getToken(context);
// Suponiendo que sendLocationUpdate devuelve Call<Void> o Call<LocationResponse>
        api.sendLocationUpdate(bearer, req)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Live location sent successfully");
                        } else {
                            Log.e(TAG, "Error sending live location: code=" + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Failed to send live location", t);
                    }
                });
    }

    /**
     * Inicia la grabación; se obtiene la ubicación de inicio, se arranca el servicio del micrófono y se inicia la grabación de video.
     *
     * @param callback Callback que se ejecuta al obtener la ubicación.
     */
    public void startRecording(final RecordingLocationListener callback) {
        if (isRecording) return;
        getLocation(true, () -> {
            startLiveLocationUpdates();
            startMicrophoneService();
            startVideoRecording();
            if (callback != null) callback.onLocationReceived();
        });
    }


    /**
     * Detiene la grabación; se obtiene la ubicación final, se para el servicio del micrófono y se finaliza la grabación de video,
     * luego se combina la ubicación con el video (encriptación y subida).
     *
     * @param callback Callback que se ejecuta al finalizar la obtención de la ubicación final.
     */
    public void stopRecording(final RecordingLocationListener callback) {
        if (!isRecording) {
            Log.d(TAG, "No hay grabación en curso");
            return;
        }
        getLocation(false, new RecordingLocationListener() {
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
    protected void startVideoRecording() {
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
    protected void setupMediaRecorder() throws IOException {
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
    protected void startPreviewAndRecording(CameraCaptureSession session) {
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
     * Combina (encripta) el archivo de video y la información de ubicación,
     * y luego los envía al servidor usando el endpoint /upload/upload-chunk.
     */
    private void combineAudioAndLocation() {
        new Thread(() -> {
            try {
                // 1) Localiza y comprueba el fichero de video
                File videoFile = new File(context.getExternalFilesDir(null), "recorded_video.mp4");
                if (!videoFile.exists()) {
                    Log.e(TAG, "❌ Error: Archivo de video no encontrado.");
                    return;
                }

                // 2) Encripta el video
                File encryptedVideoFile = new File(context.getExternalFilesDir(null), "encrypted_video.mp4");
                CryptoUtils.encryptFileFlexible(videoFile, encryptedVideoFile);

                // 3) Prepara la info de ubicación
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

                // 4) Encripta la ubicación
                File encryptedLocationFile = new File(context.getExternalFilesDir(null), "encrypted_location.txt");
                CryptoUtils.encryptFileFlexible(locationFile, encryptedLocationFile);

                // 5) Genera un ID único para esta sesión de subida
                String fileId = UUID.randomUUID().toString();

                // 6) Crea los RequestBody y el Part para la ubicación
                RequestBody fileIdBody      = RequestBody.create(MediaType.parse("text/plain"), fileId);
                RequestBody chunkIndexBody  = RequestBody.create(MediaType.parse("text/plain"), "-1");
                RequestBody totalChunksBody = RequestBody.create(MediaType.parse("text/plain"), "0");

                RequestBody locationBody = RequestBody.create(
                        MediaType.parse("application/octet-stream"),
                        encryptedLocationFile
                );
                MultipartBody.Part locationPart = MultipartBody.Part.createFormData(
                        "chunkData",
                        encryptedLocationFile.getName(),
                        locationBody
                );

                // 7) Prepara ApiService (el interceptor añade el JWT automáticamente)
                ApiService api = RetrofitClient
                        .getRetrofitInstance(context)
                        .create(ApiService.class);

                // 8) Obtén el token y llama a uploadChunk con el header y las partes
                String token  = new SessionManager(context).fetchToken();
                String bearer = "Bearer " + token;

                Response<ResponseBody> locResp = api.uploadChunk(
                        bearer,
                        fileIdBody,
                        chunkIndexBody,
                        totalChunksBody,
                        locationPart
                ).execute();

                if (locResp.isSuccessful()) {
                    Log.d(TAG, "✅ Ubicación enviada correctamente.");
                    // 9) Si OK, sube los chunks del vídeo
                    uploadVideoChunks(fileId, encryptedVideoFile);
                } else {
                    Log.e(TAG, "❌ Error al enviar ubicación. Código: "
                            + locResp.code() + " / " + locResp.errorBody().string());
                }

                // 10) (Opcional) elimina el vídeo original
                deleteFile(videoFile);

            } catch (Exception e) {
                Log.e(TAG, "❌ Error en combineAudioAndLocation", e);
            }
        }).start();
    }

    /**
     * Divide el archivo de vídeo en chunks y los sube al servidor.
     * Firma: uploadChunk(String authHeader, RequestBody fileId, RequestBody chunkIndex,
     *                    RequestBody totalChunks, MultipartBody.Part chunkData)
     */
    public void uploadVideoChunks(String fileId, File encryptedVideoFile) {
        long chunkSize  = 50L * 1024L * 1024L; // 50MB
        long fileLength = encryptedVideoFile.length();
        int totalChunks = (int)((fileLength + chunkSize - 1) / chunkSize);

        Log.d(TAG, "Total de chunks: " + totalChunks);

        ApiService api = RetrofitClient
                .getRetrofitInstance(context)
                .create(ApiService.class);

        // Prepara el header una vez
        String token  = new SessionManager(context).fetchToken();
        String bearer = "Bearer " + token;

        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (int i = 0; i < totalChunks; i++) {
            final int chunkIndex = i;
            long offset = chunkIndex * chunkSize;
            long length = Math.min(chunkSize, fileLength - offset);

            // Construye el RequestBody para este chunk
            ChunkedRequestBody chunkBody = new ChunkedRequestBody(
                    encryptedVideoFile, offset, length,
                    MediaType.parse("application/octet-stream")
            );
            MultipartBody.Part chunkPart = MultipartBody.Part.createFormData(
                    "chunkData", "chunk_" + chunkIndex, chunkBody
            );

            RequestBody fileIdBody      = RequestBody.create(
                    MediaType.parse("text/plain"), fileId);
            RequestBody chunkIndexBody  = RequestBody.create(
                    MediaType.parse("text/plain"), String.valueOf(chunkIndex));
            RequestBody totalChunksBody = RequestBody.create(
                    MediaType.parse("text/plain"), String.valueOf(totalChunks));

            executor.submit(() -> {
                int attempts = 0;
                boolean success = false;
                while (!success && attempts < 3) {
                    attempts++;
                    try {
                        Response<ResponseBody> resp = api.uploadChunk(
                                bearer,
                                fileIdBody,
                                chunkIndexBody,
                                totalChunksBody,
                                chunkPart
                        ).execute();

                        if (resp.isSuccessful()) {
                            Log.d(TAG, "Chunk " + chunkIndex + " subido correctamente.");
                            success = true;
                        } else {
                            Log.e(TAG, "Error chunk " + chunkIndex
                                    + " code=" + resp.code()
                                    + " intento " + attempts);
                            Thread.sleep(2000);
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Fallo chunk " + chunkIndex
                                + ": " + ex.getMessage()
                                + " intento " + attempts);
                        try { Thread.sleep(2000); } catch (InterruptedException ignore) {}
                    }
                }
                if (!success) {
                    Log.e(TAG, "No se pudo subir chunk " + chunkIndex);
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
                Log.e(TAG, "Timeout subida de chunks.");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Error esperando subida de chunks", e);
        }
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
