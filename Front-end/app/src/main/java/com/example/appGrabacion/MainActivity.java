package com.example.appGrabacion;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private static final int PERMISSION_REQUEST_ALL = 1;
    private static final String TAG = "MainActivity";
    private String[] appPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    // UI: TextureView para previsualización y btn.dot (que usamos como indicador)
    private TextureView textureView;
    private Button btnDot;

    // Objeto que maneja la grabación en segundo plano y bandera para saber si se está grabando
    private BackgroundRecordingManager recordingManager;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Solicita permisos si es necesario.
        if (!hasPermissions(appPermissions)) {
            ActivityCompat.requestPermissions(this, appPermissions, PERMISSION_REQUEST_ALL);
        }

        // Asigna los componentes de la interfaz (asegúrate de que en tu layout activity_main.xml existan)
        btnDot = findViewById(R.id.btnDot);
        textureView = findViewById(R.id.textureView);
        if (textureView != null) {
            textureView.setSurfaceTextureListener(this);
        }

        // Inicializa la lógica de grabación
        recordingManager = new BackgroundRecordingManager(this, textureView);

        // Si la actividad se abrió con el extra "autoStartRecording" (por ejemplo, desde el widget), inicia la grabación
        boolean autoStart = getIntent().getBooleanExtra("autoStartRecording", false);
        if (autoStart) {
            startRecording();
        }

        // Configura el btn.dot para que al pulsarlo alterne la grabación (la actualización de fondo será la única indicación visual)
        btnDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });
    }

    /**
     * Comprueba que se tengan los permisos requeridos.
     */
    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Inicia la grabación y, mediante el callback, actualiza el botón para reflejar el estado activo.
     */
    private void startRecording() {
        recordingManager.startRecording(new BackgroundRecordingManager.LocationCallback() {
            @Override
            public void onLocationReceived() {
                // En lugar de modificar el texto, se cambia el fondo para indicar grabación activa.
                // Asegúrate de tener definidos en res/drawable los recursos "button_circle_active" y "button_circle_inactive"
                btnDot.setBackgroundResource(R.drawable.button_circle_dark);
            }
        });
        isRecording = true;
    }

    /**
     * Detiene la grabación y actualiza el botón para reflejar el estado inactivo.
     */
    private void stopRecording() {
        recordingManager.stopRecording(new BackgroundRecordingManager.LocationCallback() {
            @Override
            public void onLocationReceived() {
                btnDot.setBackgroundResource(R.drawable.button_circle_orange);
            }
        });
        isRecording = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ALL) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Log.e(TAG, "No se concedieron todos los permisos necesarios.");
            } else {
                Log.d(TAG, "Todos los permisos fueron concedidos.");
                // Si la actividad ya tenía el extra autoStart, inicia la grabación
                if (getIntent().getBooleanExtra("autoStartRecording", false)) {
                    startRecording();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recordingManager != null) {
            recordingManager.stopRecording(null);
        }
    }

    // Métodos del TextureView.SurfaceTextureListener
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "SurfaceTexture disponible");
        // Puedes iniciar la grabación aquí si así lo deseas, aunque en este ejemplo la iniciamos con el extra.
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "SurfaceTexture destruido");
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) { }
}
