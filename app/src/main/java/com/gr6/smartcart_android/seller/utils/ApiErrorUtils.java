package com.gr6.smartcart_android.seller.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import retrofit2.Response;

public class ApiErrorUtils {

    private ApiErrorUtils() {
    }

    public static String extractErrorMessage(Response<?> response) {
        if (response == null) {
            return "Không nhận được phản hồi từ server";
        }

        try {
            if (response.errorBody() == null) {
                return "Thao tác thất bại. Mã lỗi: " + response.code();
            }

            String rawError = response.errorBody().string();

            if (rawError == null || rawError.trim().isEmpty()) {
                return "Thao tác thất bại. Mã lỗi: " + response.code();
            }

            JsonObject jsonObject = new Gson().fromJson(rawError, JsonObject.class);

            if (jsonObject == null) {
                return rawError;
            }

            if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull()) {
                return jsonObject.get("message").getAsString();
            }

            if (jsonObject.has("error") && !jsonObject.get("error").isJsonNull()) {
                return jsonObject.get("error").getAsString();
            }

            if (jsonObject.has("detail") && !jsonObject.get("detail").isJsonNull()) {
                return jsonObject.get("detail").getAsString();
            }

            return rawError;
        } catch (IOException e) {
            return "Không đọc được lỗi từ server. Mã lỗi: " + response.code();
        } catch (Exception e) {
            return "Thao tác thất bại. Mã lỗi: " + response.code();
        }
    }
}


