package com.gr6.smartcart_android.buyer.order.api;

import com.gr6.smartcart_android.buyer.order.request.CancelOrderRequest;
import com.gr6.smartcart_android.buyer.order.response.OrderHistoryResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OrderApiService {

    @GET("api/v2/orders/tracking/history")
    Call<BaseResponse<List<OrderHistoryResponse>>> getOrderHistory();

    @POST("api/v2/orders/refund/{shopOrderId}/cancel")
    Call<BaseResponse<String>> cancelOrder(
            @Path("shopOrderId") Long shopOrderId,
            @Body CancelOrderRequest request
    );
}