package com.example.intentoandroid;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://52.3.116.213:3000"; // Cambia esto a tu URL del backend

    private static Retrofit retrofit;
    static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(0, TimeUnit.MILLISECONDS)  // Sin límite de tiempo para conectar
            .readTimeout(0, TimeUnit.MILLISECONDS)     // Sin límite de tiempo para lectura
            .writeTimeout(0, TimeUnit.MILLISECONDS)    // Sin límite de tiempo para escritura
            .build();
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
