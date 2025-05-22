package com.example.appGrabacion.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_TOKEN = "key_token";
    private static final String KEY_USER = "key_user";

    private SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Guardar token
    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
        Log.d("SessionManager", "Token guardado: " + token);
    }

    // Obtener token
    public String fetchToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // Guardar nombre usuario
    public void saveUserName(String fullName) {
        prefs.edit().putString(KEY_USER, fullName).apply();
        Log.d("SessionManager", "Nombre usuario guardado: " + fullName);
    }

    // Obtener nombre usuario
    public String fetchUserName() {
        return prefs.getString(KEY_USER, null);
    }

    // Saber si está logueado (token existe y no vacío)
    public boolean isLoggedIn() {
        String token = fetchToken();
        return token != null && !token.isEmpty();
    }

    // Limpiar todo (token y usuario)
    public void clear() {
        boolean success = prefs.edit().clear().commit();
        if (success) {
            Log.d("SessionManager", "SharedPreferences limpiado correctamente");
        } else {
            Log.e("SessionManager", "Error al limpiar SharedPreferences");
        }
    }

    // Método estático para obtener token sin instanciar (opcional)
    public static String getToken(Context context) {
        return new SessionManager(context).fetchToken();
    }

    // Método estático para obtener usuario sin instanciar (opcional)
    public static String getUserName(Context context) {
        return new SessionManager(context).fetchUserName();
    }
}
