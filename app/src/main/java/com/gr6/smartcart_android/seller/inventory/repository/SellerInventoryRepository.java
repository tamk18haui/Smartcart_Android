package com.gr6.smartcart_android.seller.inventory.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.seller.inventory.api.SellerInventoryApiService;
import com.gr6.smartcart_android.seller.inventory.request.InventoryUpdateRequest;
import com.gr6.smartcart_android.seller.inventory.response.InventoryItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerInventoryRepository {
    private final SellerInventoryApiService apiService;

    public SellerInventoryRepository(Context context) {
        apiService = ApiClient.createService(context, SellerInventoryApiService.class);
    }

    public Call<BaseResponse<List<InventoryItemResponse>>> getSellerInventory(String keyword, Integer lowStockThreshold) {
        return apiService.getSellerInventory(keyword, lowStockThreshold);
    }

    public Call<BaseResponse<String>> increaseStock(Long variantId, int quantity) {
        return apiService.increaseStock(new InventoryUpdateRequest(variantId, quantity));
    }

    public Call<BaseResponse<String>> decreaseStock(Long variantId, int quantity) {
        return apiService.decreaseStock(new InventoryUpdateRequest(variantId, quantity));
    }

    public void loadInventory(String keyword, Integer lowStockThreshold, InventoryCallback<List<InventoryItemResponse>> callback) {
        getSellerInventory(keyword, lowStockThreshold).enqueue(wrap(callback, "Không lấy được danh sách tồn kho"));
    }

    public void addStock(Long variantId, int quantity, InventoryCallback<String> callback) {
        if (variantId == null || variantId <= 0) {
            callback.onError("Không tìm thấy biến thể cần nhập kho");
            return;
        }
        if (quantity <= 0) {
            callback.onError("Số lượng nhập phải lớn hơn 0");
            return;
        }
        increaseStock(variantId, quantity).enqueue(wrap(callback, "Nhập kho thất bại"));
    }

    public void subtractStock(Long variantId, int quantity, InventoryCallback<String> callback) {
        if (variantId == null || variantId <= 0) {
            callback.onError("Không tìm thấy biến thể cần xuất/điều chỉnh kho");
            return;
        }
        if (quantity <= 0) {
            callback.onError("Số lượng giảm phải lớn hơn 0");
            return;
        }
        decreaseStock(variantId, quantity).enqueue(wrap(callback, "Giảm kho thất bại"));
    }

    private <T> Callback<BaseResponse<T>> wrap(InventoryCallback<T> callback, String defaultError) {
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

    public interface InventoryCallback<T> {
        void onSuccess(T data, String message);
        void onError(String message);
    }
}
