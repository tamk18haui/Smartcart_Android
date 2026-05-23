package com.gr6.smartcart_android.buyer.order.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.buyer.checkout.response.CheckoutOrderResponse;
import com.gr6.smartcart_android.buyer.order.api.OrderApiService;
import com.gr6.smartcart_android.buyer.order.request.CancelOrderRequest;
import com.gr6.smartcart_android.buyer.order.response.OrderDetailResponse;
import com.gr6.smartcart_android.buyer.order.response.OrderHistoryResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepository {

    private final OrderApiService apiService;

    public OrderRepository(Context context) {
        apiService = ApiClient.createService(context, OrderApiService.class);
    }

    public void getOrderHistory(OrderHistoryCallback callback) {
        apiService.getOrderHistory().enqueue(new Callback<BaseResponse<List<OrderHistoryResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<OrderHistoryResponse>>> call,
                    @NonNull Response<BaseResponse<List<OrderHistoryResponse>>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError("Không lấy được lịch sử đơn hàng. Mã lỗi: " + response.code());
                    return;
                }

                BaseResponse<List<OrderHistoryResponse>> body = response.body();

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

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<List<OrderHistoryResponse>>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void getOrderDetail(
            Long shopOrderId,
            OrderCallback<OrderDetailResponse> callback
    ) {
        if (shopOrderId == null || shopOrderId <= 0) {
            callback.onError("shopOrderId không hợp lệ");
            return;
        }

        apiService.getOrderDetail(shopOrderId).enqueue(new Callback<BaseResponse<OrderDetailResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<OrderDetailResponse>> call,
                    @NonNull Response<BaseResponse<OrderDetailResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError("Không lấy được chi tiết đơn hàng. Mã lỗi: " + response.code());
                    return;
                }

                BaseResponse<OrderDetailResponse> body = response.body();

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

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<OrderDetailResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void cancelOrder(Long shopOrderId, String reason, SimpleCallback callback) {
        if (shopOrderId == null || shopOrderId <= 0) {
            callback.onError("shopOrderId không hợp lệ");
            return;
        }

        CancelOrderRequest request = new CancelOrderRequest(reason);

        apiService.cancelOrder(shopOrderId, request).enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<String>> call,
                    @NonNull Response<BaseResponse<String>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError("Không hủy được đơn hàng. Mã lỗi: " + response.code());
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

                callback.onSuccess(body.getSafeMessage());
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

    public void completeOrder(Long shopOrderId, SimpleCallback callback) {
        if (shopOrderId == null || shopOrderId <= 0) {
            callback.onError("shopOrderId không hợp lệ");
            return;
        }

        apiService.completeOrder(shopOrderId).enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<String>> call,
                    @NonNull Response<BaseResponse<String>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError("Không hoàn thành được đơn hàng. Mã lỗi: " + response.code());
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

                callback.onSuccess(body.getSafeMessage());
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

    public void retryPayment(
            Long shopOrderId,
            OrderCallback<CheckoutOrderResponse> callback
    ) {
        if (shopOrderId == null || shopOrderId <= 0) {
            callback.onError("shopOrderId không hợp lệ");
            return;
        }

        apiService.retryPayment(shopOrderId).enqueue(new Callback<BaseResponse<CheckoutOrderResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<CheckoutOrderResponse>> call,
                    @NonNull Response<BaseResponse<CheckoutOrderResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError("Không tạo lại thanh toán. Mã lỗi: " + response.code());
                    return;
                }

                BaseResponse<CheckoutOrderResponse> body = response.body();

                if (body == null) {
                    callback.onError("Server không trả dữ liệu");
                    return;
                }

                if (!body.isSuccess()) {
                    callback.onError(body.getSafeMessage());
                    return;
                }

                if (body.getData() == null || body.getData().getPaymentUrl() == null
                        || body.getData().getPaymentUrl().trim().isEmpty()) {
                    callback.onError("Server chưa trả link thanh toán");
                    return;
                }

                callback.onSuccess(body.getData(), body.getSafeMessage());
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

    public interface OrderCallback<T> {
        void onSuccess(T data, String message);

        void onError(String message);
    }

    public interface OrderHistoryCallback {
        void onSuccess(List<OrderHistoryResponse> data, String message);

        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess(String message);

        void onError(String message);
    }
}