package com.gr6.smartcart_android.buyer.product.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.buyer.product.api.ProductApiService;
import com.gr6.smartcart_android.buyer.product.request.AddToCartRequest;
import com.gr6.smartcart_android.buyer.product.response.ProductDetailResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {

    private final ProductApiService apiService;

    public ProductRepository(Context context) {
        apiService = ApiClient.createService(context, ProductApiService.class);
    }

    public void getProductDetail(Long productId, ProductDetailCallback callback) {
        apiService.getProductDetail(productId).enqueue(new Callback<BaseResponse<ProductDetailResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<ProductDetailResponse>> call,
                    @NonNull Response<BaseResponse<ProductDetailResponse>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError("Lấy chi tiết sản phẩm thất bại. Mã lỗi: " + response.code());
                    return;
                }

                BaseResponse<ProductDetailResponse> body = response.body();

                if (body == null) {
                    callback.onError("Server không trả dữ liệu chi tiết sản phẩm");
                    return;
                }

                if (!body.isSuccess()) {
                    callback.onError(body.getSafeMessage());
                    return;
                }

                if (body.getData() == null) {
                    callback.onError("Dữ liệu chi tiết sản phẩm không hợp lệ");
                    return;
                }

                callback.onSuccess(body.getData());
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<ProductDetailResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void addToCart(Long variantId, Integer quantity, ActionCallback callback) {
        AddToCartRequest request = new AddToCartRequest(variantId, quantity);

        apiService.addToCart(request).enqueue(new Callback<BaseResponse<Object>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<Object>> call,
                    @NonNull Response<BaseResponse<Object>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError("Thêm vào giỏ hàng thất bại. Mã lỗi: " + response.code());
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

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<Object>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void getShopVouchers(
            Long shopId,
            VoucherCallback callback
    ) {
        apiService.getShopVouchers(shopId).enqueue(new Callback<BaseResponse<List<ProductDetailResponse.ShopVoucherDTO>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<ProductDetailResponse.ShopVoucherDTO>>> call,
                    @NonNull Response<BaseResponse<List<ProductDetailResponse.ShopVoucherDTO>>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError("Không lấy được voucher. Mã lỗi: " + response.code());
                    return;
                }

                BaseResponse<List<ProductDetailResponse.ShopVoucherDTO>> body = response.body();

                if (body == null) {
                    callback.onError("Server không trả dữ liệu voucher");
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
                    @NonNull Call<BaseResponse<List<ProductDetailResponse.ShopVoucherDTO>>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server voucher: " + t.getMessage());
            }
        });
    }

    public interface VoucherCallback {
        void onSuccess(List<ProductDetailResponse.ShopVoucherDTO> vouchers);

        void onError(String message);
    }
    public interface ProductDetailCallback {
        void onSuccess(ProductDetailResponse data);

        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess(String message);

        void onError(String message);
    }
}