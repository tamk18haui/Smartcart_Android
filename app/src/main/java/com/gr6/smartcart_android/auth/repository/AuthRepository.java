package com.gr6.smartcart_android.auth.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.auth.api.AuthApiService;
import com.gr6.smartcart_android.auth.request.LoginRequest;
import com.gr6.smartcart_android.auth.request.RegisterRequest;
import com.gr6.smartcart_android.auth.request.ShopRegisterRequest;
import com.gr6.smartcart_android.auth.response.LoginResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final AuthApiService authApiService;

    public AuthRepository(Context context) {
        authApiService = ApiClient.createService(context, AuthApiService.class);
    }

    public void login(LoginRequest request, AuthCallback<LoginResponse> callback) {
        authApiService.login(request).enqueue(new Callback<BaseResponse<LoginResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<LoginResponse>> call,
                    @NonNull Response<BaseResponse<LoginResponse>> response
            ) {
                handleDataResponse(response, callback);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<LoginResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void registerBuyer(RegisterRequest request, SimpleCallback callback) {
        authApiService.registerBuyer(request).enqueue(new Callback<BaseResponse<Object>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<Object>> call,
                    @NonNull Response<BaseResponse<Object>> response
            ) {
                handleSimpleResponse(response, callback);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<Object>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void registerSeller(ShopRegisterRequest request, SimpleCallback callback) {
        authApiService.registerSeller(request).enqueue(new Callback<BaseResponse<Object>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<Object>> call,
                    @NonNull Response<BaseResponse<Object>> response
            ) {
                handleSimpleResponse(response, callback);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<Object>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    private <T> void handleDataResponse(
            Response<BaseResponse<T>> response,
            AuthCallback<T> callback
    ) {
        if (!response.isSuccessful()) {
            callback.onError("Thao tác thất bại. Mã lỗi: " + response.code());
            return;
        }

        BaseResponse<T> body = response.body();

        if (body == null) {
            callback.onError("Server không trả dữ liệu");
            return;
        }

        if (!body.isSuccess()) {
            callback.onError(body.getSafeMessage());
            return;
        }

        if (body.getData() == null) {
            callback.onError("Dữ liệu trả về không hợp lệ");
            return;
        }

        callback.onSuccess(body.getData(), body.getSafeMessage());
    }

    private void handleSimpleResponse(
            Response<BaseResponse<Object>> response,
            SimpleCallback callback
    ) {
        if (!response.isSuccessful()) {
            callback.onError("Thao tác thất bại. Mã lỗi: " + response.code());
            return;
        }

        BaseResponse<Object> body = response.body();

        if (body == null) {
            callback.onError("Server không trả dữ liệu");
            return;
        }

        if (!body.isSuccess()) {
            callback.onError(body.getSafeMessage());
            return;
        }

        callback.onSuccess(body.getSafeMessage());
    }

    public interface AuthCallback<T> {
        void onSuccess(T data, String message);

        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess(String message);

        void onError(String message);
    }
}