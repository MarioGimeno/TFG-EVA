package com.example.appGrabacion.screens;

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

import com.example.appGrabacion.BackgroundRecordingManager;
import com.example.appGrabacion.R;

public class CalculadoraScreen extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private static final int PERMISSION_REQUEST_ALL = 1;
    private static final String TAG = "CalculadoraScreen";
    private String[] appPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    // Elementos de UI: TextureView para la previsualización y btnDot para indicar el estado de grabación.
    private TextureView textureView;
    private Button btnDot;

    // Objeto que controla la grabación en segundo plano
    private BackgroundRecordingManager recordingManager;
    // Bandera para conocer si se está grabando o no
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculadora_screen); // Crea un layout nuevo para esta pantalla

        // Solicita permisos si no están concedidos
        if (!hasPermissions(appPermissions)) {
            ActivityCompat.requestPermissions(this, appPermissions, PERMISSION_REQUEST_ALL);
        }

        btnDot = findViewById(R.id.btnDot);
        textureView = findViewById(R.id.textureView);

        // Configura el TextureView; se debe disponer de uno en el layout "activity_calculadora_screen.xml"
        if (textureView != null) {
            textureView.setSurfaceTextureListener(this);
        }

        // Inicializa BackgroundRecordingManager con el contexto y el TextureView
        recordingManager = new BackgroundRecordingManager(this, textureView);

        // Si se pasó el extra "autoStartRecording" (desde el widget) se inicia la grabación
        boolean autoStart = getIntent().getBooleanExtra("autoStartRecording", false);
        if (autoStart) {
            startRecording();
        }

        // Configura el botón de manera que al tocarlo se alterne la grabación
        // pero solo cambia su color (no muestra texto de "iniciar" o "detener")
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
     * Verifica que se tengan todos los permisos necesarios.
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
