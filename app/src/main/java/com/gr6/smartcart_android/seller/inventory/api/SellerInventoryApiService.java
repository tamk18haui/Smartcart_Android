package com.gr6.smartcart_android.seller.inventory.api;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.seller.inventory.model.InventoryUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SellerInventoryApiService {
    @POST("api/v1/inventory/increase")
    Call<BaseResponse<String>> increaseStock(@Body InventoryUpdateRequest request);

    @POST("api/v1/inventory/decrease")
    Call<BaseResponse<String>> decreaseStock(@Body InventoryUpdateRequest request);
}
