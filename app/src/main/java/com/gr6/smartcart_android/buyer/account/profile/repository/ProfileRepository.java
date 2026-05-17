package com.gr6.smartcart_android.buyer.account.profile.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.gr6.smartcart_android.buyer.account.profile.api.ProfileApiService;
import com.gr6.smartcart_android.buyer.account.profile.request.ProfileUpdateRequest;
import com.gr6.smartcart_android.buyer.account.profile.response.ProfileResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {

    private final ProfileApiService apiService;

    public ProfileRepository(Context context) {
        apiService = ApiClient.createService(context, ProfileApiService.class);
    }

    public void getProfile(ProfileCallback callback) {
        apiService.getProfile().enqueue(new Callback<BaseResponse<ProfileResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<ProfileResponse>> call,
                    @NonNull Response<BaseResponse<ProfileResponse>> response
            ) {
                handleDataResponse(response, callback, "Không lấy được thông tin tài khoản");
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<ProfileResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void updateProfile(
            ProfileUpdateRequest request,
            ProfileCallback callback
    ) {
        apiService.updateProfile(request).enqueue(new Callback<BaseResponse<ProfileResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<ProfileResponse>> call,
                    @NonNull Response<BaseResponse<ProfileResponse>> response
            ) {
                handleDataResponse(response, callback, "Cập nhật tài khoản thất bại");
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<ProfileResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    private void handleDataResponse(
            Response<BaseResponse<ProfileResponse>> response,
            ProfileCallback callback,
            String fallbackMessage
    ) {
        if (!response.isSuccessful()) {
            callback.onError(parseErrorMessage(response, fallbackMessage));
            return;
        }

        BaseResponse<ProfileResponse> body = response.body();

        if (body == null) {
            callback.onError("Server không trả dữ liệu");
            return;
        }

        if (!body.isSuccess()) {
            callback.onError(body.getSafeMessage());
            return;
        }

        if (body.getData() == null) {
            callback.onError("Dữ liệu tài khoản không hợp lệ");
            return;
        }

        callback.onSuccess(body.getData(), body.getSafeMessage());
    }

    private String parseErrorMessage(
            Response<?> response,
            String fallbackMessage
    ) {
        try {
            if (response.errorBody() == null) {
                return fallbackMessage + ". Mã lỗi: " + response.code();
            }

            String raw = response.errorBody().string();

            if (raw == null || raw.trim().isEmpty()) {
                return fallbackMessage + ". Mã lỗi: " + response.code();
            }

            JsonObject json = new Gson().fromJson(raw, JsonObject.class);

            if (json != null
                    && json.has("message")
                    && !json.get("message").isJsonNull()) {
                return json.get("message").getAsString();
            }

            return fallbackMessage + ". Mã lỗi: " + response.code();
        } catch (Exception e) {
            return fallbackMessage + ". Mã lỗi: " + response.code();
        }
    }

    public interface ProfileCallback {
        void onSuccess(ProfileResponse data, String message);

        void onError(String message);
    }
}