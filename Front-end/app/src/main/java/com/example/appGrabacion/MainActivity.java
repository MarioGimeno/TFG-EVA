package com.example.appGrabacion;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

    // UI: Header con un botón y el TextureView para la previsualización.
    private TextureView textureView;
    private Button btnRecord;

    // Estado de grabación y objeto que maneja la grabación en segundo plano.
    private boolean isRecording = false;
    private BackgroundRecordingManager recordingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Puedes cambiar el tema si lo deseas; aquí se usa uno por defecto.
        setContentView(R.layout.activity_main);

        // Solicita los permisos necesarios.
        if (!hasPermissions(appPermissions)) {
            ActivityCompat.requestPermissions(this, appPermissions, PERMISSION_REQUEST_ALL);
        }

        // Se asume que el layout activity_main.xml tiene un botón con id btnRecord y un TextureView con id textureView.
        btnRecord = findViewById(R.id.btnDot);
        textureView = findViewById(R.id.textureView);
        if (textureView != null) {
            textureView.setSurfaceTextureListener(this);
        }

        // Inicializa el BackgroundRecordingManager.
        recordingManager = new BackgroundRecordingManager(this, textureView);

        // Configura el botón para iniciar/detener la grabación.
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    // Inicia la grabación.
                    recordingManager.startRecording(new BackgroundRecordingManager.LocationCallback() {
                        @Override
                        public void onLocationReceived() {
                            // Actualiza la UI: por ejemplo, cambiar el texto del botón.
                            btnRecord.setText("Detener Grabación");
                        }
                    });
                    isRecording = true;
                } else {
                    // Detiene la grabación.
                    recordingManager.stopRecording(new BackgroundRecordingManager.LocationCallback() {
                        @Override
                        public void onLocationReceived() {
                            // Actualiza la UI: por ejemplo, cambiar el texto del botón.
                            btnRecord.setText("Iniciar Grabación");
                        }
                    });
                    isRecording = false;
                }
            }
        });
    }

    /**
     * Verifica que se tengan los permisos necesarios.
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
                Toast.makeText(this, "Se requieren permisos para grabar", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Todos los permisos fueron concedidos.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Si la app se cierra y estaba grabando, se detiene la grabación.
        if (isRecording && recordingManager != null) {
            recordingManager.stopRecording(null);
        }
    }

    // Métodos de TextureView.SurfaceTextureListener
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "SurfaceTexture disponible");
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
}
