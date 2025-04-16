package com.example.appGrabacion.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.appGrabacion.screens.CalculadoraScreen;

public class RecordingWidgetReceiver extends BroadcastReceiver {

    public static final String ACTION_TOGGLE_RECORDING = "com.example.appGrabacion.ACTION_TOGGLE_RECORDING";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ACTION_TOGGLE_RECORDING.equals(intent.getAction())) {
            Log.d("RecordingWidgetReceiver", "Se pulsó el widget, acción recibida.");
            Intent activityIntent = new Intent(context, CalculadoraScreen.class);
            // Pasamos el extra para que la nueva actividad inicie la grabación automáticamente
            activityIntent.putExtra("autoStartRecording", true);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
            Log.d("RecordingWidgetReceiver", "CalculadoraScreen iniciada desde el widget.");
        }
    }
}
