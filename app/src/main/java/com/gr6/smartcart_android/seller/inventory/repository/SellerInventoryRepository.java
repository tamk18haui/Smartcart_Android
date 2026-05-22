package com.gr6.smartcart_android.seller.inventory.repository;

import android.content.Context;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.seller.inventory.api.SellerInventoryApiService;
import com.gr6.smartcart_android.seller.inventory.model.InventoryUpdateRequest;

import retrofit2.Call;

public class SellerInventoryRepository {
    private final SellerInventoryApiService apiService;

    public SellerInventoryRepository(Context context) {
        apiService = ApiClient.createService(context, SellerInventoryApiService.class);
    }

    public Call<BaseResponse<String>> increaseStock(Long variantId, int quantity) {
        return apiService.increaseStock(new InventoryUpdateRequest(variantId, quantity));
    }

    public Call<BaseResponse<String>> decreaseStock(Long variantId, int quantity) {
        return apiService.decreaseStock(new InventoryUpdateRequest(variantId, quantity));
    }
}
