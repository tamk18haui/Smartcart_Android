package com.gr6.smartcart_android.seller.order;

import com.gr6.smartcart_android.common.base.BaseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.PUT;

public interface SellerOrderApiService {
    @GET("api/v1/shop-orders")
    Call<BaseResponse<List<SellerOrderListResponse>>> getOrders(@Query("keyword") String keyword);

    @GET("api/v1/shop-orders/{orderId}")
    Call<BaseResponse<SellerOrderDetailResponse>> getOrderDetail(@Path("orderId") Long orderId);

    @PUT("api/v1/shop-orders/{orderId}/status")
    Call<BaseResponse<String>> updateStatus(
            @Path("orderId") Long orderId,
            @Body UpdateShopOrderStatusRequest request
    );
}
