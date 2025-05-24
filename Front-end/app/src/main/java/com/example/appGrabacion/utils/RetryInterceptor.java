package com.example.appGrabacion.utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

class RetryInterceptor implements Interceptor {
    private final int maxRetries;

    public RetryInterceptor(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        IOException exception = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return chain.proceed(request);
            } catch (IOException e) {
                exception = e;
                System.err.println("Reintento " + (attempt + 1) + " fallido: " + e.getMessage());
            }
        }
        throw exception;
    }
}
