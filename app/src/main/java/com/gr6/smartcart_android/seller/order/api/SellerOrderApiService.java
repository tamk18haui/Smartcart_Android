package com.gr6.smartcart_android.seller.order.api;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.seller.order.request.UpdateShopOrderStatusRequest;
import com.gr6.smartcart_android.seller.order.response.OrderDetailResponse;
import com.gr6.smartcart_android.seller.order.response.OrderListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SellerOrderApiService {

    @GET("api/v1/shop-orders")
    Call<BaseResponse<List<OrderListResponse>>> getOrders(
            @Query("keyword") String keyword
    );

    @GET("api/v1/shop-orders/{id}")
    Call<BaseResponse<OrderDetailResponse>> getOrderDetail(
            @Path("id") Long id
    );

    @PUT("api/v1/shop-orders/{id}/status")
    Call<BaseResponse<String>> updateStatus(
            @Path("id") Long id,
            @Body UpdateShopOrderStatusRequest request
    );

    @PUT("api/v1/shop-orders/{id}/confirm")
    Call<BaseResponse<String>> confirmOrder(
            @Path("id") Long id
    );
}