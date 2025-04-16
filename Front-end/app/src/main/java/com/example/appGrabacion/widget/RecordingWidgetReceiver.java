package com.example.appGrabacion.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RecordingWidgetReceiver extends BroadcastReceiver {

    public static final String ACTION_TOGGLE_RECORDING = "com.example.appGrabacion.ACTION_TOGGLE_RECORDING";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ACTION_TOGGLE_RECORDING.equals(intent.getAction())) {
            Log.d("RecordingWidgetReceiver", "Se pulsó el widget, acción recibida.");
            // Creamos un intent para abrir MainActivity
            Intent activityIntent = new Intent(context, com.example.appGrabacion.MainActivity.class);
            // Enviamos el extra para que la MainActivity inicie la grabación automáticamente
            activityIntent.putExtra("autoStartRecording", true);
            // Al iniciar una actividad desde un BroadcastReceiver es obligatorio agregar esta bandera
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
            Log.d("RecordingWidgetReceiver", "MainActivity iniciada desde el widget.");
        }
    }
}
