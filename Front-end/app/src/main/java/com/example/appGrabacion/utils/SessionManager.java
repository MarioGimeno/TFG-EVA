package com.example.appGrabacion.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS = "prefs_session";
    private static final String KEY_TOKEN = "key_token";
    private SharedPreferences prefs;

    public SessionManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String fetchToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return fetchToken() != null;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
    /** Método estático de conveniencia **/
    public static String getToken(Context ctx) {
        return new SessionManager(ctx).fetchToken();
    }
}
