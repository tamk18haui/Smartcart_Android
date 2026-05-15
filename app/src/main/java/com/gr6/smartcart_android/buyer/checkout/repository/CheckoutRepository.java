package com.gr6.smartcart_android.buyer.checkout.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.buyer.checkout.api.CheckoutApiService;
import com.gr6.smartcart_android.buyer.checkout.request.CheckoutPreviewRequest;
import com.gr6.smartcart_android.buyer.checkout.request.CreateOrderRequest;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutOrderResponse;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutPreviewResponse;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutVoucherResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutRepository {

    private final CheckoutApiService apiService;

    public CheckoutRepository(Context context) {
        apiService = ApiClient.createService(context, CheckoutApiService.class);
    }

    public void getPreview(
            CheckoutPreviewRequest request,
            CheckoutCallback<CheckoutPreviewResponse> callback
    ) {
        apiService.getCheckoutPreview(request).enqueue(new Callback<BaseResponse<CheckoutPreviewResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<CheckoutPreviewResponse>> call,
                    @NonNull Response<BaseResponse<CheckoutPreviewResponse>> response
            ) {
                handleDataResponse(response, callback, "Không lấy được thông tin thanh toán");
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<CheckoutPreviewResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void createOrder(
            CreateOrderRequest request,
            CheckoutCallback<CheckoutOrderResponse> callback
    ) {
        apiService.createOrder(request).enqueue(new Callback<BaseResponse<CheckoutOrderResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<CheckoutOrderResponse>> call,
                    @NonNull Response<BaseResponse<CheckoutOrderResponse>> response
            ) {
                handleDataResponse(response, callback, "Đặt hàng thất bại");
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<CheckoutOrderResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void getShopVouchers(
            Long shopId,
            CheckoutCallback<List<CheckoutVoucherResponse>> callback
    ) {
        if (shopId == null || shopId <= 0) {
            callback.onError("Shop không hợp lệ");
            return;
        }

        apiService.getShopVouchers(shopId).enqueue(new Callback<BaseResponse<List<CheckoutVoucherResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<CheckoutVoucherResponse>>> call,
                    @NonNull Response<BaseResponse<List<CheckoutVoucherResponse>>> response
            ) {
                handleDataResponse(response, callback, "Không lấy được voucher của shop");
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<List<CheckoutVoucherResponse>>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    private <T> void handleDataResponse(
            Response<BaseResponse<T>> response,
            CheckoutCallback<T> callback,
            String fallbackMessage
    ) {
        if (!response.isSuccessful()) {
            callback.onError(fallbackMessage + ". Mã lỗi: " + response.code());
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

        callback.onSuccess(body.getData(), body.getSafeMessage());
    }

    public interface CheckoutCallback<T> {
        void onSuccess(T data, String message);

        void onError(String message);
    }
}