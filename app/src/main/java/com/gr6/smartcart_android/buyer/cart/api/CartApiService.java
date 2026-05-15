package com.gr6.smartcart_android.buyer.cart.api;

import com.gr6.smartcart_android.buyer.cart.request.CartItemRequest;
import com.gr6.smartcart_android.buyer.cart.request.ChangeVariantRequest;
import com.gr6.smartcart_android.buyer.cart.response.CartDetailResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CartApiService {

    @GET("api/v1/storefront/cart/items")
    Call<BaseResponse<CartDetailResponse>> getCartItems();

    @PUT("api/v1/storefront/cart/update")
    Call<BaseResponse<Object>> updateQuantity(@Body CartItemRequest request);

    @DELETE("api/v1/storefront/cart/remove/{variantId}")
    Call<BaseResponse<Object>> removeItem(@Path("variantId") Long variantId);
    @PUT("api/v1/storefront/cart/change-variant")
    Call<BaseResponse<Object>> changeVariant(@Body ChangeVariantRequest request);
}