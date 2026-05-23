package com.gr6.smartcart_android.common.network;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.common.storage.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context appContext;

    public AuthInterceptor(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // API public thì KHÔNG gắn token.
        // Tránh token cũ/sai làm backend trả 403 dù endpoint permitAll.
        if (isPublicRequest(originalRequest)) {
            return chain.proceed(originalRequest);
        }

        String token = TokenManager.getInstance(appContext).getToken();

        if (token == null || token.trim().isEmpty()) {
            return chain.proceed(originalRequest);
        }

        Request newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token.trim())
                .build();

        return chain.proceed(newRequest);
    }

    private boolean isPublicRequest(Request request) {
        String method = request.method();
        String path = request.url().encodedPath();

        if ("GET".equalsIgnoreCase(method)
                && path.startsWith("/api/v1/fulfillment/product/")) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && path.startsWith("/api/v1/vouchers/shop/")) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method)
                && (path.equals("/api/v1/categories") || path.startsWith("/api/v1/categories/"))) {
            return true;
        }

        if (path.startsWith("/api/v1/auth/")
                || path.startsWith("/api/v2/auth/")
                || path.startsWith("/api/v1/storefront/discovery/")
                || path.startsWith("/api/storefront/")) {
            return true;
        }

        return false;
    }
}