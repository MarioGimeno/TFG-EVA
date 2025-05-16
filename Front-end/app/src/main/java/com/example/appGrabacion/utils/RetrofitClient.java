// src/main/java/com/example/appGrabacion/utils/RetrofitClient.java

package com.example.appGrabacion.utils;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://192.168.29.64:3000/";
    private static Retrofit retrofit;

    /**
     * Obtiene la instancia de Retrofit. Recibe un Context para poder leer el token.
     */
    public static Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            // Interceptor de logging
            HttpLoggingInterceptor log = new HttpLoggingInterceptor(msg -> Log.d("OkHttp", msg));
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Cliente OkHttp con interceptor que añade el header Authorization
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = new SessionManager(context).fetchToken();
                        Request reqWithAuth = original.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(reqWithAuth);
                    })
                    .addInterceptor(log)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
