package com.gr6.smartcart_android.common.network;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gr6.smartcart_android.common.utils.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;

    private ApiClient() {
    }

    public static Retrofit getRetrofit(Context context) {
        if (retrofit == null) {
            retrofit = buildRetrofit(context.getApplicationContext());
        }

        return retrofit;
    }

    public static <T> T createService(Context context, Class<T> serviceClass) {
        return getRetrofit(context).create(serviceClass);
    }

    public static void reset() {
        retrofit = null;
    }

    private static Retrofit buildRetrofit(Context context) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();

        /*
         * KHÔNG dùng BODY trên máy thật.
         * BODY sẽ in toàn bộ JSON product/detail/voucher/review ra Logcat
         * và làm máy lag rất rõ.
         */
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context))
                .addInterceptor(loggingInterceptor)
                .connectTimeout(Constants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(Constants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(Constants.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        String baseUrl = normalizeBaseUrl(Constants.BASE_URL);

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    private static String normalizeBaseUrl(String rawBaseUrl) {
        String baseUrl = rawBaseUrl == null ? "" : rawBaseUrl.trim();

        // Xóa toàn bộ khoảng trắng để tránh lỗi kiểu: "http:// 172.20.10.3:8080/"
        baseUrl = baseUrl.replaceAll("\\s+", "");

        if (baseUrl.isEmpty()) {
            baseUrl = "http://192.168.2.8:8080/";
        }

        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        return baseUrl;
    }
}