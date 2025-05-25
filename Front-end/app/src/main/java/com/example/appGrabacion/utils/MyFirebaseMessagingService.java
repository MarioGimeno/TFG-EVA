package com.example.appGrabacion.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.appGrabacion.R;
import com.example.appGrabacion.entities.TokenRequest;
import com.example.appGrabacion.screens.MapsActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.ContentResolver;
import android.media.AudioAttributes;
import android.net.Uri;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "live_loc";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /** Crea el canal de notificación para Android O+ */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Live Location";
            String description = "Notificaciones de ubicación en vivo";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                    + getPackageName() + "/raw/alerta");
            AudioAttributes audioAttrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttrs);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});

            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    /** Se dispara cuando se genera un nuevo token FCM */
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nuevo token FCM: " + token);
        registerTokenWithServer(this, token);
    }

    /** Se llama al llegar un mensaje data-only */
    @Override
    public void onMessageReceived(RemoteMessage message) {
        SessionManager sm = new SessionManager(this);
        if (!sm.isLoggedIn()) {
            Log.d(TAG, "Usuario desconectado: ignoro notificación");
            return;
        }
        Map<String, String> data = message.getData();
        Log.d(TAG, "Mensaje FCM recibido: " + data);

        String latStr = data.get("latitude");
        String lonStr = data.get("longitude");
        if (latStr != null && lonStr != null) {
            try {
                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);
                showNotification(lat, lon);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Lat/Lon inválidos en la notificación", e);
            }
        } else {
            Log.w(TAG, "Payload sin latitude/longitude");
        }
    }

    /**
     * Construye y dispara la notificación,
     * usando FLAG_UPDATE_CURRENT para siempre actualizar los extras lat/lon.
     */
    private void showNotification(double lat, double lon) {
        // 1) Intent que abrirá MapsActivity con los extras nuevos
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("lat", lat);
        intent.putExtra("lon", lon);
        // Lanzamos en nueva tarea y borramos la anterior para que solo haya una instancia
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 2) PendingIntent con FLAG_UPDATE_CURRENT para actualizar lat/lon en cada notificación
        PendingIntent pi = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 3) Construye la notificación como antes
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + getPackageName() + "/raw/alerta");

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_download)
                        .setContentTitle("Ubicación en vivo")
                        .setContentText("Pulsa para ver en el mapa")
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setSound(soundUri)
                        .setVibrate(new long[]{0, 500, 200, 500});

        // 4) Comprueba permisos y lanza
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Sin permiso de notificaciones, no se mostrará");
                return;
            }
        }
        NotificationManagerCompat.from(this).notify(1001, builder.build());
    }


    /** Envía el token al backend */
    public static void registerTokenWithServer(Context ctx, String fcmToken) {
        ApiService api = RetrofitClient
                .getRetrofitInstance(ctx)
                .create(ApiService.class);

        String bearer = "Bearer " + new SessionManager(ctx).fetchToken();

        Call<Void> call = api.registerToken(bearer, new TokenRequest(fcmToken));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) {
                    Log.d(TAG, "Token registrado en servidor");
                } else {
                    Log.e(TAG, "Error registrando token: " + r.code());
                }
            }
            @Override
            public void onFailure(Call<Void> c, Throwable t) {
                Log.e(TAG, "Fallo al registrar token", t);
            }
        });
    }
}
