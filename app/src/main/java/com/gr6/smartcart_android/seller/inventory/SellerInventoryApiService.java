package com.gr6.smartcart_android.seller.inventory;

import com.gr6.smartcart_android.common.base.BaseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SellerInventoryApiService {
    @GET("api/v1/inventory/seller")
    Call<BaseResponse<List<InventoryItemResponse>>> getSellerInventory(
            @Query("keyword") String keyword,
            @Query("lowStockThreshold") Integer lowStockThreshold
    );

    @POST("api/v1/inventory/increase")
    Call<BaseResponse<String>> increaseStock(@Body InventoryUpdateRequest request);

    @POST("api/v1/inventory/decrease")
    Call<BaseResponse<String>> decreaseStock(@Body InventoryUpdateRequest request);
}
