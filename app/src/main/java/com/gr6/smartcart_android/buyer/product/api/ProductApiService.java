package com.gr6.smartcart_android.buyer.product.api;

import com.gr6.smartcart_android.buyer.product.request.AddToCartRequest;
import com.gr6.smartcart_android.buyer.product.response.ProductDetailResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ProductApiService {

    @GET("api/v1/fulfillment/product/{productId}")
    Call<BaseResponse<ProductDetailResponse>> getProductDetail(
            @Path("productId") Long productId
    );

    @POST("api/v1/storefront/cart/add")
    Call<BaseResponse<Object>> addToCart(@Body AddToCartRequest request);

    @GET("api/v1/vouchers/shop/{shopId}")
    Call<BaseResponse<List<ProductDetailResponse.ShopVoucherDTO>>> getShopVouchers(
            @Path("shopId") Long shopId
    );
}