package com.gr6.smartcart_android.seller.product.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.base.PageResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.seller.product.api.SellerProductApiService;
import com.gr6.smartcart_android.seller.product.request.ProductRequest;
import com.gr6.smartcart_android.seller.product.response.CategoryResponse;
import com.gr6.smartcart_android.seller.product.response.ProductResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerProductRepository {

    private final SellerProductApiService apiService;

    public SellerProductRepository(Context context) {
        apiService = ApiClient.createService(context, SellerProductApiService.class);
    }

    public Call<BaseResponse<List<CategoryResponse>>> getCategories() {
        return apiService.getCategories();
    }

    public Call<BaseResponse<List<String>>> getBrandSuggestions(String keyword) {
        return apiService.getBrandSuggestions(keyword, null);
    }

    public Call<BaseResponse<List<String>>> getBrandSuggestions(String keyword, Long categoryId) {
        return apiService.getBrandSuggestions(keyword, categoryId);
    }

    public Call<BaseResponse<ProductResponse>> createProduct(ProductRequest request) {
        return apiService.createProduct(request);
    }

    public Call<BaseResponse<PageResponse<ProductResponse>>> getProductsByShop(Long shopId, int page, int size) {
        return apiService.getProductsByShop(shopId, page, size);
    }

    public Call<BaseResponse<ProductResponse>> getProductForSeller(Long productId) {
        return apiService.getProductForSeller(productId);
    }

    public Call<BaseResponse<ProductResponse>> updateProduct(Long productId, ProductRequest request) {
        return apiService.updateProduct(productId, request);
    }

    public Call<BaseResponse<ProductResponse>> toggleProductVisibility(Long productId, boolean hidden) {
        return apiService.toggleProductVisibility(productId, hidden);
    }

    public void loadCategories(ProductCallback<List<CategoryResponse>> callback) {
        apiService.getCategories().enqueue(wrap(callback, "Không lấy được danh mục"));
    }

    public void loadBrandSuggestions(String keyword, ProductCallback<List<String>> callback) {
        apiService.getBrandSuggestions(keyword, null).enqueue(wrap(callback, "Không lấy được thương hiệu"));
    }

    public void loadBrandSuggestions(String keyword, Long categoryId, ProductCallback<List<String>> callback) {
        apiService.getBrandSuggestions(keyword, categoryId).enqueue(wrap(callback, "Không lấy được thương hiệu"));
    }

    public void submitProduct(ProductRequest request, ProductCallback<ProductResponse> callback) {
        apiService.createProduct(request).enqueue(wrap(callback, "Đăng sản phẩm thất bại"));
    }

    public void loadProductsByShop(Long shopId, int page, int size, ProductCallback<PageResponse<ProductResponse>> callback) {
        if (shopId == null || shopId <= 0) {
            callback.onError("Không tìm thấy shop để tải sản phẩm");
            return;
        }

        apiService.getProductsByShop(shopId, page, size)
                .enqueue(wrap(callback, "Không lấy được danh sách sản phẩm"));
    }

    public void loadProductForSeller(Long productId, ProductCallback<ProductResponse> callback) {
        if (productId == null || productId <= 0) {
            callback.onError("Không tìm thấy sản phẩm");
            return;
        }

        apiService.getProductForSeller(productId)
                .enqueue(wrap(callback, "Không lấy được chi tiết sản phẩm"));
    }

    public void submitProductUpdate(Long productId, ProductRequest request, ProductCallback<ProductResponse> callback) {
        if (productId == null || productId <= 0) {
            callback.onError("Không tìm thấy sản phẩm cần sửa");
            return;
        }

        apiService.updateProduct(productId, request)
                .enqueue(wrap(callback, "Cập nhật sản phẩm thất bại"));
    }

    public void setProductHidden(Long productId, boolean hidden, ProductCallback<ProductResponse> callback) {
        if (productId == null || productId <= 0) {
            callback.onError("Không tìm thấy sản phẩm cần cập nhật");
            return;
        }

        apiService.toggleProductVisibility(productId, hidden)
                .enqueue(wrap(callback, hidden ? "Tạm ẩn sản phẩm thất bại" : "Hiển thị sản phẩm thất bại"));
    }

    private <T> Callback<BaseResponse<T>> wrap(ProductCallback<T> callback, String defaultError) {
        return new Callback<BaseResponse<T>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<T>> call, @NonNull Response<BaseResponse<T>> response) {
                if (!response.isSuccessful()) {
                    callback.onError(defaultError + ". Mã lỗi: " + response.code());
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

            @Override
            public void onFailure(@NonNull Call<BaseResponse<T>> call, @NonNull Throwable t) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        };
    }

    public interface ProductCallback<T> {
        void onSuccess(T data, String message);

        void onError(String message);
    }
}