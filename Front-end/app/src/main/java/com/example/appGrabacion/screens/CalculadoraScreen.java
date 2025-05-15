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
import android.widget.TextView;
import java.text.DecimalFormat;


import com.example.appGrabacion.BackgroundRecordingManager;
import com.example.appGrabacion.R;

public class CalculadoraScreen extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    // ——— Campos para la calculadora —————
    private TextView txtDisplay, txtOperation;
    private String    currentNumber = "";
    private String    operator      = "";
    private double    firstNumber   = 0;
    private boolean   isNewInput    = true;


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
        // — Inicializa la parte de la calculadora —
        txtDisplay   = findViewById(R.id.txtDisplay);
        txtOperation = findViewById(R.id.txtOperation);
        setNumberButtonListeners();
        setOperatorButtonListeners();

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
        recordingManager.startRecording(new BackgroundRecordingManager.RecordingLocationListener() {
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
        recordingManager.stopRecording(new BackgroundRecordingManager.RecordingLocationListener() {
            @Override
            public void onLocationReceived() {
                btnDot.setBackgroundResource(R.drawable.button_circle);
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

    private void setNumberButtonListeners() {
        int[] numberIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
                R.id.btn8, R.id.btn9
        };
        View.OnClickListener listener = v -> {
            String t = ((Button)v).getText().toString();
            if (isNewInput) {
                currentNumber = t;
                isNewInput = false;
            } else {
                if (t.equals(".") && currentNumber.contains(".")) return;
                currentNumber += t;
            }
            txtDisplay.setText(currentNumber);
        };
        for (int id : numberIds) findViewById(id).setOnClickListener(listener);
    }

    private void setOperatorButtonListeners() {
        int[] operatorIds = {
                R.id.btnAdd, R.id.btnSubtract,
                R.id.btnMultiply, R.id.btnDivide,
                R.id.btnEqual, R.id.btnClear,
                R.id.btnPercent, R.id.btnSign
        };
        View.OnClickListener listener = v -> {
            String bt = ((Button)v).getText().toString();
            switch (bt) {
                case "AC":
                    firstNumber   = 0;
                    currentNumber = "0";
                    operator      = "";
                    isNewInput    = true;
                    txtDisplay.setText(currentNumber);
                    txtOperation.setText("");
                    break;
                case "±":
                    if (!currentNumber.isEmpty()) {
                        double num = Double.parseDouble(currentNumber) * -1;
                        currentNumber = formatNumber(num);
                        txtDisplay.setText(currentNumber);
                    }
                    break;
                case "%":
                    if (!currentNumber.isEmpty()) {
                        double num = Double.parseDouble(currentNumber) / 100;
                        currentNumber = formatNumber(num);
                        txtDisplay.setText(currentNumber);
                    }
                    break;
                case "=":
                    performCalculation();
                    break;
                default:
                    if (!currentNumber.isEmpty()) {
                        if (operator.isEmpty()) {
                            firstNumber = Double.parseDouble(currentNumber);
                        } else {
                            performCalculation();
                            firstNumber = Double.parseDouble(txtDisplay.getText().toString());
                        }
                        operator   = bt;
                        isNewInput = true;
                        txtOperation.setText(formatNumber(firstNumber) + " " + operator);
                    }
                    break;
            }
        };
        for (int id : operatorIds) findViewById(id).setOnClickListener(listener);
    }

    private void performCalculation() {
        if (operator.isEmpty() || currentNumber.isEmpty()) return;
        double second = Double.parseDouble(currentNumber);
        double result = 0;
        switch (operator) {
            case "+": result = firstNumber + second; break;
            case "-": result = firstNumber - second; break;
            case "×": result = firstNumber * second; break;
            case "÷":
                if (second == 0) {
                    txtDisplay.setText("Error");
                    return;
                }
                result = firstNumber / second;
                break;
        }
        txtOperation.setText(
                formatNumber(firstNumber) + " " + operator +
                        " " + formatNumber(second) + " ="
        );
        txtDisplay.setText(formatNumber(result));
        currentNumber = String.valueOf(result);
        operator      = "";
        isNewInput    = true;
    }

    private String formatNumber(double num) {
        DecimalFormat df = new DecimalFormat("#.#####");
        if (num == (int) num) {
            return String.valueOf((int) num);
        } else {
            return df.format(num);
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
