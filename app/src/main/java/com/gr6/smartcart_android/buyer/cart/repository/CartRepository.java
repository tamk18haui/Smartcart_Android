package com.gr6.smartcart_android.buyer.cart.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.buyer.cart.api.CartApiService;
import com.gr6.smartcart_android.buyer.cart.request.CartItemRequest;
import com.gr6.smartcart_android.buyer.cart.request.ChangeVariantRequest;
import com.gr6.smartcart_android.buyer.cart.response.CartDetailResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartRepository {

    private final CartApiService apiService;

    public CartRepository(Context context) {
        apiService = ApiClient.createService(context, CartApiService.class);
    }

    public void getCartItems(CartCallback callback) {
        apiService.getCartItems().enqueue(new Callback<BaseResponse<CartDetailResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<CartDetailResponse>> call,
                    @NonNull Response<BaseResponse<CartDetailResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError("Lấy giỏ hàng thất bại. Mã lỗi: " + response.code());
                    return;
                }

                BaseResponse<CartDetailResponse> body = response.body();

                if (body == null) {
                    callback.onError("Server không trả dữ liệu giỏ hàng");
                    return;
                }

                if (!body.isSuccess()) {
                    callback.onError(body.getSafeMessage());
                    return;
                }

                callback.onSuccess(body.getData());
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<CartDetailResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void updateQuantity(Long variantId, Integer quantity, SimpleCallback callback) {
        CartItemRequest request = new CartItemRequest(variantId, quantity);

        apiService.updateQuantity(request).enqueue(new Callback<BaseResponse<Object>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<Object>> call,
                    @NonNull Response<BaseResponse<Object>> response
            ) {
                handleSimpleResponse(response, callback, "Cập nhật số lượng thất bại");
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

    public void removeItem(Long variantId, SimpleCallback callback) {
        apiService.removeItem(variantId).enqueue(new Callback<BaseResponse<Object>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<Object>> call,
                    @NonNull Response<BaseResponse<Object>> response
            ) {
                handleSimpleResponse(response, callback, "Xóa sản phẩm thất bại");
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
    public void changeVariant(Long cartItemId, Long newVariantId, SimpleCallback callback) {
        ChangeVariantRequest request = new ChangeVariantRequest(cartItemId, newVariantId);

        apiService.changeVariant(request).enqueue(new Callback<BaseResponse<Object>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<Object>> call,
                    @NonNull Response<BaseResponse<Object>> response
            ) {
                handleSimpleResponse(response, callback, "Đổi phân loại thất bại");
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

    private void handleSimpleResponse(
            Response<BaseResponse<Object>> response,
            SimpleCallback callback,
            String fallbackMessage
    ) {
        if (!response.isSuccessful()) {
            callback.onError(fallbackMessage + ". Mã lỗi: " + response.code());
            return;
        }

        BaseResponse<Object> body = response.body();

        if (body == null) {
            callback.onError("Server không trả phản hồi");
            return;
        }

        if (!body.isSuccess()) {
            callback.onError(body.getSafeMessage());
            return;
        }

        callback.onSuccess(body.getSafeMessage());
    }

    public interface CartCallback {
        void onSuccess(CartDetailResponse data);

        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess(String message);

        void onError(String message);
    }
}