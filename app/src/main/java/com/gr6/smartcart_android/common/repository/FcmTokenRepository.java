package com.gr6.smartcart_android.common.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.gr6.smartcart_android.common.api.FcmTokenApiService;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.common.request.FcmTokenRequest;
import com.gr6.smartcart_android.common.storage.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FcmTokenRepository {

    private static final String TAG = "FcmTokenRepository";
    private static FcmTokenRepository instance;

    private final Context context;
    private final FcmTokenApiService apiService;

    private FcmTokenRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = ApiClient.createService(this.context, FcmTokenApiService.class);
    }

    public static synchronized FcmTokenRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FcmTokenRepository(context);
        }
        return instance;
    }

    public void refreshAndSendToken() {
        Log.d(TAG, "Bắt đầu refreshAndSendToken");

        if (!TokenManager.getInstance(context).hasToken()) {
            Log.w(TAG, "Chưa có JWT, không gửi FCM token");
            return;
        }

        try {
            FirebaseApp.initializeApp(context);
        } catch (Exception e) {
            Log.w(TAG, "FirebaseApp initializeApp bỏ qua: " + e.getMessage());
        }

        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Không lấy được FCM token", task.getException());
                            return;
                        }

                        String token = task.getResult();
                        Log.d(TAG, "FCM token lấy được = " + token);
                        sendTokenToServer(token);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi gọi FirebaseMessaging.getToken()", e);
        }
    }

    public void refreshAndSendTokenWithRetry() {
        refreshAndSendToken();

        new Handler(Looper.getMainLooper()).postDelayed(
                this::refreshAndSendToken,
                1500
        );

        new Handler(Looper.getMainLooper()).postDelayed(
                this::refreshAndSendToken,
                3500
        );
    }

    public void sendTokenToServer(String token) {
        if (token == null || token.trim().isEmpty()) {
            Log.w(TAG, "FCM token rỗng");
            return;
        }

        if (!TokenManager.getInstance(context).hasToken()) {
            Log.w(TAG, "Có FCM token nhưng chưa có JWT");
            return;
        }

        String deviceId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        FcmTokenRequest request = new FcmTokenRequest(token, deviceId, "ANDROID");

        Log.d(TAG, "Gọi API lưu FCM token, deviceId = " + deviceId);

        apiService.saveToken(request).enqueue(new Callback<BaseResponse<Object>>() {
            @Override
            public void onResponse(Call<BaseResponse<Object>> call, Response<BaseResponse<Object>> response) {
                Log.d(TAG, "API lưu FCM token responseCode = " + response.code());

                if (response.isSuccessful()) {
                    Log.d(TAG, "Lưu FCM token thành công");
                    return;
                }

                try {
                    String errorBody = response.errorBody() != null
                            ? response.errorBody().string()
                            : "Không có errorBody";
                    Log.e(TAG, "Lưu FCM token thất bại: " + errorBody);
                } catch (Exception e) {
                    Log.e(TAG, "Không đọc được errorBody", e);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Object>> call, Throwable t) {
                Log.e(TAG, "Không gọi được API lưu FCM token", t);
            }
        });
    }
}