package com.example.appGrabacion.utils;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://56.228.65.138:3000/";
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            // Logging
            HttpLoggingInterceptor log = new HttpLoggingInterceptor(msg -> Log.d("OkHttp", msg));
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Interceptor de autorización
            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                String token = new SessionManager(context).fetchToken();
                Request reqWithAuth = original.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(reqWithAuth);
            };

            // Interceptor de reintentos
            Interceptor retryInterceptor = new RetryInterceptor(3); // 3 reintentos

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(retryInterceptor)
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

    /**
     * Interceptor que reintenta peticiones fallidas hasta maxRetries veces
     */
    private static class RetryInterceptor implements Interceptor {
        private final int maxRetries;

        public RetryInterceptor(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            IOException lastException = null;

            for (int attempt = 0; attempt <= maxRetries; attempt++) {
                try {
                    return chain.proceed(request);
                } catch (IOException e) {
                    lastException = e;
                    Log.w("RetryInterceptor", "Error en intento " + (attempt + 1) + ": " + e.getMessage());
                    try {
                        Thread.sleep(1000L * (attempt + 1)); // Espera creciente
                    } catch (InterruptedException ignored) {}
                }
            }

            throw lastException;
        }
    }
}
