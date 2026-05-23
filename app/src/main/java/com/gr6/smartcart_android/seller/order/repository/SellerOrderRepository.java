package com.gr6.smartcart_android.seller.order.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.seller.order.api.SellerOrderApiService;
import com.gr6.smartcart_android.seller.order.request.UpdateShopOrderStatusRequest;
import com.gr6.smartcart_android.seller.order.response.OrderDetailResponse;
import com.gr6.smartcart_android.seller.order.response.OrderListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerOrderRepository {

    private final SellerOrderApiService apiService;

    public SellerOrderRepository(Context context) {
        apiService = ApiClient.createService(context, SellerOrderApiService.class);
    }

    public void loadOrders(
            String keyword,
            OrderCallback<List<OrderListResponse>> callback
    ) {
        apiService.getOrders(keyword).enqueue(new Callback<BaseResponse<List<OrderListResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<OrderListResponse>>> call,
                    @NonNull Response<BaseResponse<List<OrderListResponse>>> response
            ) {
                handleDataResponse(response, callback);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<List<OrderListResponse>>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void loadOrderDetail(
            Long orderId,
            OrderCallback<OrderDetailResponse> callback
    ) {
        apiService.getOrderDetail(orderId).enqueue(new Callback<BaseResponse<OrderDetailResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<OrderDetailResponse>> call,
                    @NonNull Response<BaseResponse<OrderDetailResponse>> response
            ) {
                handleDataResponse(response, callback);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<OrderDetailResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void updateOrderStatus(
            Long orderId,
            String status,
            String reason,
            SimpleCallback callback
    ) {
        UpdateShopOrderStatusRequest request =
                new UpdateShopOrderStatusRequest(status, reason);

        apiService.updateStatus(orderId, request).enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<String>> call,
                    @NonNull Response<BaseResponse<String>> response
            ) {
                handleSimpleResponse(response, callback);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<String>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void confirmOrderStatus(
            Long orderId,
            SimpleCallback callback
    ) {
        apiService.confirmOrder(orderId).enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<String>> call,
                    @NonNull Response<BaseResponse<String>> response
            ) {
                handleSimpleResponse(response, callback);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<String>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    private <T> void handleDataResponse(
            Response<BaseResponse<T>> response,
            OrderCallback<T> callback
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

        callback.onSuccess(body.getData(), body.getSafeMessage());
    }

    private void handleSimpleResponse(
            Response<BaseResponse<String>> response,
            SimpleCallback callback
    ) {
        if (!response.isSuccessful()) {
            callback.onError("Thao tác thất bại. Mã lỗi: " + response.code());
            return;
        }

        BaseResponse<String> body = response.body();

        if (body == null) {
            callback.onError("Server không trả dữ liệu");
            return;
        }

        if (!body.isSuccess()) {
            callback.onError(body.getSafeMessage());
            return;
        }

        String message = body.getData();

        if (message == null || message.trim().isEmpty()) {
            message = body.getSafeMessage();
        }

        callback.onSuccess(message);
    }

    public interface OrderCallback<T> {
        void onSuccess(T data, String message);

        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess(String message);

        void onError(String message);
    }
}