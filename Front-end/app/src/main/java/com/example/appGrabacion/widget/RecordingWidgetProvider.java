package com.example.appGrabacion.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.appGrabacion.R;
import com.example.appGrabacion.screens.CalculadoraScreen;

public class RecordingWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context,
                         AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            // Intent que iniciará tu CalculadoraScreen
            Intent intent = new Intent(context, CalculadoraScreen.class);
            intent.putExtra("autoStartRecording", true);
            // Esenciales para arrancar la activity aunque la app no esté en memoria
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pi = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            RemoteViews views = new RemoteViews(
                    context.getPackageName(),
                    R.layout.widget_layout
            );
            views.setOnClickPendingIntent(R.id.widget_image, pi);
            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }
}
