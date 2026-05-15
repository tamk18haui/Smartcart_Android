package com.gr6.smartcart_android.buyer.checkout.api;

import com.gr6.smartcart_android.buyer.checkout.request.CheckoutPreviewRequest;
import com.gr6.smartcart_android.buyer.checkout.request.CreateOrderRequest;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutOrderResponse;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutPreviewResponse;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutVoucherResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CheckoutApiService {

    @POST("api/v1/orders/preview")
    Call<BaseResponse<CheckoutPreviewResponse>> getCheckoutPreview(
            @Body CheckoutPreviewRequest request
    );

    @POST("api/v1/orders/checkout")
    Call<BaseResponse<CheckoutOrderResponse>> createOrder(
            @Body CreateOrderRequest request
    );

    @GET("api/v1/vouchers/shop/{shopId}")
    Call<BaseResponse<List<CheckoutVoucherResponse>>> getShopVouchers(
            @Path("shopId") Long shopId
    );
}